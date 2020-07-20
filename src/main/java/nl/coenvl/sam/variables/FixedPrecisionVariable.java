/**
 * File FixedPrecisionVariable.java
 *
 * Copyright 2016 TNO
 */
package nl.coenvl.sam.variables;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import nl.coenvl.sam.exceptions.InvalidDomainException;
import nl.coenvl.sam.exceptions.InvalidValueException;

/**
 * FixedPrecisionVariable
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 23 sep. 2016
 */
public class FixedPrecisionVariable extends ListVariable<Double> {

    private static int unnamedVariableSequence = 0;

    private final double precision;

    /**
     * Creates a variable with a given lower bound and upper bound. Future calls to setValue will never be able to set
     * the Variable value to something higher or lower than these bounds' values
     *
     * @param lowerBound
     *            the lower bound of the variable domain
     * @param upperBound
     *            the upper bound of the variable domain
     *
     * @throws InvalidDomainException
     *             exception is the lower bound is higher than the upper bound
     */
    public FixedPrecisionVariable(final double lowerBound, final double upperBound, final double step) {
        this(lowerBound,
                upperBound,
                step,
                "MyFixedPrecisionVariable" + FixedPrecisionVariable.unnamedVariableSequence++);
    }

    /**
     * Creates a variable with a given lower bound and upper bound. Future calls to setValue will never be able to set
     * the Variable value to something higher or lower than these bounds' values. Also provide the variable with a name
     * which can be used to identify it later.
     *
     * @param lowerBound
     *            the lower bound of the variable domain
     * @param upperBound
     *            the upper bound of the variable domain
     * @param name
     *            The name of the variable
     *
     * @throws InvalidDomainException
     *             exception is the lower bound is higher than the upper bound
     *
     * @see #IntegerVariable(Integer, Integer)
     */
    public FixedPrecisionVariable(final double lowerBound,
            final double upperBound,
            final double step,
            final String name) throws InvalidDomainException {
        super(FixedPrecisionVariable.generateDomain(lowerBound, upperBound, step), name);
        this.precision = step;
    }

    private static List<Double> generateDomain(final double lowerBound, final double upperBound, final double step)
            throws InvalidDomainException {
        if ((lowerBound > upperBound) || (step <= 0) || (step > (upperBound - lowerBound))) {
            throw new InvalidDomainException();
        }

        final List<Double> domain = new LinkedList<>();
        for (double d = lowerBound; d <= upperBound; d += step) {
            domain.add(Double.valueOf(d));
        }
        return Collections.unmodifiableList(domain);
    }

    @Override
    public Variable<Double> setValue(final Double value) throws InvalidValueException {
        Double nearest = null;
        Double diff = this.precision / 10;
        for (final Double d : this) {
            if (Math.abs(d - value) < diff) {
                nearest = d;
                diff = Math.abs(d - value);
            }
        }
        if (nearest != null) {
            return super.setValue(nearest);
        } else {
            return super.setValue(value);
        }
    }

}
