/**
 * File CostMatrixConstraint.java
 *
 * This file is part of the jSAM project.
 *
 * Copyright 2016 TNO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.coenvl.sam.constraints;

import nl.coenvl.sam.variables.DiscreteVariable;

/**
 * CostMatrixConstraint
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 26 feb. 2016
 */
public class PreferentialEqualityConstraint<V> extends CostMatrixConstraint<V> {

    /**
     * @param var1
     * @param var2
     */
    public PreferentialEqualityConstraint(DiscreteVariable<V> var1,
            DiscreteVariable<V> var2,
            double[] pref1,
            double[] pref2,
            double inequalityCost) {
        super(var1,
                var2,
                PreferentialEqualityConstraint.buildCostMatrix(pref1, inequalityCost),
                PreferentialEqualityConstraint.buildCostMatrix(pref2, inequalityCost));
    }

    /**
     * @param pref
     * @param inequalityCost
     * @return
     */
    private static double[][] buildCostMatrix(double[] pref, double inequalityCost) {
        int size = pref.length;
        double[][] costs = new double[size][size];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (x == y) {
                    costs[x][y] = pref[x];
                } else {
                    costs[x][y] = inequalityCost;
                }
            }
        }
        return costs;
    }

}
