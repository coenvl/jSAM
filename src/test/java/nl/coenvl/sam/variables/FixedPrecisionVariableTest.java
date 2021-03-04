/**
 * File testVariable.java
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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.coenvl.sam.exceptions.InvalidDomainException;
import nl.coenvl.sam.exceptions.InvalidValueException;
import nl.coenvl.sam.exceptions.VariableNotSetException;

/**
 * testVariable
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 4 feb. 2014
 *
 */
public class FixedPrecisionVariableTest {

    private FixedPrecisionVariable var;

    @BeforeEach
    public void init() throws InvalidDomainException {
        this.var = new FixedPrecisionVariable(0, 1, .125);
    }

    @Test
    public void testConstructor() throws InvalidDomainException {
        @SuppressWarnings("unused")
        FixedPrecisionVariable t;
        t = new FixedPrecisionVariable(0, 1, .33);
        t = new FixedPrecisionVariable(-8, 100, 20);
        t = new FixedPrecisionVariable(77, 100, 4);
        try {
            t = new FixedPrecisionVariable(0, -7, 1);
            Assertions.fail("An exception was expected");
        } catch (final Exception e) {
            Assertions.assertEquals(e.getClass(), InvalidDomainException.class);
        }

        try {
            t = new FixedPrecisionVariable(0, 1, 1.0001);
            Assertions.fail("An exception was expected");
        } catch (final Exception e) {
            Assertions.assertEquals(e.getClass(), InvalidDomainException.class);
        }

        try {
            t = new FixedPrecisionVariable(0, 1, 0);
            Assertions.fail("An exception was expected");
        } catch (final Exception e) {
            Assertions.assertEquals(e.getClass(), InvalidDomainException.class);
        }
    }

    @Test
    public void testIterator() {
        final Iterator<Double> it = this.var.iterator();
        Assertions.assertTrue(it.hasNext());
        Assertions.assertEquals(it.next(), new Double(0));
        Assertions.assertEquals(it.next(), new Double(0.125));
        Assertions.assertEquals(it.next(), new Double(0.250));
        Assertions.assertEquals(it.next(), new Double(0.375));
        Assertions.assertEquals(it.next(), new Double(0.5));
        Assertions.assertEquals(it.next(), new Double(0.625));
        Assertions.assertEquals(it.next(), new Double(0.750));
        Assertions.assertEquals(it.next(), new Double(0.875));
        Assertions.assertEquals(it.next(), new Double(1.0));
        Assertions.assertFalse(it.hasNext());
        try {
            it.next();
            Assertions.fail("An exception was expected");
        } catch (final Exception e) {
            Assertions.assertEquals(e.getClass(), NoSuchElementException.class);
        }
    }

    @Test
    public void testIteratorRemove() {
        final Iterator<Double> it = this.var.iterator();
        try {
            it.remove();
            Assertions.fail("An exception was expected");
        } catch (final Exception e) {
            Assertions.assertEquals(e.getClass(), UnsupportedOperationException.class);
        }
    }

    @Test
    public void testLowerBound() {
        Assertions.assertEquals(this.var.getLowerBound(), new Double(0));
    }

    @Test
    public void testRandomValue() throws InvalidValueException {
        for (int i = 0; i < 5000; i++) {
            this.var.setValue(this.var.getRandomValue());
            System.out.println("Current value: " + this.var);
        }
    }

    @Test
    public void testShortHandIter() {
        for (final Double iter : this.var) {
            System.out.println("Current value is: " + iter);
        }
    }

    @Test
    public void testUpperBound() {
        Assertions.assertEquals(this.var.getUpperBound(), new Double(1));
    }

    @Test
    public void testValue() throws InvalidValueException, VariableNotSetException {
        this.var.setValue(0.875);
        Assertions.assertEquals(this.var.getValue(), new Double(0.875));
    }
}
