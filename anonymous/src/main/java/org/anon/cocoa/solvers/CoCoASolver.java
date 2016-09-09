/**
 * File UniqueFirstCooperativeSolver.java
 *
 * This file is part of the jCoCoA project 2014.
 *
 * Copyright 2014 Anomymous
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
package org.anon.cocoa.solvers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.anon.cocoa.MailMan;
import org.anon.cocoa.agents.Agent;
import org.anon.cocoa.messages.HashMessage;
import org.anon.cocoa.messages.Message;
import org.anon.cocoa.variables.AssignmentMap;
import org.anon.cocoa.variables.CostMap;
import org.anon.cocoa.variables.DiscreteVariable;
import org.anon.cocoa.variables.RandomAccessVector;

/**
 * UniqueFirstCooperativeSolver
 *
 * @author Anomymous
 * @version 0.1
 * @since 11 apr. 2014
 *
 */
public class CoCoASolver<V> extends AbstractSolver<DiscreteVariable<V>, V> implements Solver {

	private static enum State {
		ACTIVE,
		DONE,
		HOLD,
		IDLE
	}

	public static final String ROOTNAME_PROPERTY = "isRoot";

	private static final String ASSIGN_VAR = "CoCoASolver:PickAVar";
	private static final String COST_MSG = "CoCoASolver:CostOfAssignments";
	private static final String CURRENT_STATE = "CoCoASolver:StateChanged";
	private static final String INQUIRE_MSG = "CoCoASolver:InquireAssignment";

	private final AssignmentMap<State> neighborStates;
	private final AssignmentMap<V> context;

	private volatile State currentState;
	private volatile List<CostMap<V>> receivedMaps;

	private int uniquenessBound;

	public CoCoASolver(Agent<DiscreteVariable<V>, V> agent) {
		super(agent);
		this.neighborStates = new AssignmentMap<>();
		this.context = new AssignmentMap<>();
		this.currentState = State.IDLE;
		this.uniquenessBound = 1;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.anon.cocoa.Solver#init()
	 */
	@Override
	public void init() {
		if (this.isRoot()) {
			this.push(new HashMessage(null, CoCoASolver.ASSIGN_VAR));
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.anon.cocoa.solvers.Solver#push(org.anon.cocoa.messages.Message)
	 */
	@Override
	public synchronized void push(Message m) {

		if (m.containsKey("cpa")) {
			@SuppressWarnings("unchecked")
			AssignmentMap<V> cpa = (AssignmentMap<V>) m.getMap("cpa");
			this.context.putAll(cpa);
		}

		if (m.getType().equals(CoCoASolver.ASSIGN_VAR)) {
			// Check if we are not currently busy or on hold, start
			if ((this.currentState == State.IDLE) || (this.currentState == State.HOLD)) {
				this.updateLocalState(State.ACTIVE);
				this.sendInquireMsgs();
			}
		} else if (m.getType().equals(CoCoASolver.INQUIRE_MSG)) {
			this.respond(m);
		} else if (m.getType().equals(CoCoASolver.COST_MSG)) {
			if (this.currentState != State.ACTIVE) {
				System.err.println("This never ought to happen!");
				return;
			}

			this.processCostMessage(m);
		} else if (m.getType().equals(CoCoASolver.CURRENT_STATE)) {
			this.updateRemoteState(m);
		} else {
			// System.err.println(this.getClass().getName() + ": Unexpected message of type " + m.getType());
			return;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.anon.cocoa.solvers.Solver#reset()
	 */
	@Override
	public void reset() {
		super.reset();
		this.neighborStates.clear();
		this.context.clear();

		this.receivedMaps = null;
		this.currentState = State.IDLE;
	}

	private synchronized void sendInquireMsgs() {
		// Create a map for storing incoming costmap messages
		this.receivedMaps = new ArrayList<>();

		Message m = new HashMessage(this.myVariable.getID(), CoCoASolver.INQUIRE_MSG);
		m.put("cpa", this.context);

		this.sendToNeighbors(m);
	}

	/**
	 * Iterate over all possible assignments for the source
	 *
	 * @param m
	 */
	private synchronized void respond(Message m) {
		CostMap<V> costMap = new CostMap<>();

		UUID source = m.getSource();
		AssignmentMap<V> pa = this.context.clone(); // Should by now already include the CPA of the source

		// Build the cost map making the strong assumption that I have the same
		// type of variable as the source
		for (V iterAssignment : this.myVariable) {
			pa.put(source, iterAssignment);

			Double iterCost = Double.MAX_VALUE;
			if (this.myVariable.isSet()) {
				iterCost = this.parent.getLocalCostIf(pa);
			} else {
				// Now the internal loop to optimize MY value
				AssignmentMap<V> spa = pa.clone();

				for (V siterAssignment : this.myVariable) {
					spa.setAssignment(this.myVariable, siterAssignment);
					double siterCost = this.parent.getLocalCostIf(spa);

					if (siterCost < iterCost) {
						iterCost = siterCost;
					}
				}
			}

			costMap.put(iterAssignment, iterCost);
		}

		// Respond to source
		Message response = new HashMessage(this.myVariable.getID(), CoCoASolver.COST_MSG);
		response.put("costMap", costMap);
		response.put("cpa", this.context);

		MailMan.sendMessage(source, response);
	}

	/**
	 * The neighbors will respond with cost Messages, in this function such a message is handled. If the cost message is
	 * the last one to be received, continue picking one assignment by calling the {@link #pickValue()} function
	 *
	 * @param m
	 */
	private synchronized void processCostMessage(Message m) {
		@SuppressWarnings("unchecked")
		CostMap<V> costMap = (CostMap<V>) m.getMap("costMap");
		this.receivedMaps.add(costMap);

		if (this.receivedMaps.size() >= this.numNeighbors()) {
			this.pickValue();
		}
	}

	/**
	 * This function is called when all cost messages have arrived and I can now make a decision on how to assign the
	 * variable
	 */
	private void pickValue() {
		// Gather all of the results and get the best assignment for me
		double bestCost = Double.MAX_VALUE;
		RandomAccessVector<V> bestAssignment = new RandomAccessVector<>();

		// Create a problemContext to play around with to see which assignment is optimal
		AssignmentMap<V> pa = this.context.clone();
		for (V iterAssignment : this.myVariable) {
			// Sum the total cost of this partial assignment that all neighbors will incur
			double totalCost = 0;
			for (CostMap<V> neighborMap : this.receivedMaps) {
				Double neighborCost = neighborMap.get(iterAssignment);
				totalCost += neighborCost;
			}

			// Add my OWN cost
			pa.setAssignment(this.myVariable, iterAssignment);
			totalCost += this.parent.getLocalCostIf(pa);

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
				this.updateLocalState(State.HOLD);
				return;
			}
		}

		// Set the value
		V assign = bestAssignment.randomElement();
		this.myVariable.setValue(assign);
		this.context.setAssignment(this.myVariable, assign);

		this.updateLocalState(State.DONE);
		this.activateNeighbors();
	}

	/**
	 * Update my state and inform my neighbors
	 *
	 * @param newState
	 */
	private void updateLocalState(State newState) {
		this.currentState = newState;

		Message updateMessage = new HashMessage(this.myVariable.getID(), CoCoASolver.CURRENT_STATE);
		updateMessage.put("state", newState.name());
		updateMessage.put("cpa", this.context);

		this.sendToNeighbors(updateMessage);
	}

	private void updateRemoteState(Message m) {
		UUID source = m.getSource();
		State newState = State.valueOf(m.get("state"));
		this.neighborStates.put(source, newState);

		if ((newState == State.HOLD) && (this.currentState == State.HOLD) && (this.getActiveNeighbors() < 1)) {
			this.receivedMaps.clear();
			this.uniquenessBound++;
			// System.err.println("Increasing the uniqueness bound!");
			this.updateLocalState(State.ACTIVE); // break the deadlock
			this.sendInquireMsgs();
		} else if ((newState == State.DONE) && (this.currentState == State.HOLD)) {
			this.receivedMaps.clear();
			this.updateLocalState(State.ACTIVE);
			this.sendInquireMsgs();
		} else if ((newState == State.DONE) && (this.currentState == State.DONE)) {
			this.activateNeighbors();
		}

	}

	/**
	 * Send an activation message (ASSIGN_VAR) to the non-active neighbors
	 */
	private void activateNeighbors() {
		HashMessage nextMessage = new HashMessage(this.myVariable.getID(), CoCoASolver.ASSIGN_VAR);
		nextMessage.put("cpa", this.context);

		// Iterate over the set until we found a non-activated neighbor
		for (UUID neighborid : this.parent.getConstrainedVariableIds()) {
			// neighbor.push(nextMessage);
			if (!this.neighborStates.containsKey(neighborid) || ((this.neighborStates.get(neighborid) != State.ACTIVE)
					&& (this.neighborStates.get(neighborid) != State.DONE))) {
				MailMan.sendMessage(neighborid, nextMessage);
				return;
			}
		}
	}

	/**
	 * Subroutine to count the number of neighbors that are in an ACTIVE state
	 *
	 * @return the number of neighbors that are active
	 */
	private int getActiveNeighbors() {
		int activeNeighbors = 0;

		for (State neighborState : this.neighborStates.values()) {
			if (neighborState == State.ACTIVE) {
				activeNeighbors++;
			}
		}

		return activeNeighbors;
	}

	protected boolean isRoot() {
		return this.parent.has(CoCoSolver.ROOTNAME_PROPERTY) && (Boolean) this.parent.get(CoCoSolver.ROOTNAME_PROPERTY);
	}

}
