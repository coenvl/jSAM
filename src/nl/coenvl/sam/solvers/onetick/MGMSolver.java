/**
 * File MGMSolver.java
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
package nl.coenvl.sam.solvers.onetick;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import nl.coenvl.sam.MailMan;
import nl.coenvl.sam.agents.Agent;
import nl.coenvl.sam.messages.HashMessage;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.solvers.AbstractSolver;
import nl.coenvl.sam.solvers.IterativeSolver;
import nl.coenvl.sam.variables.AssignmentMap;
import nl.coenvl.sam.variables.DiscreteVariable;

/**
 * MGMSolver
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 17 okt. 2014
 *
 */
@Deprecated
public class MGMSolver<V> extends AbstractSolver<DiscreteVariable<V>, V> implements IterativeSolver {

	// private static final double EQUAL_UPDATE_PROBABILITY = 0.5;
	private static final String UPDATE_VALUE = "MGM:UpdateValue";
	private static final String LOCAL_REDUCTION = "MGM:BestLocalReduction";

	private AssignmentMap<V> myProblemContext;

	private AssignmentMap<Double> neighborReduction;
	private Set<UUID> receivedValues;
	private double bestLocalReduction;
	private V bestLocalAssignment;

	public MGMSolver(Agent<DiscreteVariable<V>, V> agent) {
		super(agent);
	}

	@Override
	public void init() {
		this.myProblemContext = new AssignmentMap<>();
		this.receivedValues = new HashSet<>();
		this.neighborReduction = new AssignmentMap<>();
		this.myVariable.setValue(this.myVariable.getRandomValue());
	}

	@Override
	public synchronized void push(Message m) {
		final UUID source = m.getUUID("source");

		if (m.getType().equals(MGMSolver.UPDATE_VALUE)) {
			@SuppressWarnings("unchecked")
			final V value = (V) m.getInteger("value");

			this.myProblemContext.put(source, value);
			this.receivedValues.add(source);

			if (this.receivedValues.size() == this.parent.getConstraintIds().size()) {
				// Clear ASAP to stay in line with asynchronous running neighbors
				this.receivedValues.clear();
				this.computeLocalReductions();
			}

		} else if (m.getType().equals(MGMSolver.LOCAL_REDUCTION)) {
			final double reduction = m.getDouble("LR");

			this.neighborReduction.put(source, reduction);

			if (this.neighborReduction.size() == this.parent.getConstraintIds().size()) {
				this.pickValue();
			}
		}
	}

	@Override
	public synchronized void tick() {
		final Message updateMsg = new HashMessage(MGMSolver.UPDATE_VALUE);

		updateMsg.put("value", this.myVariable.getValue());
		updateMsg.put("source", this.myVariable.getID());

		for (final UUID id : this.parent.getConstraintIds()) {
			MailMan.sendMessage(id, updateMsg);
		}
	}

	/**
	 *
	 */
	private void computeLocalReductions() {
		// By now the problem context should be updated with all the neighbors' values
		this.myProblemContext.setAssignment(this.myVariable, this.myVariable.getValue());

		final double before = this.parent.getLocalCostIf(this.myProblemContext);
		double bestCost = before; // Double.MAX_VALUE; //
		V bestAssignment = null;

		final AssignmentMap<V> temp = this.myProblemContext.clone();

		for (final V assignment : this.myVariable) {
			temp.setAssignment(this.myVariable, assignment);

			final double localCost = this.parent.getLocalCostIf(this.myProblemContext);

			if (localCost < bestCost) {
				bestCost = localCost;
				bestAssignment = assignment;
			}
		}

		this.bestLocalReduction = before - bestCost;
		this.bestLocalAssignment = bestAssignment;

		final Message lrMsg = new HashMessage(MGMSolver.LOCAL_REDUCTION);

		lrMsg.put("source", this.myVariable.getID());
		lrMsg.put("LR", this.bestLocalReduction);

		for (final UUID id : this.parent.getConstraintIds()) {
			MailMan.sendMessage(id, lrMsg);
		}
	}

	/**
	 *
	 */
	private void pickValue() {
		Double bestNeighborReduction = Double.MIN_VALUE;
		UUID bestNeighbor = null;
		for (UUID n : this.parent.getConstraintIds()) {
			if (this.neighborReduction.get(n) > bestNeighborReduction) {
				bestNeighborReduction = this.neighborReduction.get(n);
				bestNeighbor = n;
			}
		}

		if (this.bestLocalReduction > bestNeighborReduction) {
			this.myVariable.setValue(this.bestLocalAssignment);
		}
		if (this.bestLocalReduction == bestNeighborReduction && this.myVariable.getID().compareTo(bestNeighbor) < 0) {
			this.myVariable.setValue(this.bestLocalAssignment);
		}

		this.neighborReduction.clear();
	}

	@Override
	public void reset() {
		this.myVariable.clear();
	}

}
