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
 * TickCFLSolver
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 17 okt. 2014
 *
 */
public class SCA2Solver implements IterativeSolver {

	/**
	 * State
	 *
	 * @author leeuwencjv
	 * @version 0.1
	 * @since 10 apr. 2015
	 *
	 */
	public enum State {
		Value, Offer, AcceptReject
	}

	private static final double OFFER_PROBABILITY = 0.5; // (Q in paper)

	private static final double ACTIVATION_PROBABILITY = 0.5; // (P in paper)

	private static final String UPDATE_VALUE = "SCA2:UpdateValue";

	private static final String OFFER = "SCA2:MoveOffer";

	private static final String ACCEPT = "SCA2:AcceptOffer";

	private CostFunction myCostFunction;

	private LocalProblemContext<Integer> myProblemContext;

	private IntegerVariable myVariable;

	private LocalCommunicatingAgent parent;

	private State algoState;

	private List<Offer> receivedOffers;

	private boolean isOfferer;

	private boolean committed;

	public SCA2Solver(LocalCommunicatingAgent agent, CostFunction costfunction) {
		this.parent = agent;
		this.myCostFunction = costfunction;
		this.myVariable = (IntegerVariable) this.parent.getVariable();
	}

	@Override
	public void init() {
		this.algoState = State.Value;
		this.myProblemContext = new LocalProblemContext<Integer>(this.parent);
		this.receivedOffers = new LinkedList<Offer>();

		// So that it will not try to pick a local best at first
		this.committed = true;

		try {
			this.myVariable.setValue(this.myVariable.getRandomValue());
		} catch (InvalidValueException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void push(Message m) {
		Agent source = (Agent) m.getContent("source");

		if (m.getType().equals(SCA2Solver.UPDATE_VALUE)) {
			Integer value = (Integer) m.getContent("value");

			this.myProblemContext.setValue(source, value);
		} else if (m.getType().equals(SCA2Solver.OFFER)) {

			// Any OFFER message should contain this...
			if (m.hasContent("offers")) {
				@SuppressWarnings("unchecked")
				List<Offer> offerList = (List<Offer>) m.getContent("offers");
				this.receivedOffers.addAll(offerList);
			}

		} else if (m.getType().equals(SCA2Solver.ACCEPT)) {

			// Any ACCEPT message should contain this...
			if (m.hasContent("offer")) {
				Offer acceptedOffer = (Offer) m.getContent("offer");
				this.pickValue(acceptedOffer);
			}

		} else {
			throw new RuntimeException("Unexpected message: " + m.getType());
		}
	}

	@Override
	public void tick() {
		switch (this.algoState) {
		case Offer:
			sendOffer();
			this.algoState = State.AcceptReject;
			break;

		case AcceptReject:
			this.sendAccept();
			this.algoState = State.Value;
			break;

		default:
		case Value:
			if (!this.committed)
				pickBestLocalValue();

			sendValue();
			this.committed = false;
			this.algoState = State.Offer;
			break;
		}

	}

	private void sendValue() {
		try {
			Message updateMsg = new HashMessage(SCA2Solver.UPDATE_VALUE);

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
	 * 
	 */
	@SuppressWarnings("null")
	private void sendOffer() {
		// First determine wether we will offer or receive
		if (Math.random() > SCA2Solver.OFFER_PROBABILITY) {

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
						offerList
								.add(new Offer(this.parent, i, j, before - val));
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

		// Only if this Agent did not offer to anyone else...
		if (!this.isOfferer) {

			LocalProblemContext<Integer> temp = new LocalProblemContext<Integer>(
					this.parent);
			@SuppressWarnings("unchecked")
			HashMap<Agent, Integer> cpa = (HashMap<Agent, Integer>) this.myProblemContext
					.getAssignment().clone();

			double bestGain = Double.MIN_VALUE;
			temp.setAssignment(cpa);
			temp.setValue(this.myVariable.getValue());
			double before = this.myCostFunction.evaluate(temp);
			Offer bestOffer = null;

			for (Offer suggestedOffer : this.receivedOffers) {
				temp.setAssignment(cpa);
				temp.setValue(suggestedOffer.offerer,
						suggestedOffer.offererValue);
				temp.setValue(suggestedOffer.receiverValue);
				double val = this.myCostFunction.evaluate(temp);
				double localReduction = before - val;
				double globalReduction = suggestedOffer.offererReduction
						+ localReduction;
				if (globalReduction > 0 && globalReduction > bestGain) {
					bestGain = globalReduction;
					bestOffer = suggestedOffer;
				}
			}

			// Send accept if there is a global reduction
			if (bestOffer != null) {
				Message accept = new HashMessage(SCA2Solver.ACCEPT);
				accept.addContent("source", this.parent);
				accept.addContent("offer", bestOffer);

//				System.out.println(this.parent.getName()
//						+ " accepts offer from " + bestOffer.offerer.getName());
				bestOffer.offerer.push(accept);

				// Set the value now
				this.committed = true;
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

	private void pickValue(Offer acceptedOffer) {
		this.committed = true;
		try {
			this.myVariable.setValue(acceptedOffer.offererValue);
		} catch (InvalidValueException e) {
			e.printStackTrace();
		}
	}

	private void pickBestLocalValue() {
		if (Math.random() > SCA2Solver.ACTIVATION_PROBABILITY) {
			double bestCost = Double.MAX_VALUE;
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

			try {
				this.myVariable.setValue(bestAssignment);
			} catch (InvalidValueException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void reset() {
		this.myVariable.clear();
	}

	private class Offer {
		public Agent offerer;

		public Integer offererValue;

		public Integer receiverValue;

		public Double offererReduction;

		// public Double reiceverReduction;

		public Offer(Agent offerer, Integer offererValue,
				Integer receiverValue, Double offererReduction) {
			this.offerer = offerer;
			this.offererValue = offererValue;
			this.receiverValue = receiverValue;
			this.offererReduction = offererReduction;
		}
	}

}
