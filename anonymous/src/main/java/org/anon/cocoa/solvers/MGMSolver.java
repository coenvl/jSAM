/**
 * File MGMSolver.java
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
import org.anon.cocoa.variables.DiscreteVariable;

/**
 * MGMSolver
 *
 * @author Anomymous
 * @version 0.1
 * @since 17 okt. 2014
 *
 */
public class MGMSolver<V> extends AbstractSolver<DiscreteVariable<V>, V> implements IterativeSolver {

	private enum State {
		SENDVALUE,
		SENDGAIN,
		PICKVALUE
	}

	// private static final double EQUAL_UPDATE_PROBABILITY = 0.5;
	protected static final String UPDATE_VALUE = "MGM:UpdateValue";
	protected static final String LOCAL_REDUCTION = "MGM:BestLocalReduction";

	protected final AssignmentMap<V> myProblemContext;
	private final AssignmentMap<Double> neighborReduction;

	private State algoState;
	protected double bestLocalReduction;
	protected V bestLocalAssignment;

	public MGMSolver(Agent<DiscreteVariable<V>, V> agent) {
		super(agent);
		this.myProblemContext = new AssignmentMap<>();
		this.neighborReduction = new AssignmentMap<>();
		this.algoState = State.SENDVALUE;
	}

	@Override
	public void init() {
		this.myVariable.setValue(this.myVariable.getRandomValue());
	}

	@Override
	public synchronized void push(Message m) {
		final UUID source = m.getSource();

		if (m.getType().equals(MGMSolver.UPDATE_VALUE)) {
			@SuppressWarnings("unchecked")
			final V value = (V) m.getInteger("value");
			this.myProblemContext.put(source, value);
		} else if (m.getType().equals(MGMSolver.LOCAL_REDUCTION)) {
			this.neighborReduction.put(source, m.getDouble("LR"));
		}
	}

	@Override
	public synchronized void tick() {
		switch (this.algoState) {
		case SENDGAIN:
			this.sendGain();
			this.algoState = State.PICKVALUE;
			break;

		case PICKVALUE:
			this.pickValue();
			this.algoState = State.SENDVALUE;
			break;

		default:
		case SENDVALUE:
			this.sendValue();
			this.algoState = State.SENDGAIN;
			break;
		}
	}

	protected void sendValue() {
		final Message updateMsg = new HashMessage(this.myVariable.getID(), MGMSolver.UPDATE_VALUE);
		updateMsg.put("value", this.myVariable.getValue());

		this.sendToNeighbors(updateMsg);
	}

	/**
	 *
	 */
	private void sendGain() {
		// By now the problem context should be updated with all the neighbors' values
		this.myProblemContext.setAssignment(this.myVariable, this.myVariable.getValue());

		final double before = this.parent.getLocalCostIf(this.myProblemContext);
		double bestCost = before; // Double.MAX_VALUE; //
		V bestAssignment = null;

		final AssignmentMap<V> temp = this.myProblemContext.clone();

		for (final V assignment : this.myVariable) {
			temp.setAssignment(this.myVariable, assignment);

			final double localCost = this.parent.getLocalCostIf(temp);

			if (localCost < bestCost) {
				bestCost = localCost;
				bestAssignment = assignment;
			}
		}

		this.bestLocalReduction = before - bestCost;
		this.bestLocalAssignment = bestAssignment;

		final Message lrMsg = new HashMessage(this.myVariable.getID(), MGMSolver.LOCAL_REDUCTION);
		lrMsg.put("LR", this.bestLocalReduction);

		this.sendToNeighbors(lrMsg);
	}

	/**
	 *
	 */
	protected void pickValue() {
		Double bestNeighborReduction = Double.MIN_VALUE;
		UUID bestNeighbor = null;
		for (UUID id : this.parent.getConstrainedVariableIds()) {
			if (this.neighborReduction.get(id) > bestNeighborReduction) {
				bestNeighborReduction = this.neighborReduction.get(id);
				bestNeighbor = id;
			}
		}

		if (this.bestLocalReduction > bestNeighborReduction) {
			this.myVariable.setValue(this.bestLocalAssignment);
		}
		if ((this.bestLocalReduction == bestNeighborReduction)
				&& (this.myVariable.getID().compareTo(bestNeighbor) < 0)) {
			this.myVariable.setValue(this.bestLocalAssignment);
		}
	}

	@Override
	public void reset() {
		this.myVariable.clear();
		this.myProblemContext.clear();
		this.neighborReduction.clear();
		this.algoState = State.SENDVALUE;
	}

}
