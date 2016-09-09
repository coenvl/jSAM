/**
 * File SemiRandomConstraint.java
 *
 * This file is part of the jCoCoA project.
 *
 * Copyright 2016 Anonymous
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
package org.anon.cocoa.constraints;

import org.anon.cocoa.variables.DiscreteVariable;

/**
 * SemiRandomConstraint
 *
 * @author Anomymous
 * @version 0.1
 * @since 26 feb. 2016
 */
public class SemiRandomConstraint<V> extends CostMatrixConstraint<V> {

	public static final double COST_ZERO_PROB = 0.35;

	public static final int MAX_COST = 100;

	public SemiRandomConstraint(DiscreteVariable<V> var1, DiscreteVariable<V> var2) {
		super(var1,
				var2,
				SemiRandomConstraint.randomMatrix(var1.getRange(), var2.getRange()),
				SemiRandomConstraint.randomMatrix(var2.getRange(), var1.getRange()));
	}

	/**
	 * Create a random matrix
	 *
	 * @param v2
	 * @param v1
	 * @return
	 */
	private static double[][] randomMatrix(int x, int y) {
		double[][] costs = new double[x][y];

		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				if (Math.random() > SemiRandomConstraint.COST_ZERO_PROB) {
					costs[i][j] = Math.random() * SemiRandomConstraint.MAX_COST;
				}
			}
		}

		return costs;
	}
}
