/**
 * File GreedyCooperativeSolver.java
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

import java.util.UUID;

import org.anon.cocoa.agents.Agent;
import org.anon.cocoa.messages.HashMessage;
import org.anon.cocoa.messages.Message;
import org.anon.cocoa.variables.AssignmentMap;
import org.anon.cocoa.variables.CostMap;
import org.anon.cocoa.variables.DiscreteVariable;
import org.anon.cocoa.variables.RandomAccessVector;

/**
 * GreedyCooperativeSolver
 *
 * @author Anomymous
 * @version 0.1
 * @since 11 apr. 2014
 *
 */
public class ReCoCoMGMSolver<V> extends ReCoCoSolver<V> {

	protected static final String REDUCTION_MSG = "ReCoCoMGMSolver:LocalReduction";

	private V bestLocalAssignment;
	private volatile double bestLocalReduction;
	private AssignmentMap<Double> neighborReductions;
	// private boolean doCoCo;

	public ReCoCoMGMSolver(Agent<DiscreteVariable<V>, V> parent) {
		super(parent);
		this.neighborReductions = new AssignmentMap<>();
	}

	@Override
	public synchronized void tick() {
		this.neighborReductions.clear();
		super.tick();
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

		if (m.getType().equals(CoCoSolver.ASSIGN_VAR)) {
			if (!this.started) {
				this.sendInquireMsgs();
			}
		} else if (m.getType().equals(CoCoSolver.INQUIRE_MSG)) {
			this.respond(m);
		} else if (m.getType().equals(CoCoSolver.COST_MSG)) {
			this.processCostMessage(m);
		} else if (m.getType().equals(ReCoCoMGMSolver.REDUCTION_MSG)) {
			this.processReductionMessage(m);
		} else {
			// System.err.println(this.getClass().getName() + ": Unexpected message of type " + m.getType());
			return;
		}
	}

	@Override
	protected void processCostMessage(Message m) {
		@SuppressWarnings("unchecked")
		CostMap<V> costMap = (CostMap<V>) m.getMap("costMap");
		this.receivedMaps.add(costMap);

		if (this.receivedMaps.size() >= this.numNeighbors()) {
			this.computeLocalGain();
		}
	}

	private void processReductionMessage(Message m) {
		this.neighborReductions.put(m.getSource(), m.getDouble("reduction"));

		// If also WE are done, we can pick a value
		if (this.started && (this.bestLocalAssignment != null)
				&& (this.neighborReductions.size() >= this.numNeighbors())) {
			this.assignValue();
		}
	}

	private void assignValue() {
		// If our variable wasn't set in the first place set it now...
		if (!this.myVariable.isSet()) {
			this.setVariable();
			return;
		}

		Double bestNeighborReduction = 0.0; // -Double.MAX_VALUE;
		UUID bestNeighbor = null;
		for (UUID id : this.parent.getConstrainedVariableIds()) {
			double nReduction = this.neighborReductions.get(id);
			if (nReduction > bestNeighborReduction) {
				bestNeighborReduction = this.neighborReductions.get(id);
				bestNeighbor = id;
			}
		}

		// If it is an improvement, AND better than any improvement in the area, do it
		if ((this.bestLocalReduction > bestNeighborReduction)
				|| ((bestNeighbor != null) && (this.bestLocalReduction == bestNeighborReduction)
						&& (this.myVariable.getID().compareTo(bestNeighbor) < 0))) {
			this.setVariable();
		}
	}

	/**
	 *
	 */
	private void setVariable() {
		this.myVariable.setValue(this.bestLocalAssignment);
		this.context.setAssignment(this.myVariable, this.bestLocalAssignment);
	}

	/**
	 *
	 */
	private void computeLocalGain() {
		// Compute cost before any changes
		double before = this.parent.getLocalCostIf(this.context);
		for (CostMap<V> neighborMap : this.receivedMaps) {
			if (this.myVariable.isSet() && neighborMap.containsKey(this.myVariable.getValue())) {
				before += neighborMap.get(this.myVariable.getValue());
			}
		}

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

		// Set the value
		this.bestLocalAssignment = bestAssignment.randomElement();
		this.bestLocalReduction = before - bestCost;
		assert (this.bestLocalAssignment != null);

		// If we are the last of the neighbors, we can immediately pick value
		if (this.neighborReductions.size() >= this.numNeighbors()) {
			this.assignValue();
		}

		// Send reduction message to neighbors
		if (!this.myVariable.isSet()) {
			this.setVariable();
		} else { // if (bestLocalReduction > 0)
			Message msg = new HashMessage(this.myVariable.getID(), ReCoCoMGMSolver.REDUCTION_MSG);
			msg.put("reduction", this.bestLocalReduction);
			this.sendToNeighbors(msg);
		}

		// THIS WAS THE ISSUE, SET VARIABLE FIRST, THEN ACTIVATE NEIGHBORS
		this.activateNeighbors();
	}
}
