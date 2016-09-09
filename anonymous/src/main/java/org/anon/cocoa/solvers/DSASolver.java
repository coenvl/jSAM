/**
 * File DSASolver.java
 *
 * This file is part of the jCoCoA project.
 *
 * Copyright 2014 Anomymous
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.anon.cocoa.solvers;

import java.util.UUID;

import org.anon.cocoa.agents.Agent;
import org.anon.cocoa.messages.HashMessage;
import org.anon.cocoa.messages.Message;
import org.anon.cocoa.variables.AssignmentMap;
import org.anon.cocoa.variables.DiscreteVariable;
import org.anon.cocoa.variables.RandomAccessVector;

/**
 * DSASolver
 *
 * @author Anomymous
 * @version 0.1
 * @since 11 dec. 2014
 *
 */
public class DSASolver<V> extends AbstractSolver<DiscreteVariable<V>, V> implements IterativeSolver {

	public static final double CHANGE_TO_EQUAL_PROB = 0.5;
	public static final double CHANGE_TO_IMPROVE_PROB = 0.5;
	public static final String UPDATE_VALUE = "DSASolver:Value";
	public static final String KEY_VARVALUE = "value";

	private AssignmentMap<V> context;

	/**
	 * @param dsaAgent
	 * @param costfun
	 */
	public DSASolver(Agent<DiscreteVariable<V>, V> agent) {
		super(agent);
		this.context = new AssignmentMap<>();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.anon.cocoa.solvers.Solver#init()
	 */
	@Override
	public synchronized void init() {
		this.updateMyValue(this.myVariable.getRandomValue());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.anon.cocoa.solvers.Solver#push(org.anon.cocoa.messages.Message)
	 */
	@Override
	public synchronized void push(Message m) {
		if (m.getType().equals(DSASolver.UPDATE_VALUE)) {
			UUID varId = m.getSource();

			@SuppressWarnings("unchecked")
			V newValue = (V) m.getInteger(DSASolver.KEY_VARVALUE);

			this.context.put(varId, newValue);
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
		this.context.clear();
	}

	/**
	 *
	 */
	@Override
	public synchronized void tick() {
		double bestCost = Double.MAX_VALUE;

		RandomAccessVector<V> bestAssignment = new RandomAccessVector<>();
		this.context.setAssignment(this.myVariable, this.myVariable.getValue());
		double oldCost = this.parent.getLocalCostIf(this.context);

		for (V value : this.myVariable) {
			this.context.setAssignment(this.myVariable, value);

			double localCost = this.parent.getLocalCostIf(this.context);

			if (localCost < bestCost) {
				bestCost = localCost;
				bestAssignment.clear();
			}

			if (localCost <= bestCost) {
				bestAssignment.add(value);
			}
		}

		if (bestCost > oldCost) {
			return;
		}

		if ((bestCost == oldCost) && (Math.random() > DSASolver.CHANGE_TO_EQUAL_PROB)) {
			return;
		}

		if ((bestCost < oldCost) && (Math.random() > DSASolver.CHANGE_TO_IMPROVE_PROB)) {
			return;
		}

		// Chose any of the "best" assignments
		V assign = bestAssignment.randomElement();

		if (assign != this.myVariable.getValue()) {
			this.updateMyValue(assign);
		}
	}

	/**
	 * @param assign
	 */
	private void updateMyValue(V assign) {
		this.myVariable.setValue(assign);

		HashMessage nextMessage = new HashMessage(this.myVariable.getID(), DSASolver.UPDATE_VALUE);
		nextMessage.put(DSASolver.KEY_VARVALUE, assign);

		this.sendToNeighbors(nextMessage);
	}

}
