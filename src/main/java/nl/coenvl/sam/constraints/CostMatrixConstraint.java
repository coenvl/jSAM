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

import java.util.HashMap;
import java.util.Map;

import nl.coenvl.sam.exceptions.CostMatrixRangeException;
import nl.coenvl.sam.exceptions.VariableNotInvolvedException;
import nl.coenvl.sam.variables.AssignmentMap;
import nl.coenvl.sam.variables.DiscreteVariable;

/**
 * CostMatrixConstraint
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 26 feb. 2016
 */
public class CostMatrixConstraint<V> extends BinaryConstraint<DiscreteVariable<V>, V> {

    private final CostMatrix costMatrix1;

    private final CostMatrix costMatrix2;

    /**
     * @param var1
     * @param var2
     */
    public CostMatrixConstraint(DiscreteVariable<V> var1, DiscreteVariable<V> var2, double[][] costs) {
        super(var1, var2);
        this.costMatrix1 = new CostMatrix(var1, var2, costs);
        this.costMatrix2 = this.costMatrix1.transpose();
    }

    /**
     * @param var1
     * @param var2
     */
    public CostMatrixConstraint(DiscreteVariable<V> var1,
            DiscreteVariable<V> var2,
            double[][] costs1,
            double[][] costs2) {
        super(var1, var2);
        this.costMatrix1 = new CostMatrix(var1, var2, costs1);
        this.costMatrix2 = new CostMatrix(var2, var1, costs2);
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.constraints.Constraint#getCost(nl.coenvl.sam.variables. Variable)
     */
    @Override
    public double getCost(DiscreteVariable<V> targetVariable) {
        super.assertVariableIsInvolved(targetVariable);
        CompareCounter.compare();

        if (targetVariable.equals(this.var1)) {
            return this.costMatrix1.getCost(this.var1.getValue(), this.var2.getValue());
        } else if (targetVariable.equals(this.var2)) {
            return this.costMatrix2.getCost(this.var2.getValue(), this.var1.getValue());
        } else {
            throw new VariableNotInvolvedException("Invalid target variable " + targetVariable);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.constraints.Constraint#getCostIf(nl.coenvl.sam.variables. Variable, java.util.Map)
     */
    @Override
    public double getCostIf(DiscreteVariable<V> targetVariable, AssignmentMap<V> values) {
        super.assertVariableIsInvolved(targetVariable);
        CompareCounter.compare();

        // Per default if the valueMap does not contain both values, the cost is zero
        // if ((values == null) || !values.containsAssignment(targetVariable)) { // ||
        // // !values.containsAssignment(this.var1)
        // // ||
        // // !values.containsAssignment(this.var2))
        // // {
        // return 0;
        // }

        if (!values.containsAssignment(targetVariable)) {
            throw new VariableNotInvolvedException("Undefined behavior if target variable is not in valuemap");
        }

        V value1 = values.getAssignment(this.var1);
        V value2 = values.getAssignment(this.var2);

        if (targetVariable.equals(this.var1)) {
            if (value2 == null) {
                return this.costMatrix1.meanValueFor(value1);
            }
            return this.costMatrix1.getCost(value1, value2);
        } else if (targetVariable.equals(this.var2)) {
            if (value1 == null) {
                return this.costMatrix2.meanValueFor(value2);
            }
            return this.costMatrix2.getCost(value2, value1);
        } else {
            throw new VariableNotInvolvedException("Invalid target variable " + targetVariable);
        }
    }

    @Override
    public double getExternalCost() {
        return this.costMatrix1.getCost(this.var1.getValue(), this.var2.getValue())
                + this.costMatrix2.getCost(this.var2.getValue(), this.var1.getValue());
    }

    /**
     * CostMatrix
     *
     * A cost matrix instead of storing the raw double array. This is to avoid memory issues when for instance the
     * variables have values 100 through 110, or if they have enum values.
     *
     * @author leeuwencjv
     * @version 0.1
     * @since 26 feb. 2016
     */
    protected final class CostMatrix {

        private final Map<V, Map<V, Double>> matrix;

        /**
         * Create the cost matrix for two variables based on the given array of arrays of doubles. Checks to see if the
         * array lengths match the variable ranges
         *
         * @param fromVariable
         * @param toVariable
         * @param costs
         */
        public CostMatrix(DiscreteVariable<V> fromVariable, DiscreteVariable<V> toVariable, double[][] costs) {
            this.matrix = new HashMap<>();

            if (costs.length != fromVariable.getRange()) {
                throw new CostMatrixRangeException(
                        "Cost matrix's first dimension does not match first variable's range");
            }

            int idx1 = 0, idx2 = 0;
            for (V val1 : fromVariable) {
                if (costs[idx1].length != toVariable.getRange()) {
                    throw new CostMatrixRangeException(
                            "Cost matrix array " + idx1 + " length does not match second variable's range");
                }

                Map<V, Double> xMap = new HashMap<>();

                idx2 = 0;
                for (V val2 : toVariable) {
                    xMap.put(val2, costs[idx1][idx2++]);
                }

                this.matrix.put(val1, xMap);
                idx1++;
            }
        }

        /**
         * @param value1
         * @return
         */
        public double meanValueFor(V value1) {
            double sum = 0;
            for (Double val : this.matrix.get(value1).values()) {
                sum += val;
            }
            return sum / this.matrix.get(value1).size();
        }

        /**
         * Creates the transpose of the cost matrix, where for each value {@code i,j=v} in the current matrix, in the
         * output every value {@code j,i=v}.
         *
         * @return
         */
        public CostMatrix transpose() {
            CostMatrix other = new CostMatrix();
            for (V from : this.matrix.keySet()) {
                for (V to : this.matrix.get(from).keySet()) {
                    if (!other.matrix.containsKey(to)) {
                        other.matrix.put(to, new HashMap<V, Double>());
                    }
                    other.matrix.get(to).put(from, this.getCost(from, to));
                }
            }
            return other;
        }

        /**
         * Private constructor for transpose function.
         */
        private CostMatrix() {
            this.matrix = new HashMap<>();
        }

        public double getCost(V value1, V value2) {
            if (!this.matrix.containsKey(value1)) {
                throw new CostMatrixRangeException("Value for variable 1 out of range (" + value1 + ")");
            }

            if (!this.matrix.get(value1).containsKey(value2)) {
                throw new CostMatrixRangeException("Value for variable 2 out of range (" + value2 + ")");
            }

            return this.matrix.get(value1).get(value2);
        }

    }
}
