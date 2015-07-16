/**
 * File CFLSolver.java
 *
 * Copyright 2014 Coen van Leeuwen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package nl.coenvl.sam.solvers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nl.coenvl.sam.agents.Agent;
import nl.coenvl.sam.agents.LocalCommunicatingAgent;
import nl.coenvl.sam.costfunctions.CostFunction;
import nl.coenvl.sam.exceptions.InvalidValueException;
import nl.coenvl.sam.exceptions.VariableNotSetException;
import nl.coenvl.sam.messages.HashMessage;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.problemcontexts.LocalProblemContext;
import nl.coenvl.sam.variables.IntegerVariable;

/**
 * MGM2Solver
 * 
 * Based on the paper 'Distributed Algorithms for DCOP: A Graphical-Game-Based
 * Approach' of Maheswaran, Pearce and Tambe.
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 2 april 2015
 *
 */
public class MGM2Solver implements IterativeSolver {

	/**
	 * State
	 *
	 * @author leeuwencjv
	 * @version 0.1
	 * @since 10 apr. 2015
	 *
	 */
	public enum State {
		Value, Offer, AcceptReject, Gain, GoNoGo
	}

	private static final double OFFER_PROBABILITY = 0.5; // (Q in paper)

	// private static final double ACTIVATION_PROBABILITY = 0.5; // (P in paper)

	private static final double EQUAL_UPDATE_PROBABILITY = 0.5;

	private static final String UPDATE_VALUE = "MGM2:UpdateValue";

	private static final String OFFER = "MGM2:MoveOffer";

	private static final String ACCEPT = "MGM2:AcceptOffer";

	private static final String GAIN = "MGM2:UtilityGain";

	private static final String GO = "MGM2:GO";

	private CostFunction myCostFunction;

	private LocalProblemContext<Integer> myProblemContext;

	private IntegerVariable myVariable;

	private LocalCommunicatingAgent parent;

	private State algoState;

	private List<Offer> receivedOffers;

	private Map<Agent, Double> neighborGains;

	private boolean isOfferer;

	private Offer committedOffer;

	private Integer bestLocalAssignment;

	private double bestLocalReduction;

	public MGM2Solver(LocalCommunicatingAgent agent, CostFunction costfunction) {
		this.parent = agent;
		this.myCostFunction = costfunction;
		this.myVariable = (IntegerVariable) this.parent.getVariable();
	}

	@Override
	public void init() {
		this.algoState = State.Value;
		this.myProblemContext = new LocalProblemContext<Integer>(this.parent);
		this.receivedOffers = new LinkedList<Offer>();
		this.neighborGains = new HashMap<Agent, Double>();

		// So that it will not try to pick a local best at first
		this.committedOffer = null; // new Offer(null, null, null, null, null);
		this.isOfferer = false;

		try {
			this.myVariable.setValue(this.myVariable.getRandomValue());
		} catch (InvalidValueException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void push(Message m) {
		Agent source = (Agent) m.getContent("source");

		if (m.getType().equals(MGM2Solver.UPDATE_VALUE)) {
			Integer value = (Integer) m.getContent("value");

			this.myProblemContext.setValue(source, value);
		} else if (m.getType().equals(MGM2Solver.OFFER)) {

			// Any OFFER message should contain this...
			if (m.hasContent("offers")) {
				@SuppressWarnings("unchecked")
				List<Offer> offerList = (List<Offer>) m.getContent("offers");
				this.receivedOffers.addAll(offerList);
			}

		} else if (m.getType().equals(MGM2Solver.ACCEPT)) {

			this.committedOffer = (Offer) m.getContent("offer");

		} else if (m.getType().equals(MGM2Solver.GAIN)) {

			// Any ACCEPT message should contain this...
			Double gain = (Double) m.getContent("gain");
			this.neighborGains.put(source, gain);

		} else if (m.getType().equals(MGM2Solver.GO)) {

			if (this.committedOffer == null) {
				System.err.println("Warning, something stinky is going on!");
				return;
			}

			try {
				if (this.isOfferer) {
					assert (source == this.committedOffer.receiver);
					assert (this.parent == this.committedOffer.offerer);

					// System.out.println(this.parent.getName()
					// + " is go as offerer");

					this.myVariable.setValue(this.committedOffer.offererValue);
				} else {
					assert (source == this.committedOffer.offerer);
					assert (this.parent == this.committedOffer.receiver);

					// System.out.println(this.parent.getName()
					// + " is go as receiver");

					this.myVariable.setValue(this.committedOffer.receiverValue);
				}
				this.committedOffer = null;
			} catch (InvalidValueException e) {
				e.printStackTrace();
			}

		} else {
			throw new RuntimeException("Unexpected message: " + m.getType());
		}
	}

	@Override
	public synchronized void tick() {
		switch (this.algoState) {
		case Offer:
			this.sendOffer();
			this.algoState = State.AcceptReject;
			break;

		case AcceptReject:
			this.sendAccept();
			this.algoState = State.Gain;
			break;

		case Gain:
			this.sendGain();
			this.algoState = State.GoNoGo;
			break;

		case GoNoGo:
			this.sendGo();
			this.algoState = State.Value;
			break;

		default:
		case Value:
			sendValue();
			this.committedOffer = null;
			this.algoState = State.Offer;
			break;
		}
	}

	private void sendValue() {
		try {
			Message updateMsg = new HashMessage(MGM2Solver.UPDATE_VALUE);

			updateMsg
					.addContent("value", this.myVariable.getValue().intValue());
			updateMsg.addContent("source", this.parent);

			for (Agent n : this.parent.getNeighborhood())
				n.push(updateMsg);

		} catch (VariableNotSetException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Decides IF this neighbor is an offerer or a receiver. If we are an
	 * offerer: send a list of offers to the neighbor with local cost
	 * reductions. If receiver, do nothing.
	 */
	private void sendOffer() {
		// if (1 == 1)	return;
		
		// First determine wether we will offer or receive
		if (Math.random() > MGM2Solver.OFFER_PROBABILITY) {

			// Select a random neighbor
			int r = (int) Math.ceil(Math.random()
					* this.parent.getNeighborhood().size());
			Iterator<Agent> neighborSelector = this.parent.getNeighborhood()
					.iterator();

			Agent neighbor = null;
			while (r > 0 && neighborSelector.hasNext()) {
				neighbor = neighborSelector.next();
				r--;
			}

			assert (neighbor != null);

			// Get all offers that reduce the local cost
			List<Offer> offerList = new LinkedList<Offer>();

			try {
				this.myProblemContext.setValue(this.myVariable.getValue());
			} catch (VariableNotSetException e) {
				e.printStackTrace();
			}

			LocalProblemContext<Integer> temp = new LocalProblemContext<Integer>(
					this.parent);
			@SuppressWarnings("unchecked")
			HashMap<Agent, Integer> assignment = (HashMap<Agent, Integer>) this.myProblemContext
					.getAssignment().clone();
			temp.setAssignment(assignment);

			double before = this.myCostFunction.evaluate(myProblemContext);

			for (Integer i : this.myVariable) {
				temp.setValue(i);
				for (Integer j : this.myVariable) {
					temp.setValue(neighbor, j);
					double val = this.myCostFunction.evaluate(temp);
					if (val < before)
						offerList.add(new Offer(this.parent, neighbor, i, j,
								before - val));
				}
			}

			// Send the offers to the randomly selected neighbor
			Message offerMessage = new HashMessage(OFFER);
			offerMessage.addContent("source", this.parent);
			offerMessage.addContent("offers", offerList);

			neighbor.push(offerMessage);

			this.isOfferer = true;
		} else {
			this.isOfferer = false;
		}

	}

	/**
	 * 
	 */
	private void sendAccept() {
		//if (1 == 1) return;

		// Only if this Agent did not offer to anyone else...
		if (!this.isOfferer) {

			LocalProblemContext<Integer> temp = new LocalProblemContext<Integer>(
					this.parent);
			@SuppressWarnings("unchecked")
			HashMap<Agent, Integer> cpa = (HashMap<Agent, Integer>) this.myProblemContext
					.getAssignment().clone();

			double bestGain = Double.MIN_VALUE;
			double before = this.myCostFunction.evaluate(myProblemContext);
			Offer bestOffer = null;

			for (Offer suggestedOffer : this.receivedOffers) {
				temp.setAssignment(cpa);
				temp.setValue(suggestedOffer.offerer,
						suggestedOffer.offererValue);
				temp.setValue(suggestedOffer.receiverValue);

				double val = this.myCostFunction.evaluate(temp);
				suggestedOffer.reiceverReduction = before - val;
				double globalReduction = computeGlobalGain(
						suggestedOffer.offererReduction,
						suggestedOffer.reiceverReduction);
				if (globalReduction > 0 && globalReduction > bestGain) {
					bestGain = globalReduction;
					bestOffer = suggestedOffer;
				}
			}

			// Send accept if there is a global reduction
			if (bestOffer != null) {
				Message accept = new HashMessage(MGM2Solver.ACCEPT);
				accept.addContent("source", this.parent);
				accept.addContent("offer", bestOffer);

				// System.out.println(this.parent.getName()
				// + " accepts offer from " + bestOffer.offerer.getName());
				bestOffer.offerer.push(accept);

				// Set the value now
				this.committedOffer = bestOffer;
				try {
					this.myVariable.setValue(bestOffer.receiverValue);
				} catch (InvalidValueException e) {
					e.printStackTrace();
				}
			}
		}

		// Clear offers for the next round
		this.receivedOffers.clear();
	}

	private void sendGain() {
		// System.out.println(this.parent.getName() + " sending gain messages");
		Message gainMessage = new HashMessage(MGM2Solver.GAIN);
		gainMessage.addContent("source", this.parent);
		if (this.committedOffer != null) {

			this.bestLocalReduction = computeGlobalGain(
					this.committedOffer.offererReduction,
					this.committedOffer.reiceverReduction);
			gainMessage.addContent("gain", this.bestLocalReduction);

		} else {

			try {
				this.myProblemContext.setValue(this.myVariable.getValue());
			} catch (VariableNotSetException e) {
				e.printStackTrace();
			}

			double before = this.myCostFunction.evaluate(this.myProblemContext);
			double bestCost = before; //Double.MAX_VALUE;
			Integer bestAssignment = null;

			for (Integer assignment : this.myVariable) {
				this.myProblemContext.setValue(assignment);

				double localCost = this.myCostFunction
						.evaluate(this.myProblemContext);

				if (localCost < bestCost) {
					bestCost = localCost;
					bestAssignment = assignment;
				}
			}

			this.bestLocalReduction = before - bestCost;
			this.bestLocalAssignment = bestAssignment;

			gainMessage.addContent("gain", this.bestLocalReduction);
		}

		for (Agent n : this.parent.getNeighborhood())
			n.push(gainMessage);
	}

	private void sendGo() {
		// System.out.println(this.parent.getName() + "Analyzing!");

		if (committedOffer == null) {
			// If there was no better solution skip this step
			if (bestLocalAssignment == null)
				return;

			Double bestNeighborReduction = Double.MIN_VALUE;
			for (Agent n : this.parent.getNeighborhood())
				if (this.neighborGains.get(n) > bestNeighborReduction)
					bestNeighborReduction = this.neighborGains.get(n);

			// If this solution is better than any of the neighbors, do the
			// update
			try {
				if (this.bestLocalReduction > bestNeighborReduction)
					this.myVariable.setValue(bestLocalAssignment);
				if (this.bestLocalReduction == bestNeighborReduction
						&& Math.random() > EQUAL_UPDATE_PROBABILITY)
					this.myVariable.setValue(bestLocalAssignment);
			} catch (InvalidValueException e) {
				e.printStackTrace();
			}

		} else {
			Agent partner;
			if (this.isOfferer) {
				assert (this.committedOffer.offerer == this.parent);
				partner = this.committedOffer.receiver;
			} else {
				assert (this.committedOffer.receiver == this.parent);
				partner = this.committedOffer.offerer;
			}

			Double bestNeighborReduction = Double.MIN_VALUE;
			for (Agent n : this.parent.getNeighborhood())
				if (n != partner
						&& this.neighborGains.get(n) > bestNeighborReduction)
					bestNeighborReduction = this.neighborGains.get(n);

			if (this.bestLocalReduction > bestNeighborReduction) {
				Message goMessage = new HashMessage(MGM2Solver.GO);
				// This is not strictly necessary, but do it for assertion later
				// on
				goMessage.addContent("source", this.parent);
				// System.out.println(this.parent.getName() +
				// " GO with partner "
				// + partner.getName());
				partner.push(goMessage);
			}

			// committedOffer = null;
		}

		this.neighborGains.clear();
	}

	@Override
	public void reset() {
		this.myVariable.clear();
	}

	private class Offer {
		public Agent offerer;

		public Agent receiver;

		public Integer offererValue;

		public Integer receiverValue;

		public Double offererReduction;

		public Double reiceverReduction;

		public Offer(Agent offerer, Agent receiver, Integer offererValue,
				Integer receiverValue, Double offererReduction) {
			this.offerer = offerer;
			this.receiver = receiver;
			this.offererValue = offererValue;
			this.receiverValue = receiverValue;
			this.offererReduction = offererReduction;
		}
	}

	private static double computeGlobalGain(double localGain, double remoteGain) {
		return localGain + remoteGain - Math.abs(localGain - remoteGain);
		// return (localGain + remoteGain) / 2;
	}

}
