/**
 * File PathLossFactor.java
 *
 * Copyright 2017 TNO
 */
package nl.coenvl.sam.wpt;

/**
 * PathLossFactor
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 17 mrt. 2017
 */
public class PathLossFactor {

    /**
     * @param distance
     * @return power levels according to the simple path loss
     */
    public static double computePathLoss(final double distance) {
        final double alpha = 100;
        final double beta = 100;
        return alpha / Math.pow(distance + beta, 2);
    }

    public static double computePathLoss(final double[] from, final double[] to) {
        final double distance = Math.hypot(from[0] - to[0], from[1] - to[1]);
        return PathLossFactor.computePathLoss(distance);
    }

}
