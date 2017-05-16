/**
 * File WPTSensorConstraint.java
 *
 * Copyright 2016 TNO
 */
package nl.coenvl.sam.wpt;

import nl.coenvl.sam.constraints.CompareCounter;
import nl.coenvl.sam.constraints.HigherOrderConstraint;
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

    public static final double THRESHOLD = 0.018;
    public static final double COST = 1e9;

    private final double mError;

    private final double[] position;

    /**
     *
     */
    public WPTSensorConstraint(final double[] pos) {
        this.position = pos;
        this.mError = 1.0;
    }

    /**
    *
    */
    public WPTSensorConstraint(final double[] pos, final double mError) {
        this.position = pos;
        this.mError = mError;
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.constraints.Constraint#getCost(nl.coenvl.sam.variables.Variable)
     */
    @Override
    public double getCost(final T targetVariable) {
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
        CompareCounter.compare();
        double receivedEnergy = 0.0;
        for (final T var : this.constrainedVariables.values()) {
            if (valueMap.containsAssignment(var)) {
                final double pathLoss = PathLossFactor.computePathLoss(this.position, (double[]) var.get("position"));

                // If the variable is set to the proposed value, act as is we ACTUALLY measured the received energy by
                // incorporating the error
                // if (var.isSet() && valueMap.getAssignment(var).equals(var.getValue())) {
                receivedEnergy += this.mError * valueMap.getAssignment(var).doubleValue() * pathLoss;
                // } else {
                // receivedEnergy += valueMap.getAssignment(var).doubleValue() * pathLoss;
                // }

            }
        }
        return receivedEnergy < WPTSensorConstraint.THRESHOLD ? 0 : WPTSensorConstraint.COST;
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.constraints.Constraint#getExternalCost()
     */
    @Override
    public double getExternalCost() {
        double receivedEnergy = 0.0;
        for (final T var : this.constrainedVariables.values()) {
            final double pathLoss = PathLossFactor.computePathLoss(this.position, (double[]) var.get("position"));
            receivedEnergy += this.mError * (var.getValue().doubleValue()) * pathLoss;
        }
        return receivedEnergy < WPTSensorConstraint.THRESHOLD ? 0 : WPTSensorConstraint.COST;
    }

}
