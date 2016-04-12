/**
 * File BiPartiteConstraint.java
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

import nl.coenvl.sam.variables.AssignmentMap;
import nl.coenvl.sam.variables.Variable;

/**
 * BiPartiteConstraint
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 8 apr. 2016
 */
public class BiPartiteConstraint<T extends Variable<V>, V> extends BinaryConstraint<T, V> {

	private final Constraint<T, V> innerConstraint;

	public BiPartiteConstraint(T var1, T var2, Constraint<T, V> c) {
		super(var1, var2);
		assert (c.getVariableIds().contains(var1));
		assert (c.getVariableIds().contains(var2));
		this.innerConstraint = c;
	}

	public T getFrom() {
		return this.var1;
	}

	public T getTo() {
		return this.var2;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nl.coenvl.sam.constraints.Constraint#getCost(nl.coenvl.sam.variables.Variable)
	 */
	@Override
	public double getCost(T targetVariable) {
		return this.innerConstraint.getCost(targetVariable);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nl.coenvl.sam.constraints.Constraint#getCostIf(nl.coenvl.sam.variables.Variable,
	 * nl.coenvl.sam.variables.AssignmentMap)
	 */
	@Override
	public double getCostIf(T variable, AssignmentMap<V> valueMap) {
		return this.innerConstraint.getCostIf(variable, valueMap);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nl.coenvl.sam.constraints.Constraint#getExternalCost()
	 */
	@Override
	public double getExternalCost() {
		return this.innerConstraint.getExternalCost();
	}

}
