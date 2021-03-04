/**
 * File PreferenceConstraint.java
 *
 * Copyright 2019 TNO
 */
package nl.coenvl.sam.constraints;

import java.util.HashMap;
import java.util.Map;

import nl.coenvl.sam.variables.AssignmentMap;
import nl.coenvl.sam.variables.DiscreteVariable;

/**
 * PreferenceConstraint
 *
 * @author coenvl
 * @version 0.1
 * @since Aug 28, 2019
 */
public class PreferenceConstraint<T extends DiscreteVariable<V>, V> extends UnaryConstraint<T, V> {

    Map<V, Double> costs;

    /**
     * @param var1
     * @param var2
     */
    public PreferenceConstraint(final T var, final double[] prefs) {
        this(var, PreferenceConstraint.buildCostMatrix(var, prefs));
    }

    /**
     * @param var1
     * @param var2
     */
    public PreferenceConstraint(final T var, final Map<V, Double> costs) {
        super(var);
        this.costs = costs;

        for (final V value : var) {
            if (!costs.containsKey(value)) {
                throw new IllegalArgumentException("Missing cost for value " + value);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.constraints.Constraint#getCost(nl.coenvl.sam.variables.Variable)
     */
    @Override
    public double getCost(final T targetVariable) {
        super.assertVariableIsInvolved(targetVariable);
        CompareCounter.compare();

        return this.getExternalCost();
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.constraints.Constraint#getCostIf(nl.coenvl.sam.variables.Variable,
     * nl.coenvl.sam.variables.AssignmentMap)
     */
    @Override
    public double getCostIf(final T variable, final AssignmentMap<V> valueMap) {
        super.assertVariableIsInvolved(variable);
        CompareCounter.compare();

        if (valueMap.containsAssignment(variable)) {
            return this.costs.get(valueMap.getAssignment(variable));
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
        if (this.var.isSet()) {
            return this.costs.get(this.var.getValue());
        } else {
            return 0;
        }
    }

    private static <V> Map<V, Double> buildCostMatrix(final DiscreteVariable<V> var, final double[] prefs) {
        final Map<V, Double> ret = new HashMap<>();
        int i = 0;
        for (final V value : var) {
            ret.put(value, prefs[i++]);
        }
        return ret;
    }

}
