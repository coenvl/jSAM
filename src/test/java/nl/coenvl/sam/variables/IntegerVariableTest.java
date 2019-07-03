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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
@SuppressWarnings("static-method")
public class IntegerVariableTest {

    private IntegerVariable var;

    @Before
    public void init() throws InvalidDomainException {
        this.var = new IntegerVariable(0, 10);
    }

    @Test
    public void testConstructor() throws InvalidDomainException {
        @SuppressWarnings("unused")
        IntegerVariable t;
        t = new IntegerVariable(0, 1);
        t = new IntegerVariable(-4, 10);
        t = new IntegerVariable(77, 100);
        try {
            t = new IntegerVariable(0, -7);
            Assert.fail("An exception was expected");
        } catch (final Exception e) {
            Assert.assertEquals(e.getClass(), InvalidDomainException.class);
        }
    }

    @Test
    public void testIterator() {
        final Iterator<Integer> it = this.var.iterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals(Integer.valueOf(0), it.next());
        Assert.assertEquals(Integer.valueOf(1), it.next());
        Assert.assertEquals(Integer.valueOf(2), it.next());
        Assert.assertEquals(Integer.valueOf(3), it.next());
        Assert.assertEquals(Integer.valueOf(4), it.next());
        Assert.assertEquals(Integer.valueOf(5), it.next());
        Assert.assertEquals(Integer.valueOf(6), it.next());
        Assert.assertEquals(Integer.valueOf(7), it.next());
        Assert.assertEquals(Integer.valueOf(8), it.next());
        Assert.assertEquals(Integer.valueOf(9), it.next());
        Assert.assertEquals(Integer.valueOf(10), it.next());
        Assert.assertFalse(it.hasNext());
        try {
            it.next();
            Assert.fail("An exception was expected");
        } catch (final Exception e) {
            Assert.assertEquals(e.getClass(), NoSuchElementException.class);
        }
    }

    @Test
    public void testIteratorRemove() {
        final Iterator<Integer> it = this.var.iterator();
        try {
            it.remove();
            Assert.fail("An exception was expected");
        } catch (final Exception e) {
            Assert.assertEquals(e.getClass(), UnsupportedOperationException.class);
        }
    }

    @Test
    public void testLowerBound() {
        Assert.assertEquals(Integer.valueOf(0), this.var.getLowerBound());
    }

    @Test
    public void testNamedConstructor() throws InvalidDomainException {
        @SuppressWarnings("unused")
        IntegerVariable t;
        t = new IntegerVariable(0, 0, "ZeroVar");
        t = new IntegerVariable(-4, 10, "OtherVar");
        t = new IntegerVariable(77, 100, "LastValidVar");
        try {
            t = new IntegerVariable(0, -7, "Never really a var");
            Assert.fail("An exception was expected");
        } catch (final Exception e) {
            Assert.assertEquals(e.getClass(), InvalidDomainException.class);
        }
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
        for (final Integer iter : this.var) {
            System.out.println("Current value is: " + iter);
        }
    }

    @Test
    public void testUpperBound() {
        Assert.assertEquals(Integer.valueOf(10), this.var.getUpperBound());
    }

    @Test
    public void testValue() throws InvalidValueException, VariableNotSetException {
        this.var.setValue(9);
        Assert.assertEquals(Integer.valueOf(9), this.var.getValue());
    }

    @Test
    public void testProblemContext() {
        final AssignmentMap<Integer> pc = new AssignmentMap<>();
        this.var.setValue(5);
        pc.setAssignment(this.var, this.var.getValue());
        final String str = pc.serialize();
        final AssignmentMap<?> dp = (AssignmentMap<?>) PublishableMap.deserialize(str);
        Assert.assertEquals(this.var.getValue(), dp.get(this.var.getID()));
    }
}
