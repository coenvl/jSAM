/**
 * File GreedyCooperativeSolver.java
 *
 * This file is part of the jSAM project 2014.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import nl.coenvl.sam.agents.Agent;
import nl.coenvl.sam.agents.LocalCommunicatingAgent;
import nl.coenvl.sam.costfunctions.CostFunction;
import nl.coenvl.sam.exceptions.InvalidValueException;
import nl.coenvl.sam.messages.HashMessage;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.problemcontexts.LocalProblemContext;
import nl.coenvl.sam.variables.IntegerVariable;

/**
 * GreedyCooperativeSolver
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 11 apr. 2014
 * 
 */
@SuppressWarnings("unchecked")
public class UniqueFirstCooperativeSolver implements Solver {

	private static enum State {
		ACTIVE, DONE, HOLD, IDLE
	}

	private static final String ASSIGN_VAR = "UniqueFirstCooperativeSolver:PickAVar";

	private static final String COST_MSG = "UniqueFirstCooperativeSolver:CostOfAssignments";
	private static final String CURRENT_STATE = "UniqueFirstCooperativeSolver:StateChanged";
	private static final String INQUIRE_MSG = "UniqueFirstCooperativeSolver:InquireAssignment";
	private volatile LocalProblemContext<Integer> context;

	private final CostFunction costfun;

	private volatile State currentState;

	private final IntegerVariable myVariable;

	private final Map<Agent, State> neighborStates;

	private final LocalCommunicatingAgent parent;

	private volatile List<HashMap<Integer, Double>> receivedMaps;

	private int uniquenessBound;

	public UniqueFirstCooperativeSolver(LocalCommunicatingAgent parent,
			CostFunction costfun) {
		this.parent = parent;
		this.costfun = costfun;
		// Assume we DO always have an integer variable
		this.myVariable = (IntegerVariable) parent.getVariable();
		this.neighborStates = new HashMap<Agent, State>();
	}

	/**
	 * Send an activation message (ASSIGN_VAR) to the non-active neighbors
	 */
	private void activateNeighbors() {
		HashMessage nextMessage = new HashMessage(ASSIGN_VAR);
		nextMessage.addContent("cpa", context.getAssignment().clone());
		nextMessage.addContent("source", this.parent);

		// Iterate over the set until we found a non-activated neighbor
		for (Agent neighbor : this.parent.getNeighborhood())
			// neighbor.push(nextMessage);
			if (!this.neighborStates.containsKey(neighbor)
					|| (this.neighborStates.get(neighbor) != State.ACTIVE && this.neighborStates
							.get(neighbor) != State.DONE)) {
				neighbor.push(nextMessage);
				return;
			}
	}

	/**
	 * Subroutine to count the number of neighbors that are in an ACTIVE state
	 * 
	 * @return the number of neighbors that are active
	 */
	private int getActiveNeighbors() {
		int activeNeighbors = 0;

		for (State neighborState : this.neighborStates.values())
			if (neighborState == State.ACTIVE)
				activeNeighbors++;

		return activeNeighbors;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.Solver#init()
	 */
	@Override
	public void init() {
		this.currentState = State.IDLE;
		this.neighborStates.clear();
		this.context = new LocalProblemContext<Integer>(this.parent);
		this.uniquenessBound = 1;
	}

	/**
	 * This function is called when all cost messages have arrived and I can now
	 * make a decision on how to assign the variable
	 * 
	 * @throws InvalidValueException
	 */
	private void pickValue() throws InvalidValueException {
		// Gather all of the results and get the best assignment for me
		double bestCost = Double.MAX_VALUE;
		Vector<Integer> bestAssignment = new Vector<Integer>();

		// Create a problemContext to play around with to see which assignment
		// is optimal
		LocalProblemContext<Integer> pa = new LocalProblemContext<Integer>(
				this.parent);
		pa.setAssignment(this.context.getAssignment());

		for (Integer iterAssignment : this.myVariable) {
			// Sum the total cost of this partial assignment that all neighbors
			// will incur
			double totalCost = 0;
			for (HashMap<Integer, Double> neighborMap : this.receivedMaps) {
				Double neighborCost = neighborMap.get(iterAssignment);
				totalCost += neighborCost;
			}

			// Add my OWN cost
			pa.setValue(iterAssignment);
			totalCost += costfun.evaluate(pa);

			// Store the best value, and maintain it's uniqueness
			if (totalCost < bestCost) {
				bestCost = totalCost;
				bestAssignment.clear();
			}

			if (totalCost <= bestCost) {
				bestAssignment.add(iterAssignment);
			}
		}

		// See if we are ready to set this value
		if (bestAssignment.size() > this.uniquenessBound) {
			if (this.getActiveNeighbors() > 0) {
				updateLocalState(State.HOLD);
				return;
			}
		}

		Integer assign;
		if (bestAssignment.size() > 1) {
			int i = (new Random()).nextInt(bestAssignment.size());
			assign = bestAssignment.elementAt(i);
		} else {
			assign = bestAssignment.elementAt(0);
		}

		// Set the value
		this.myVariable.setValue(assign);
		context.setValue(assign);

		updateLocalState(State.DONE);
		activateNeighbors();
	}

	/**
	 * The neighbors will respond with cost Messages, in this function such a
	 * message is handled. If the cost message is the last one to be received,
	 * continue picking one assignment by calling the {@link #pickValue()}
	 * function
	 * 
	 * @param m
	 */
	private synchronized void processCostMessage(Message m) {
		HashMap<Integer, Double> costMap = (HashMap<Integer, Double>) m
				.getContent("costMap");
		this.receivedMaps.add(costMap);

		if (this.receivedMaps.size() < this.parent.getNeighborhood().size()) {
			return;
		}
		
		try {
			pickValue();
		} catch (InvalidValueException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.solvers.Solver#push(nl.coenvl.sam.messages.Message)
	 */
	@Override
	public synchronized void push(Message m) {

		if (m.hasContent("cpa")) {
			if (context == null)
				context = new LocalProblemContext<Integer>(this.parent);

			context.setAssignment((HashMap<Agent, Integer>) m.getContent("cpa"));
		}

		if (m.getType().equals(UniqueFirstCooperativeSolver.ASSIGN_VAR)) {
			// Check if we are not currently busy or on hold, start
			if (this.currentState == State.IDLE
					|| this.currentState == State.HOLD) {
				this.updateLocalState(State.ACTIVE);
				this.sendInquireMsgs();
			}
		} else if (m.getType().equals(UniqueFirstCooperativeSolver.INQUIRE_MSG)) {
			this.respond(m);
		} else if (m.getType().equals(UniqueFirstCooperativeSolver.COST_MSG)) {
			if (this.currentState != State.ACTIVE)
				return;

			this.processCostMessage(m);
		} else if (m.getType().equals(
				UniqueFirstCooperativeSolver.CURRENT_STATE)) {
			this.updateRemoteState(m);
		} else {
			System.err.println(this.getClass().getName()
					+ ": Unexpected message of type " + m.getType());
			return;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.solvers.Solver#reset()
	 */
	@Override
	public void reset() {
		this.myVariable.clear();
		this.neighborStates.clear();
		this.receivedMaps = null;
		this.context = null;
		this.currentState = State.IDLE;
	}

	/**
	 * Iterate over all possible assignments for the source
	 * 
	 * @param m
	 */
	private synchronized void respond(Message m) {
		HashMap<Integer, Double> costMap = new HashMap<Integer, Double>();

		Agent source = (Agent) m.getContent("source");
		LocalProblemContext<Integer> pa = new LocalProblemContext<Integer>(
				source);
		pa.setAssignment(context.getAssignment());

		// Build the cost map making the strong assumption that I have the same
		// type of variable as the source
		// IntegerVariableIterator iter = this.myVariable.iterator();
		// while (iter.hasNext()) {
		// Integer iterAssignment = iter.next();
		for (Integer iterAssignment : myVariable) {
			pa.setValue(iterAssignment);

			Double iterCost = Double.MAX_VALUE;
			if (myVariable.isSet()) {
				double localCost = this.costfun.evaluate(pa);

				if (localCost < iterCost)
					iterCost = localCost;
			} else {
				// Now the internal loop to optimize MY value
				LocalProblemContext<Integer> spa = new LocalProblemContext<Integer>(
						this.parent);
				spa.setAssignment(pa.getAssignment());
				// IntegerVariableIterator siter = this.myVariable.iterator();

				// while (siter.hasNext()) {
				// Integer siterAssignment = siter.next();
				for (Integer siterAssignment : myVariable) {
					spa.setValue(siterAssignment);
					double siterCost = this.costfun.evaluate(spa);

					if (siterCost < iterCost)
						iterCost = siterCost;
				}
			}

			costMap.put(iterAssignment, iterCost);
		}

		// Respond to source
		Message response = new HashMessage(
				UniqueFirstCooperativeSolver.COST_MSG);
		response.addContent("costMap", costMap);
		response.addContent("cpa", this.context.getAssignment().clone());
		// response.addContent("target", source);
		source.push(response);
	}

	private synchronized void sendInquireMsgs() {
		// Create a map for storing incoming costmap messages
		this.receivedMaps = new ArrayList<HashMap<Integer, Double>>();

		Message m = new HashMessage(INQUIRE_MSG);
		m.addContent("cpa", this.context.getAssignment().clone());
		m.addContent("source", this.parent);

		for (Agent neighbor : this.parent.getNeighborhood())
			neighbor.push(m);
	}

	/**
	 * Update my state and inform my neighbors
	 * 
	 * @param newState
	 */
	private void updateLocalState(State newState) {
		this.currentState = newState;

		Message updateMessage = new HashMessage(CURRENT_STATE);
		updateMessage.addContent("source", this.parent);
		updateMessage.addContent("state", newState);
		updateMessage.addContent("cpa", this.context.getAssignment().clone());

		for (Agent i : this.parent.getNeighborhood())
			i.push(updateMessage);
	}

	private void updateRemoteState(Message m) {
		Agent source = (Agent) m.getContent("source");
		State newState = (State) m.getContent("state");
		this.neighborStates.put(source, newState);

		if (newState == State.HOLD && this.currentState == State.HOLD
				&& getActiveNeighbors() < 1) {
			this.receivedMaps.clear();
			this.uniquenessBound++;
			// System.err.println("Increasing the uniqueness bound!");
			this.updateLocalState(State.ACTIVE); // break the deadlock
			this.sendInquireMsgs();
		} else if (newState == State.DONE && this.currentState == State.HOLD) {
			this.receivedMaps.clear();
			this.updateLocalState(State.ACTIVE);
			this.sendInquireMsgs();
		} else if (newState == State.DONE && this.currentState == State.DONE) {
			activateNeighbors();
		}

	}

}
