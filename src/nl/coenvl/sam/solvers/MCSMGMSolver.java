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
public class MCSMGMSolver implements IterativeSolver {

	private static final String UPDATE_VALUE = "MCSMGM:UpdateValue";
	private static final String IMPACT_VALUE = "MCSMGM:ImpactValue";
	private static final String LOCAL_REDUCTION = "MCSMGM:BestLocalReduction";
	private static final double EQUAL_UPDATE_PROBABILITY = 0.5;

	private CostFunction myCostFunction;
	private LocalProblemContext<Integer> myProblemContext;
	private IntegerVariable myVariable;
	private LocalCommunicatingAgent parent;

	private HashMap<Agent, Integer> neighborValues;
	private HashMap<Agent, Double> localCosts;
	private HashMap<Agent, Double> updateImpacts;
	private HashMap<Integer, HashMap<Agent, Double>> addedCosts;
	private HashMap<Agent, Double> lastReduction;
	private double bestLocalReduction;
	private Integer bestLocalAssignment;

	public MCSMGMSolver(LocalCommunicatingAgent agent, CostFunction costfunction) {
		this.parent = agent;
		this.myCostFunction = costfunction;
		this.myVariable = (IntegerVariable) this.parent.getVariable();
	}

	@Override
	public void init() {
		this.myProblemContext = new LocalProblemContext<Integer>(this.parent);
		this.neighborValues = new HashMap<Agent, Integer>();

		this.localCosts = new HashMap<Agent, Double>();
		this.updateImpacts = new HashMap<Agent, Double>();
		this.lastReduction = new HashMap<Agent, Double>();

		this.addedCosts = new HashMap<Integer, HashMap<Agent, Double>>();
		for (Integer i : this.myVariable)
			this.addedCosts.put(i, new HashMap<Agent, Double>());

		try {
			this.myVariable.setValue(this.myVariable.getRandomValue());
			this.myProblemContext.setValue(this.myVariable.getValue());
		} catch (InvalidValueException e) {
			throw new RuntimeException("Unexpected exception at this point", e);
		} catch (VariableNotSetException e) {
			throw new RuntimeException("Unexpected exception at this point", e);
		}
	}

	@Override
	public void push(final Message m) {
		final Agent source = (Agent) m.getContent("source");

		if (m.getType().equals(MCSMGMSolver.UPDATE_VALUE)) {
			final Integer value = (Integer) m.getContent("value");
			this.neighborValues.put(source, value);

			if (this.neighborValues.size() == parent.getNeighborhood().size()) {
				this.updateImpacts.clear();
				this.upateLocalCosts();
			}
		} else if (m.getType().equals(MCSMGMSolver.IMPACT_VALUE)) {
			final Double delta = (Double) m.getContent("delta");

			// Change the local problem
			this.updateImpacts.put(source, delta);
			try {
				this.addedCosts.get(this.myVariable.getValue()).put(source, delta);
			} catch (VariableNotSetException e) {
				throw new RuntimeException("Unexpected exception at this point", e);
			}

			if (this.updateImpacts.size() == parent.getNeighborhood().size()) {
				this.lastReduction.clear();
				this.computeLocalReductions();
			}
		} else if (m.getType().equals(MCSMGMSolver.LOCAL_REDUCTION)) {
			final Double reduction = (Double) m.getContent("LR");

			this.lastReduction.put(source, reduction);

			if (this.lastReduction.size() == parent.getNeighborhood().size()) {
				this.neighborValues.clear();
				this.pickValue();
			}
		}
	}

	@Override
	public void tick() {
		try {
			Message updateMsg = new HashMessage(MCSMGMSolver.UPDATE_VALUE);

			updateMsg.addContent("value", this.myVariable.getValue());
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
	private void upateLocalCosts() {
		try {
			this.myProblemContext.setValue(this.myVariable.getValue());
		} catch (VariableNotSetException e) {
			e.printStackTrace();
		}
		
		for (Agent n : neighborValues.keySet()) {
			// And this is why we do not immediately update the local problem
			// context
			double before = this.myCostFunction.evaluate(this.myProblemContext);
			this.myProblemContext.setValue(n, this.neighborValues.get(n));
			double after = this.myCostFunction.evaluate(this.myProblemContext);

			// Compute the cost increase due to the update
			double delta = before - after;
			this.localCosts.put(n, delta);

			HashMessage m = new HashMessage(MCSMGMSolver.IMPACT_VALUE);
			m.addContent("source", this.parent);
			// Only add delta if we want to propagate back our cost
			//if (this.lastReduction.containsKey(n) && delta > this.lastReduction.get(n)) {
			if (delta > 0) {
				m.addContent("delta", new Double(delta));
				this.localCosts.put(n, 0.);
			} else {
				m.addContent("delta", new Double(0.));
			}

			n.push(m);
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

			// Update with any known remote costs
			for (Agent a : this.addedCosts.get(assignment).keySet())
				localCost += this.addedCosts.get(assignment).get(a);
			
			if (localCost < bestCost) {
				bestCost = localCost;
				bestAssignment = assignment;
			}
		}

		this.bestLocalReduction = before - bestCost;
		this.bestLocalAssignment = bestAssignment;

		Message lrMsg = new HashMessage(MCSMGMSolver.LOCAL_REDUCTION);

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
			if (this.lastReduction.get(n) > bestNeighborReduction)
				bestNeighborReduction = this.lastReduction.get(n);

		try {
			if (this.bestLocalReduction > bestNeighborReduction)
				this.myVariable.setValue(bestLocalAssignment);
			if (this.bestLocalReduction == bestNeighborReduction && Math.random() > EQUAL_UPDATE_PROBABILITY)
				this.myVariable.setValue(bestLocalAssignment);
		} catch (InvalidValueException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void reset() {
		this.myVariable.clear();
	}

}
