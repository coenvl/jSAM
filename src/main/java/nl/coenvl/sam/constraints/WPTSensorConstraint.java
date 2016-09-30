/**
 * File WPTSensorConstraint.java
 *
 * Copyright 2016 TNO
 */
package nl.coenvl.sam.constraints;

import nl.coenvl.sam.variables.AssignmentMap;
import nl.coenvl.sam.variables.Variable;

/**
 * WPTSensorConstraint
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 23 sep. 2016
 */
public class WPTSensorConstraint<T extends Variable<V>, V extends Number> extends HigherOrderConstraint<T, V> {

    public static final double THRESHOLD = 0.5;
    public static final double CONSTANT = 1.0;
    public static final double COST = 1e9;

    private final double[] position;

    /**
     *
     */
    public WPTSensorConstraint(double[] pos) {
        this.position = pos;
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.constraints.Constraint#getCost(nl.coenvl.sam.variables.Variable)
     */
    @Override
    public double getCost(T targetVariable) {
        CompareCounter.compare();
        return this.getExternalCost();
    }

    /**
     * @param var
     * @return
     */
    private double distanceTo(T var) {
        double[] loc = (double[]) var.get("position");
        return Math.pow(loc[0] - this.position[0], 2) + Math.pow(loc[1] - this.position[1], 2);
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.constraints.Constraint#getCostIf(nl.coenvl.sam.variables.Variable,
     * nl.coenvl.sam.variables.AssignmentMap)
     */
    @Override
    public double getCostIf(T variable, AssignmentMap<V> valueMap) {
        CompareCounter.compare();
        double energy = 0;
        double distance;
        for (T var : this.constrainedVariables.values()) {
            if (valueMap.containsAssignment(var)) {
                distance = this.distanceTo(var);
                energy += (WPTSensorConstraint.CONSTANT * valueMap.getAssignment(var).doubleValue()) / distance;
            }
        }
        return energy < WPTSensorConstraint.THRESHOLD ? 0 : WPTSensorConstraint.COST;
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.constraints.Constraint#getExternalCost()
     */
    @Override
    public double getExternalCost() {
        double energy = 0;
        double distance;
        for (T var : this.constrainedVariables.values()) {
            distance = this.distanceTo(var);
            energy += (WPTSensorConstraint.CONSTANT * var.getValue().doubleValue()) / distance;
        }
        return energy < WPTSensorConstraint.THRESHOLD ? 0 : WPTSensorConstraint.COST;
    }

}
