/**
 * File LessThanConstraint.java
 *
 * Copyright 2016 TNO
 */
package nl.coenvl.sam.constraints;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import nl.coenvl.sam.variables.AssignmentMap;
import nl.coenvl.sam.variables.Variable;

/**
 * LessThanConstraint
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 27 aug. 2016
 */
public class LessThanConstraint<T extends Variable<V>, V extends Number> extends BinaryConstraint<T, V> {

    private final T dynamicVariable;
    private final T staticVariable;

    private final double cost;

    /**
     * @param dynamicVariable
     * @param staticVariable
     */
    public LessThanConstraint(T var1, T var2, double lessThanCost) {
        super(var1, var2);
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
     * @see nl.coenvl.sam.constraints.Constraint#getCost(nl.coenvl.sam.variables.Variable)
     */
    @Override
    public double getCost(T targetVariable) {
        super.assertVariableIsInvolved(targetVariable);
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
     * @see nl.coenvl.sam.constraints.Constraint#getCostIf(nl.coenvl.sam.variables.Variable,
     * nl.coenvl.sam.variables.AssignmentMap)
     */
    @Override
    public double getCostIf(T targetVariable, AssignmentMap<V> valueMap) {
        super.assertVariableIsInvolved(targetVariable);
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
     * @see nl.coenvl.sam.constraints.Constraint#getExternalCost()
     */
    @Override
    public double getExternalCost() {
        return this.dynamicVariable.getValue().doubleValue() < this.staticVariable.getValue().doubleValue() ? 0
                : this.cost;
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.constraints.Constraint#getVariableIds()
     */
    @Override
    public Set<UUID> getVariableIds() {
        return Collections.singleton(this.dynamicVariable.getID());
        // return new HashSet<>(Arrays.asList(this.dynamicVariable.getID(), this.staticVariable.getID()));
    }

}
