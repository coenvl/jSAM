/**
 * File BooleanVariable.java
 *
 * Copyright 2019 TNO
 */
package nl.coenvl.sam.variables;

import java.util.Arrays;

import nl.coenvl.sam.exceptions.InvalidDomainException;

/**
 * BooleanVariable
 *
 * @author coenvl
 * @version 0.1
 * @since Jul 3, 2019
 */
public class BooleanVariable extends ListVariable<Boolean> {

    private static int unnamedVariableSequence = 0;

    public BooleanVariable() {
        this("MyBooleanVariable" + BooleanVariable.unnamedVariableSequence++);
    }

    public BooleanVariable(final String name) throws InvalidDomainException {
        super(Arrays.asList(false, true), name);
    }

}
