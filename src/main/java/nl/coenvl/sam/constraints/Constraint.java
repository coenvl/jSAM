/**
 * File Constraint.java
 *
 * This file is part of the jSAM project.
 *
 * Copyright 2016 TNO
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
package nl.coenvl.sam.constraints;

import java.util.Set;
import java.util.UUID;

import nl.coenvl.sam.variables.AssignmentMap;
import nl.coenvl.sam.variables.Variable;

/**
 * Constraint
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 26 feb. 2016
 */
public interface Constraint<T extends Variable<V>, V> {

	/**
	 * Return a set of ids of the variables that are involved in this constraint
	 *
	 * @return
	 */
	public Set<UUID> getVariableIds();

	/**
	 * Returns the costs for any involved variable. May be the same for all involved variables, or may be different.
	 *
	 * @param targetVariable
	 *            The variable for which to return the current cost
	 * @return A double indicating the cost of this constraint
	 */
	public double getCost(T targetVariable);

	/**
	 * Returns the costs for any involved variable assuming the involved variables are set to the values as provided
	 * instead of taking the actual current values.
	 *
	 * @param variable
	 *            The variable for which to return the current cost
	 * @param valueMap
	 *            The a Map of key value pairs, in which the keys are the ids of the involved variables (or a superset
	 *            thereof), and the values are the corresponding variable values.
	 * @return A double indicating the cost of this constraint in the case that the variables are set as in the values
	 *         Map.
	 */
	public double getCostIf(T variable, AssignmentMap<V> valueMap);

	/**
	 * This function is to be used only from OUTSIDE of the simulation. It does not increase the CompareCounter, and it
	 * sums the costs of all involved variables.
	 *
	 * @return
	 */
	public double getExternalCost();

}
