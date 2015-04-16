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
public class LocalInequalityConstraintCostFunction implements CostFunction {

	public static int nComparisons = 0;

	private final LocalCommunicatingAgent localAgent;

	/**
	 * @param Agent
	 */
	public LocalInequalityConstraintCostFunction(LocalCommunicatingAgent me) {
		this.localAgent = me;
	}

	// public double currentValue() throws VariableNotSetException {
	// HashMap<Agent, Integer> assignment = new HashMap<Agent, Integer>();
	//
	// assignment.put(localAgent, (Integer)
	// localAgent.getVariable().getValue());
	// for (Agent a : localAgent.getNeighborhood())
	// assignment.put(a, (Integer) a.getVariable().getValue());
	//
	// LocalProblemContext<Integer> cpc = new
	// LocalProblemContext<Integer>(localAgent);
	// cpc.setAssignment(assignment);
	//
	// return this.evaluate(cpc);
	// }

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

		LocalInequalityConstraintCostFunction.nComparisons++;

		// Get the current assignment in the problemcontext
		HashMap<Agent, Integer> currentAssignments = context.getAssignment();

		Integer myAssignedValue = currentAssignments.get(this.localAgent);

		try {
			assert (myAssignedValue == this.localAgent.getVariable().getValue());
		} catch (VariableNotSetException e) {
			throw new RuntimeException(
					"Variable value should not be null here?");
		}

		// Should never be the case right?
		if (myAssignedValue == null)
			return 0.0;

		double cost = 0;

		for (Agent neighbor : this.localAgent.getNeighborhood()) {
			if (currentAssignments.containsKey(neighbor)
					&& myAssignedValue.equals(currentAssignments.get(neighbor)))
				cost++;
		}

		// Iterate over all values in the problem context to see if there is one
		// with the same value
		// Start with negative one because it should always equal to itself
		// double cost = -1.0;
		//
		// for (Iterator<Integer> iter = context.iterator(); iter.hasNext();) {
		// nComparisons++;
		// if (assignedValue.equals(iter.next()))
		// cost++;
		// }

		return cost;
	}
}
