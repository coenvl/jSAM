/**
 * File LocalInequalityConstraintCostFunction.java
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
 * LocalInequalityConstraintCostFunction
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 19 may 2014
 * 
 */
public class NoisyLocalInequalityConstraintCostFunction implements CostFunction {

	private final LocalCommunicatingAgent localAgent;

	/**
	 * @param Agent
	 */
	public NoisyLocalInequalityConstraintCostFunction(LocalCommunicatingAgent me) {
		this.localAgent = me;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * nl.coenvl.sam.CostFunction#evaluate(nl.coenvl.sam.IndexedProblemContext)
	 */
	@Override
	public double evaluate(ProblemContext<?> pc) {

		if (!(pc instanceof LocalProblemContext<?>))
			throw new RuntimeException(
					"Error using LocalInequalityConstraintCostFunction with invalid problemcontext");

		@SuppressWarnings("unchecked")
		LocalProblemContext<Integer> context = (LocalProblemContext<Integer>) pc;

		// Get the current assignment in the problemcontext
		HashMap<Agent, Integer> currentAssignments = context.getAssignment();

		if (!currentAssignments.containsKey(localAgent))
			throw new VariableNotSetException();
		int myAssignedValue = currentAssignments.get(this.localAgent);

		double cost = 0;
		for (Agent neighbor : this.localAgent.getNeighborhood()) {
			CompareCounter.compare();
			if (currentAssignments.containsKey(neighbor)
					&& myAssignedValue == currentAssignments.get(neighbor))
				cost += (Math.random() < .5 ? 0 : 1);
		}

		return cost;
	}
	
	@Override
	public double evaluateFull(ProblemContext<?> pc) {
		if (!(pc instanceof LocalProblemContext<?>))
			throw new RuntimeException(
					"Error using LocalInequalityConstraintCostFunction with invalid problemcontext");

		@SuppressWarnings("unchecked")
		LocalProblemContext<Integer> context = (LocalProblemContext<Integer>) pc;
		HashMap<Agent, Integer> currentAssignments = context.getAssignment();
		
		double cost = 0;
		
		for (Agent one : currentAssignments.keySet()) {
			for (Agent other : currentAssignments.keySet()) {
				if (one == other) continue;
				
				CompareCounter.compare();
				if (currentAssignments.get(one) == currentAssignments.get(other))
					cost += (Math.random() < .5 ? 0 : 1);
			}
		}
		
		return cost;
	}
	
}
