/**
 * File IntegerVariable.java
 *
 * This file is part of the jSAM project 2014.
 *
 * Copyright 2014 Coen van Leeuwen
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
 *
 */
package nl.coenvl.sam.variables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.coenvl.sam.exceptions.InvalidDomainException;

/**
 * IntegerVariable
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 4 feb. 2014
 *
 */
public final class IntegerVariable extends ListVariable<Integer> {

    private static int unnamedVariableSequence = 0;

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
     *
     * @see #IntegerVariable(Integer, Integer, String)
     */
    public IntegerVariable(final int lowerBound, final int upperBound) throws InvalidDomainException {
        this(lowerBound, upperBound, "MyIntegerVariable" + IntegerVariable.unnamedVariableSequence++);
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
    public IntegerVariable(final int lowerBound, final int upperBound, final String name)
            throws InvalidDomainException {
        super(IntegerVariable.generateDomain(lowerBound, upperBound), name);
    }

    private static List<Integer> generateDomain(final int lowerBound, final int upperBound) {
        if (lowerBound > upperBound) {
            throw new InvalidDomainException();
        }
        final List<Integer> ret = new ArrayList<>();
        for (int i = lowerBound; i <= upperBound; i++) {
            ret.add(i);
        }
        return Collections.unmodifiableList(ret);
    }

}
