/**
 * File RandomCostFunction.java
 * 
 * This file is part of the jSAM project.
 *
 * Copyright 2015 TNO
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
package nl.coenvl.sam.costfunctions;

import java.util.HashMap;
import java.util.Map;

import nl.coenvl.sam.agents.Agent;
import nl.coenvl.sam.agents.LocalCommunicatingAgent;
import nl.coenvl.sam.exceptions.VariableNotSetException;
import nl.coenvl.sam.problemcontexts.LocalProblemContext;
import nl.coenvl.sam.problemcontexts.ProblemContext;
import nl.coenvl.sam.variables.IntegerVariable;

/**
 * RandomCostFunction
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 16 apr. 2015
 *
 */
public class RandomCostFunction implements CostFunction {

	private final LocalCommunicatingAgent localAgent;

	private final Map<Agent, CostMatrix> costs;

	/**
	 * 
	 */
	public RandomCostFunction(LocalCommunicatingAgent localAgent) {
		this.costs = new HashMap<Agent, CostMatrix>();
		this.localAgent = localAgent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.costfunctions.CostFunction#evaluate(nl.coenvl.sam.
	 * problemcontexts.ProblemContext)
	 */
	@Override
	public double evaluate(ProblemContext<?> pc) {
		
		if (!(pc instanceof LocalProblemContext<?>))
			throw new RuntimeException(
					"Error using RandomCostFunction with invalid problemcontext");

		@SuppressWarnings("unchecked")
		LocalProblemContext<Integer> context = (LocalProblemContext<Integer>) pc;
		
		// Get the current assignment in the problemcontext
		HashMap<Agent, Integer> currentAssignments = context.getAssignment();

		if (!currentAssignments.containsKey(localAgent))
			throw new VariableNotSetException();
		
		int myAssignedValue = currentAssignments.get(this.localAgent);

		/*try {
			assert (myAssignedValue == this.localAgent.getVariable().getValue());
		} catch (VariableNotSetException e) {
			throw new RuntimeException(
					"Variable value should not be null here?");
		}

		// Should never be the case right?
		if (myAssignedValue == null)
			return 0.0;*/

		double cost = 0;

		for (Agent neighbor : this.localAgent.getNeighborhood()) {
			if (!this.costs.containsKey(neighbor))
				this.costs.put(neighbor, new CostMatrix(localAgent, neighbor));

			CompareCounter.compare();
			
			Integer neighborAssignment = currentAssignments.get(neighbor);
			if (neighborAssignment == null)
				continue;
			
			cost += this.costs.get(neighbor).getCost(myAssignedValue, neighborAssignment);
		}

		return cost;
	}

	private class CostMatrix {

		private final Map<Integer, Map<Integer, Double>> matrix;

		public CostMatrix(Agent owner, Agent neighbor) {
			this.matrix = new HashMap<Integer, Map<Integer, Double>>();

			IntegerVariable myVar;
			IntegerVariable otherVar;

			if (owner.getVariable() instanceof IntegerVariable)
				myVar = (IntegerVariable) owner.getVariable();
			else
				throw new RuntimeException(
						"Random Cost Functions can only be generated for discrete variables");

			if (neighbor.getVariable() instanceof IntegerVariable)
				otherVar = (IntegerVariable) neighbor.getVariable();
			else
				throw new RuntimeException(
						"Random Cost Functions can only be generated for discrete variables");

			// Generate the actual random matrix;
			for (Integer myX : myVar) {
				Map<Integer, Double> xMap = new HashMap<Integer, Double>();
				for (Integer hisX : otherVar)
					xMap.put(hisX, Math.random());
				this.matrix.put(myX, xMap);
			}

		}

		public double getCost(Integer myValue, Integer otherValue) {
			return this.matrix.get(myValue).get(otherValue);
		}

	}
	
	/* (non-Javadoc)
	 * @see nl.coenvl.sam.costfunctions.CostFunction#evaluateFull(nl.coenvl.sam.problemcontexts.ProblemContext)
	 */
	@Override
	public double evaluateFull(ProblemContext<?> context) {
		throw new RuntimeException("NYI");
	}

}
