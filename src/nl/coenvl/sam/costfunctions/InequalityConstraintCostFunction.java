/**
 * File InequalityConstraintCostFunction.java
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
import java.util.HashSet;
import java.util.Iterator;

import nl.coenvl.sam.problemcontexts.IndexedProblemContext;
import nl.coenvl.sam.problemcontexts.ProblemContext;

/**
 * InequalityConstraintCostFunction
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 19 mrt. 2014
 * 
 */
public class InequalityConstraintCostFunction implements CostFunction {

	private HashSet<Integer> constraintsIndices = new HashSet<Integer>();

	private final Integer myIndex;

	public InequalityConstraintCostFunction(int index) {
		this.myIndex = index;
	}

	/**
	 * Add an index to the constraint of which the value may not be equal to the
	 * assigned value.
	 * 
	 * @param idx
	 */
	public void addConstraintIndex(int idx) {
		this.constraintsIndices.add(idx);
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

		if (!(pc instanceof IndexedProblemContext<?>))
			throw new RuntimeException(
					"Error using MobilityCostFunction with invalid problemcontext");

		@SuppressWarnings("unchecked")
		IndexedProblemContext<Integer> context = (IndexedProblemContext<Integer>) pc;

		double cost = 0;
		Object assignedValue = context.getValue(myIndex);

		if (assignedValue == null)
			return cost;

		ArrayList<Integer> currentAssignment = context.getAssignment();

		for (Iterator<Integer> v = constraintsIndices.iterator(); v.hasNext();) {
			CompareCounter.compare();
			Integer constraintIdx = v.next();
			if (currentAssignment.size() > constraintIdx
					&& assignedValue.equals(currentAssignment
							.get(constraintIdx)))
				cost++;
		}

		return cost;
	}
	
	/* (non-Javadoc)
	 * @see nl.coenvl.sam.costfunctions.CostFunction#evaluateFull(nl.coenvl.sam.problemcontexts.ProblemContext)
	 */
	@Override
	public double evaluateFull(ProblemContext<?> context) {
		throw new RuntimeException("NYI");
	}
}
