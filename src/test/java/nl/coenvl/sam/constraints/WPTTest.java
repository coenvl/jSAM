/**
 * File WPTTest.java
 *
 * Copyright 2017 TNO
 */
package nl.coenvl.sam.constraints;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nl.coenvl.sam.exceptions.InvalidPropertyException;
import nl.coenvl.sam.variables.FixedPrecisionVariable;
import nl.coenvl.sam.wpt.PathLossFactor;
import nl.coenvl.sam.wpt.WPTReceiverConstraint;
import nl.coenvl.sam.wpt.WPTSensorConstraint;

/**
 * WPTTest
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 17 mrt. 2017
 */
public class WPTTest {

    private final double ACCEPTABLE_ERROR = 1e-12;

    @Test
    public void testDecay() {
        Assertions.assertEquals(6.2497e-12, PathLossFactor.computePathLoss(4e6), this.ACCEPTABLE_ERROR);
        Assertions.assertEquals(4e-4, PathLossFactor.computePathLoss(400), this.ACCEPTABLE_ERROR);
        Assertions.assertEquals(0.0016, PathLossFactor.computePathLoss(150), this.ACCEPTABLE_ERROR);
        Assertions.assertEquals(0.01, PathLossFactor.computePathLoss(0.0), this.ACCEPTABLE_ERROR);
        Assertions.assertEquals(0.0, PathLossFactor.computePathLoss(Double.MAX_VALUE), this.ACCEPTABLE_ERROR);
    }

    @Test
    public void testDistance() {
        final double[] a = {0, 0};
        final double[] b = {3, 4};
        final double[] c = {100, 100};
        final double[] d = {-500, 1000};

        // These values are calculated using the original function in MATLAB
        Assertions.assertEquals(0.01, PathLossFactor.computePathLoss(a, a), this.ACCEPTABLE_ERROR);
        Assertions.assertEquals(9.070294784e-3, PathLossFactor.computePathLoss(a, b), this.ACCEPTABLE_ERROR);
        Assertions.assertEquals(1.715728752e-3, PathLossFactor.computePathLoss(a, c), this.ACCEPTABLE_ERROR);
        Assertions.assertEquals(6.740330399e-05, PathLossFactor.computePathLoss(a, d), this.ACCEPTABLE_ERROR);
    }

    @Test
    public void testReceiverConstraint() throws InvalidPropertyException {
        final double[] receiverPosition = {0, 0};
        final HigherOrderConstraint<FixedPrecisionVariable, Double> receiverConstraint = new WPTReceiverConstraint<>(
                receiverPosition);

        // A variable AT the receiver should have maximum influence
        final FixedPrecisionVariable var = new FixedPrecisionVariable(0, 10, 0.1);
        var.set("position", receiverPosition);
        var.setValue(0.5);

        receiverConstraint.addVariable(var);
        Assertions.assertEquals(-0.005, receiverConstraint.getExternalCost(), this.ACCEPTABLE_ERROR);

        // A variable at some distance has a reasonable influence
        final double[] a = {81, -63};
        final FixedPrecisionVariable var2 = new FixedPrecisionVariable(0, 10, 0.1);
        var2.set("position", a);
        var2.setValue(1.0);

        receiverConstraint.addVariable(var2);
        Assertions.assertEquals(-0.007435866221259, receiverConstraint.getExternalCost(), this.ACCEPTABLE_ERROR);

        // A variable VERY far away should have no influence
        final double[] b = {Double.MAX_VALUE, Double.MAX_VALUE};
        final FixedPrecisionVariable var3 = new FixedPrecisionVariable(0, 10, 0.1);
        var3.set("position", b);
        var3.setValue(9.999);

        receiverConstraint.addVariable(var3);
        Assertions.assertEquals(-0.007435866221259, receiverConstraint.getExternalCost(), this.ACCEPTABLE_ERROR);
    }

    @Test
    public void testSensorConstraint() throws InvalidPropertyException {
        final double[] sensorPosition = {0, 0};
        final HigherOrderConstraint<FixedPrecisionVariable, Double> sensorConstraint = new WPTSensorConstraint<>(
                sensorPosition);

        // A variable AT the receiver should have maximum influence
        final FixedPrecisionVariable var = new FixedPrecisionVariable(0, 10, 0.1);
        var.set("position", sensorPosition);
        var.setValue(1.0);

        sensorConstraint.addVariable(var);
        Assertions.assertEquals(0.0, sensorConstraint.getExternalCost(), this.ACCEPTABLE_ERROR);

        var.setValue(1.7);
        Assertions.assertEquals(0.0, sensorConstraint.getExternalCost(), this.ACCEPTABLE_ERROR);

        // At this value we reach the threshold received energy, so we get a huge penalty
        var.setValue(1.8);
        Assertions.assertEquals(WPTSensorConstraint.COST, sensorConstraint.getExternalCost(), this.ACCEPTABLE_ERROR);
    }

}
