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
    public InequalityConstraint(T var1, T var2, double inequalityCost) {
        super(var1, var2);
        this.cost = inequalityCost;
    }

    public InequalityConstraint(T var1, T var2) {
        this(var1, var2, 0.5);
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.constraints.Constraint#getCost(nl.coenvl.sam.variables. Variable)
     */
    @Override
    public double getCost(T targetVariable) {
        super.assertVariableIsInvolved(targetVariable);
        CompareCounter.compare();

        if (this.var1.getValue().equals(this.var2.getValue())) {
            return this.cost;
        }

        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.constraints.Constraint#getCostIf(nl.coenvl.sam.variables. Variable, java.util.Map)
     */
    @Override
    public double getCostIf(T targetVariable, AssignmentMap<V> values) {
        super.assertVariableIsInvolved(targetVariable);
        CompareCounter.compare();
        if (values.containsAssignment(this.var1) && values.containsAssignment(this.var2)
                && values.getAssignment(this.var1).equals(values.getAssignment(this.var2))) {
            return this.cost;
        }

        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.constraints.Constraint#getExternalCost()
     */
    @Override
    public double getExternalCost() {
        return this.var1.getValue().equals(this.var2.getValue()) ? 2 * this.cost : 0;
    }

}
