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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
public class MGMSolver implements IterativeSolver {

	private static final double EQUAL_UPDATE_PROBABILITY = 0.5;
	private static final String UPDATE_VALUE = "MGM:UpdateValue";
	private static final String LOCAL_REDUCTION = "MGM:BestLocalReduction";

	private CostFunction myCostFunction;
	private LocalProblemContext<Integer> myProblemContext;
	private IntegerVariable myVariable;
	private LocalCommunicatingAgent parent;

	private Map<Agent, Double> neighborReduction;
	private Set<Agent> receivedValues;
	private double bestLocalReduction;
	private Integer bestLocalAssignment;

	public MGMSolver(LocalCommunicatingAgent agent, CostFunction costfunction) {
		this.parent = agent;
		this.myCostFunction = costfunction;
		this.myVariable = (IntegerVariable) this.parent.getVariable();
	}

	@Override
	public void init() {
		this.myProblemContext = new LocalProblemContext<Integer>(this.parent);
		this.receivedValues = new HashSet<Agent>();
		this.neighborReduction = new HashMap<Agent, Double>();

		try {
			this.myVariable.setValue(this.myVariable.getRandomValue());
		} catch (InvalidValueException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void push(Message m) {
		Agent source = (Agent) m.getContent("source");
		// System.out.println("Agent " + this.parent.getName() +
		// " received message type " + m.getType() + " from " +
		// source.getName());

		if (m.getType().equals(MGMSolver.UPDATE_VALUE)) {
			Integer value = (Integer) m.getContent("value");

			this.myProblemContext.setValue(source, value);
			this.receivedValues.add(source);

			if (this.receivedValues.size() == parent.getNeighborhood().size()) {
				// Clear ASAP to stay in line with asynchronous running
				// neighbors
				this.receivedValues.clear();
				this.computeLocalReductions();
			}

		} else if (m.getType().equals(MGMSolver.LOCAL_REDUCTION)) {
			final Double reduction = (Double) m.getContent("LR");

			this.neighborReduction.put(source, reduction);

			if (this.neighborReduction.size() == parent.getNeighborhood().size())
				this.pickValue();
		}
	}

	@Override
	public void tick() {
		try {
			Message updateMsg = new HashMessage(MGMSolver.UPDATE_VALUE);

			updateMsg.addContent("value", this.myVariable.getValue().intValue());
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
	private void computeLocalReductions() {
		// By now the problem context should be updated with all the neighbors'
		// values
		try {
			this.myProblemContext.setValue(this.myVariable.getValue());
		} catch (VariableNotSetException e) {
			e.printStackTrace();
		}

		double before = this.myCostFunction.evaluate(myProblemContext);
		double bestCost = before; // Double.MAX_VALUE; //
		Integer bestAssignment = null;

		for (Integer assignment : this.myVariable) {
			this.myProblemContext.setValue(assignment);

			double localCost = this.myCostFunction.evaluate(myProblemContext);

			if (localCost < bestCost) {
				bestCost = localCost;
				bestAssignment = assignment;
			}
		}

		this.bestLocalReduction = before - bestCost;
		this.bestLocalAssignment = bestAssignment;

		Message lrMsg = new HashMessage(MGMSolver.LOCAL_REDUCTION);

		lrMsg.addContent("source", this.parent);
		lrMsg.addContent("LR", this.bestLocalReduction);

		for (Agent n : this.parent.getNeighborhood())
			n.push(lrMsg);
	}

	/**
	 * 
	 */
	private void pickValue() {
		Double bestNeighborReduction = Double.MIN_VALUE;
		for (Agent n : this.parent.getNeighborhood())
			if (this.neighborReduction.get(n) > bestNeighborReduction)
				bestNeighborReduction = this.neighborReduction.get(n);

		try {
			if (this.bestLocalReduction > bestNeighborReduction)
				this.myVariable.setValue(bestLocalAssignment);
			if (this.bestLocalReduction == bestNeighborReduction && Math.random() > EQUAL_UPDATE_PROBABILITY)
				this.myVariable.setValue(bestLocalAssignment);
		} catch (InvalidValueException e) {
			e.printStackTrace();
		}

		this.neighborReduction.clear();
	}

	@Override
	public void reset() {
		this.myVariable.clear();
	}

}
