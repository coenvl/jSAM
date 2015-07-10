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
import nl.coenvl.sam.exceptions.PropertyNotSetException;
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
public class ChannelAllocationCostFunction implements CostFunction {

	private final static boolean USE_STEP_COST_FUN = false;

	private final LocalCommunicatingAgent localAgent;

	/**
	 * @param Agent
	 */
	public ChannelAllocationCostFunction(LocalCommunicatingAgent me) {
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
			double x = (Double) this.localAgent.get("xpos");
			double y = (Double) this.localAgent.get("ypos");
			double z;
			if (this.localAgent.has("zpos"))
				z = (Double) this.localAgent.get("zpos");
			else
				z = 0;

			for (Agent neighbor : this.localAgent.getNeighborhood()) {
				if (currentAssignments.containsKey(neighbor)) {
					CompareCounter.compare();
					int neighborValue = currentAssignments.get(neighbor);
					int channelDist = Math.abs(myAssignedValue - neighborValue);

					if (channelDist > 2)
						continue;

					double neighbor_x = (Double) neighbor.get("xpos");
					double neighbor_y = (Double) neighbor.get("ypos");
					double neighbor_z;
					if (neighbor.has("zpos"))
						neighbor_z = (Double) neighbor.get("zpos");
					else
						neighbor_z = 0;

					double dist = dist(x, neighbor_x, y, neighbor_y, z,
							neighbor_z);
					double close = 0;

					if (channelDist == 0) {
						close = 30;
					} else if (channelDist == 1) {
						close = 10;
					} else if (channelDist == 2) {
						close = 5;
					} else
						throw new RuntimeException(
								"Invalid value for channelDistance at this point: "
										+ channelDist);

					if (USE_STEP_COST_FUN)
						cost += (dist < close ? 1 : 0);
					else
						cost += (dist - 3 * close) * (dist - 3 * close)
								/ (9 * close * close);

				}
			}
		} catch (PropertyNotSetException e) {
			throw new RuntimeException(e);
		}

		return cost;
	}

	private double dist(double xa, double xb, double ya, double yb, double za,
			double zb) {
		return Math.sqrt((xa - xb) * (xa - xb) + (ya - yb) * (ya - yb)
				+ (za - zb) * (za - zb));
	}
}
