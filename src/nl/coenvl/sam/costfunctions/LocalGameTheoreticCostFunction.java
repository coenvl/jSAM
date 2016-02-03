/**
 * File LocalGameTheoreticCostFunction.java
 *
 * This file is part of the jSAM project 2014.
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
package nl.coenvl.sam.costfunctions;

import java.util.HashMap;

import nl.coenvl.sam.agents.Agent;
import nl.coenvl.sam.agents.LocalCommunicatingAgent;
import nl.coenvl.sam.exceptions.VariableNotSetException;
import nl.coenvl.sam.problemcontexts.LocalProblemContext;
import nl.coenvl.sam.problemcontexts.ProblemContext;

/**
 * LocalGameTheoreticCostFunction
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 19 may 2014
 * 
 */
public class LocalGameTheoreticCostFunction implements CostFunction {

	private final LocalCommunicatingAgent localAgent;

	/**
	 * @param Agent
	 */
	public LocalGameTheoreticCostFunction(LocalCommunicatingAgent me) {
		this.localAgent = me;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.anon.cocoa.CostFunction#evaluate(org.anon.cocoa.IndexedProblemContext
	 * )
	 */
	@Override
	public double evaluate(ProblemContext<?> pc) {
		if (!(pc instanceof LocalProblemContext<?>))
			throw new RuntimeException("Error using LocalInequalityConstraintCostFunction with invalid problemcontext");

		@SuppressWarnings("unchecked")
		LocalProblemContext<Integer> context = (LocalProblemContext<Integer>) pc;
		HashMap<Agent, Integer> currentAssignments = context.getAssignment();

		if (!currentAssignments.containsKey(localAgent))
			throw new VariableNotSetException();
		int myAssignedValue = currentAssignments.get(this.localAgent);

		double cost = 0;

		for (Agent neighbor : this.localAgent.getNeighborhood()) {
			if (currentAssignments.containsKey(neighbor)) {
				CompareCounter.compare();

				Integer otherValue = currentAssignments.get(neighbor);
				cost += LocalGameTheoreticCostFunction.getCost(myAssignedValue, otherValue);
			}

		}

		return cost;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.costfunctions.CostFunction#evaluateFull(nl.coenvl.sam.
	 * problemcontexts.ProblemContext)
	 */
	@Override
	public double evaluateFull(ProblemContext<?> pc) {
		if (!(pc instanceof LocalProblemContext<?>))
			throw new RuntimeException("Error using LocalInequalityConstraintCostFunction with invalid problemcontext");

		@SuppressWarnings("unchecked")
		LocalProblemContext<Integer> context = (LocalProblemContext<Integer>) pc;
		HashMap<Agent, Integer> currentAssignments = context.getAssignment();

		double cost = 0;

		for (Agent one : currentAssignments.keySet()) {
			for (Agent two : currentAssignments.keySet()) {
				CompareCounter.compare();

				Integer a1 = currentAssignments.get(one);
				Integer a2 = currentAssignments.get(two);
				cost += LocalGameTheoreticCostFunction.getCost(a1, a2);
			}

		}

		return cost;
	}

	/**
	 * @param a
	 * @param b
	 * @return
	 */
	private static double getCost(int a, int b) {
		if (a == 1 && b == 1)
			return 1;
		else if (a == 2 && b == 2)
			return 1;
		else if (a == 3 && b == 3)
			return 1;
		else if (a == 1 && b == 2)
			return 0;
		else if (a == 2 && b == 3)
			return 0;
		else if (a == 3 && b == 1)
			return 0;
		else if (a == 1 && b == 3)
			return 3;
		else if (a == 2 && b == 1)
			return 3;
		else if (a == 3 && b == 2)
			return 3;
		else
			throw new RuntimeException("Invalid values for the GameTheoretic cost function! (" + a + " & " + b + ")");
	}
}
