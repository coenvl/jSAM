/**
 * File LessThanConstraint.java
 *
 * Copyright 2016 Anonymous
 */
package org.anon.cocoa.constraints;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import org.anon.cocoa.exceptions.VariableNotInvolvedException;
import org.anon.cocoa.variables.AssignmentMap;
import org.anon.cocoa.variables.Variable;

/**
 * LessThanConstraint
 *
 * @author Anomymous
 * @version 0.1
 * @since 27 aug. 2016
 */
public class LessThanConstraint<T extends Variable<V>, V extends Number> implements Constraint<T, V> {

    private final T dynamicVariable;
    private final T staticVariable;

    private final double cost;

    /**
     * @param dynamicVariable
     * @param staticVariable
     */
    public LessThanConstraint(T var1, T var2, double lessThanCost) {
        this.dynamicVariable = var1;
        this.staticVariable = var2;
        this.cost = lessThanCost;
    }

    public LessThanConstraint(T var1, T var2) {
        this(var1, var2, 0.5);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.anon.cocoa.constraints.Constraint#getCost(org.anon.cocoa.variables.Variable)
     */
    @Override
    public double getCost(T targetVariable) {
        if (!targetVariable.equals(this.dynamicVariable)) {
            throw new VariableNotInvolvedException("Variable " + targetVariable + " is not involved in the constraint");
        }

        CompareCounter.compare();

        if (!this.dynamicVariable.isSet()) {
            // If variable1 is not set, return 0; if variable2 let exception be thrown
            return 0;
        }

        return this.dynamicVariable.getValue().doubleValue() < this.staticVariable.getValue().doubleValue() ? 0
                : this.cost;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.anon.cocoa.constraints.Constraint#getCostIf(org.anon.cocoa.variables.Variable,
     * org.anon.cocoa.variables.AssignmentMap)
     */
    @Override
    public double getCostIf(T targetVariable, AssignmentMap<V> valueMap) {
        if (!targetVariable.equals(this.dynamicVariable)) {
            throw new VariableNotInvolvedException("Variable " + targetVariable + " is not involved in the constraint");
        }

        CompareCounter.compare();

        if (!valueMap.containsAssignment(this.dynamicVariable)) {
            // If variable1 is not set, return 0; if variable2 let exception be thrown
            return 0;
        }

        return valueMap.getAssignment(this.dynamicVariable).doubleValue() < this.staticVariable.getValue().doubleValue()
                ? 0 : this.cost;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.anon.cocoa.constraints.Constraint#getExternalCost()
     */
    @Override
    public double getExternalCost() {
        return this.dynamicVariable.getValue().doubleValue() < this.staticVariable.getValue().doubleValue() ? 0
                : this.cost;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.anon.cocoa.constraints.Constraint#getVariableIds()
     */
    @Override
    public Set<UUID> getVariableIds() {
        return Collections.singleton(this.dynamicVariable.getID());
    }

}
