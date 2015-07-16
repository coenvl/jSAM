/**
 * File TaskSchedulingCostFunction.java
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
import nl.coenvl.sam.exceptions.PropertyNotSetException;
import nl.coenvl.sam.exceptions.VariableNotSetException;
import nl.coenvl.sam.problemcontexts.LocalProblemContext;
import nl.coenvl.sam.problemcontexts.ProblemContext;

/**
 * TaskSchedulingCostFunction
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 19 may 2014
 * 
 */
public class TaskSchedulingCostFunction implements CostFunction {

	private final LocalCommunicatingAgent localAgent;

	/**
	 * @param Agent
	 */
	public TaskSchedulingCostFunction(LocalCommunicatingAgent me) {
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
			throw new RuntimeException(
					"Error using LocalInequalityConstraintCostFunction with invalid problemcontext");

		@SuppressWarnings("unchecked")
		LocalProblemContext<Integer> context = (LocalProblemContext<Integer>) pc;

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

		try {
			double data = (Double) this.localAgent.get("data");
			double ops = (Double) this.localAgent.get("ops");

			for (Agent neighbor : this.localAgent.getNeighborhood()) {
				if (currentAssignments.containsKey(neighbor)) {
					CompareCounter.compare();
					int neighborValue = currentAssignments.get(neighbor);

					int delta = Math.abs(myAssignedValue - neighborValue);

					cost += (data * delta) + (delta == 0 ? ops : 0);
					//cost += (data * delta) + (ops / (delta + 1));
				}
			}
		} catch (PropertyNotSetException e) {
			throw new RuntimeException(e);
		}

		return cost;
	}
}
