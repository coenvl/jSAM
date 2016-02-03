/**
 * File SemiRandomCostFunction.java
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
 * SemiRandomCostFunction
 * 
 * This semi-random cost function describes the cost functions as used in the
 * experiment by Grinspoun et al. in "Asymmetric Distributed Constraint
 * Optimization Problems" section 5.2
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 2 jan. 2016
 *
 */
public class SemiRandomCostFunction implements CostFunction {

	public static final double COST_ZERO_PROB = 0.35;
	public static final int MAX_COST = 100;

	private static Map<Agent, Map<Agent, CostMatrix>> costMatrices = new HashMap<Agent, Map<Agent, CostMatrix>>();
	
	private final LocalCommunicatingAgent localAgent;

	//private final Map<Agent, CostMatrix> costs;

	/**
	 * 
	 */
	public SemiRandomCostFunction(LocalCommunicatingAgent localAgent) {
		this.localAgent = localAgent;
		Map<Agent, CostMatrix> localCosts = new HashMap<Agent, CostMatrix>();
		SemiRandomCostFunction.costMatrices.put(localAgent, localCosts);
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
			throw new RuntimeException("Error using SemiRandomCostFunction with invalid problemcontext");

		@SuppressWarnings("unchecked")
		LocalProblemContext<Integer> context = (LocalProblemContext<Integer>) pc;

		// Get the current assignment in the problemcontext
		HashMap<Agent, Integer> currentAssignments = context.getAssignment();

		if (!currentAssignments.containsKey(localAgent))
			throw new VariableNotSetException();
		int myAssignedValue = currentAssignments.get(this.localAgent);

		double cost = 0;

		Map<Agent, CostMatrix> localCosts = SemiRandomCostFunction.costMatrices.get(localAgent);
		for (Agent neighbor : this.localAgent.getNeighborhood()) {
			if (!localCosts.containsKey(neighbor))
				localCosts.put(neighbor, new CostMatrix(localAgent, neighbor));

			CompareCounter.compare();

			Integer neighborAssignment = currentAssignments.get(neighbor);
			if (neighborAssignment == null)
				continue;

			cost += localCosts.get(neighbor).getCost(myAssignedValue, neighborAssignment);
		}

		return cost;
	}

	/* (non-Javadoc)
	 * @see nl.coenvl.sam.costfunctions.CostFunction#evaluateFull(nl.coenvl.sam.problemcontexts.ProblemContext)
	 */
	@Override
	public double evaluateFull(ProblemContext<?> pc) {
		if (!(pc instanceof LocalProblemContext<?>))
			throw new RuntimeException("Error using SemiRandomCostFunction with invalid problemcontext");

		@SuppressWarnings("unchecked")
		LocalProblemContext<Integer> context = (LocalProblemContext<Integer>) pc;
		HashMap<Agent, Integer> currentAssignments = context.getAssignment();
		
		double cost = 0;
		for (Agent one : currentAssignments.keySet()) {
			for (Agent other : currentAssignments.keySet()) {
				if (one == other) continue;
				
				if (!SemiRandomCostFunction.costMatrices.containsKey(one))
					SemiRandomCostFunction.costMatrices.put(one, new HashMap<Agent, CostMatrix>());
				
				Map<Agent, CostMatrix> oneCost = SemiRandomCostFunction.costMatrices.get(one);
				
				if (!oneCost.containsKey(other))
					oneCost.put(other, new CostMatrix(one, other));
				
				CompareCounter.compare();
	
				Integer a1 = currentAssignments.get(one);
				Integer a2 = currentAssignments.get(other);
				if (a1 == null || a2 == null)
					continue;
	
				cost += oneCost.get(other).getCost(a1, a2);
			}
		}
		return cost;
	}
	
	static public void resetCostMatrices() {
		for (Map<Agent, CostMatrix> mat : costMatrices.values())
			mat.clear();
		
		costMatrices.clear();
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
				throw new RuntimeException("Random Cost Functions can only be generated for discrete variables");

			if (neighbor.getVariable() instanceof IntegerVariable)
				otherVar = (IntegerVariable) neighbor.getVariable();
			else
				throw new RuntimeException("Random Cost Functions can only be generated for discrete variables");

			// Generate the actual random matrix;
			for (Integer myX : myVar) {
				Map<Integer, Double> xMap = new HashMap<Integer, Double>();
				for (Integer hisX : otherVar) {
					if (Math.random() < SemiRandomCostFunction.COST_ZERO_PROB)
						xMap.put(hisX, 0.0);
					else
						xMap.put(hisX, Math.ceil(Math.random() * SemiRandomCostFunction.MAX_COST));
				}
				this.matrix.put(myX, xMap);
			}

		}

		public double getCost(int myValue, int otherValue) {
			return this.matrix.get(myValue).get(otherValue);
		}

	}
}
