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

import nl.coenvl.sam.variables.AssignmentMap;
import nl.coenvl.sam.variables.Variable;

/**
 * CostMatrixConstraint
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 26 feb. 2016
 */
public class InequalityConstraint<T extends Variable<V>, V> extends BinaryConstraint<T, V> {

    private final double cost;

    /**
     * @param var1
     * @param var2
     */
    public InequalityConstraint(final T var1, final T var2, final double inequalityCost) {
        super(var1, var2);
        this.cost = inequalityCost;
    }

    public InequalityConstraint(final T var1, final T var2) {
        this(var1, var2, 0.5);
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.constraints.Constraint#getCost(nl.coenvl.sam.variables. Variable)
     */
    @Override
    public double getCost(final T targetVariable) {
        super.assertVariableIsInvolved(targetVariable);
        CompareCounter.compare();

        return this.costOf(this.var1.getValue(), this.var2.getValue());
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.constraints.Constraint#getCostIf(nl.coenvl.sam.variables. Variable, java.util.Map)
     */
    @Override
    public double getCostIf(final T targetVariable, final AssignmentMap<V> values) {
        super.assertVariableIsInvolved(targetVariable);
        CompareCounter.compare();
        if (values.containsAssignment(this.var1) && values.containsAssignment(this.var2)) {
            return this.costOf(values.getAssignment(this.var1), values.getAssignment(this.var2));
        } else {
            return 0;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.constraints.Constraint#getExternalCost()
     */
    @Override
    public double getExternalCost() {
        return 2 * this.costOf(this.var1.getValue(), this.var2.getValue());
    }

    /**
     * @param value
     * @param value2
     * @return
     */
    private double costOf(final V value, final V value2) {
        if ((value instanceof Number) && (value2 instanceof Number)) {
            final double v1 = ((Number) value).doubleValue();
            final double v2 = ((Number) value2).doubleValue();
            if (Math.abs(v1 - v2) < 1e-10) {
                return this.cost;
            } else {
                return 0;
            }
        } else if (value.equals(value2)) {
            return this.cost;
        } else {
            return 0;
        }
    }

}
