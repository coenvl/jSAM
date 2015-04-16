/**
 * File MobilityCostFunction.java
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

import java.util.ArrayList;

import nl.coenvl.sam.problemcontexts.IndexedProblemContext;
import nl.coenvl.sam.problemcontexts.ProblemContext;

/**
 * MobilityCostFunction
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 14 mei 2014
 * 
 */
public class MobilityCostFunction implements CostFunction {

	private static final int NO_LEADER = Integer.MIN_VALUE;

	private int leadingIdx = NO_LEADER;

	/**
     * 
     */
	public MobilityCostFunction() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.costfunctions.CostFunction#evaluate(nl.coenvl.sam.
	 * IndexedProblemContext)
	 */
	@Override
	public double evaluate(ProblemContext<?> pc) {

		if (!(pc instanceof IndexedProblemContext<?>))
			throw new RuntimeException(
					"Error using MobilityCostFunction with invalid problemcontext");

		@SuppressWarnings("unchecked")
		IndexedProblemContext<Integer> context = (IndexedProblemContext<Integer>) pc;

		if (this.leadingIdx == NO_LEADER)
			return 0;

		ArrayList<Integer> cpa = context.getAssignment();

		// Calculate the packet loss
		double packet_loss = 0;
		for (Integer a : cpa) {
			if (a == null)
				continue;
			packet_loss += a / 4;
		}

		packet_loss /= 25;
		packet_loss *= packet_loss;

		// Create the packet_loss multiplier
		double packet_loss_multiplier = 5 * packet_loss;
		packet_loss_multiplier *= packet_loss_multiplier;
		packet_loss_multiplier += 1;

		// Now calculate the safe distance to follower
		Integer leadingFrequency = cpa.get(this.leadingIdx);
		if (leadingFrequency == null)
			leadingFrequency = 0;

		double safe_dist = leadingFrequency + .1;
		safe_dist /= 1e5;
		safe_dist = Math.pow(safe_dist, -.3);

		return packet_loss_multiplier * safe_dist;
	}

	public void setLeadingVehicleIndex(int idx) {
		this.leadingIdx = idx;
	}

}
