/**
 * File GreedySolver.java
 *
 * This file is part of the jSAM project 2014.
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

import nl.coenvl.sam.agents.Agent;
import nl.coenvl.sam.messages.HashMessage;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.variables.AssignmentMap;
import nl.coenvl.sam.variables.DiscreteVariable;
import nl.coenvl.sam.variables.RandomAccessVector;

/**
 * GreedySolver
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 11 apr. 2014
 *
 */
public class GreedySolver<V> extends AbstractSolver<DiscreteVariable<V>, V> implements Solver {

	private static final String ASSIGN_VAR = "GreedySolver:AssignVariable";
	private AssignmentMap<V> context;

	public GreedySolver(Agent<DiscreteVariable<V>, V> agent) {
		super(agent);
		this.context = new AssignmentMap<>();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nl.coenvl.sam.Solver#init()
	 */
	@Override
	public void init() {
		// Nothing to do here
	}

	/**
	 * @param m
	 */
	private synchronized void pickVar(AssignmentMap<V> pa) {
		this.context.putAll(pa);
		double bestCost = Double.MAX_VALUE;

		RandomAccessVector<V> bestAssignment = new RandomAccessVector<>();
		for (V iterAssignment : this.myVariable) {
			this.context.setAssignment(this.myVariable, iterAssignment);

			double localCost = this.parent.getLocalCostIf(this.context);

			if (localCost < bestCost) {
				bestCost = localCost;
				bestAssignment.clear();
			}

			if (localCost <= bestCost) {
				bestAssignment.add(iterAssignment);
			}
		}

		V assign = bestAssignment.randomElement();

		this.myVariable.setValue(assign);
		this.context.setAssignment(this.myVariable, assign);

		HashMessage nextMessage = new HashMessage(this.myVariable.getID(), GreedySolver.ASSIGN_VAR);
		nextMessage.put("cpa", this.context);

		// Maybe it would be better if I would send the update message 1 by 1.
		this.sendToNeighbors(nextMessage);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.anon.cocoa.solvers.Solver#push(org.anon.cocoa.messages.Message)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public synchronized void push(Message m) {
		if (m.getType().equals(GreedySolver.ASSIGN_VAR)) {
			if (this.myVariable.isSet()) {
				return;
			}

			AssignmentMap<V> pa;
			if (m.containsKey("cpa")) {
				pa = (AssignmentMap<V>) m.getMap("cpa");
			} else {
				pa = new AssignmentMap<>();
			}

			this.pickVar(pa);
		} else {
			// System.err.println("Unexpected message of type " + m.getType());
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
		super.reset();
		this.context.clear();
	}

}
