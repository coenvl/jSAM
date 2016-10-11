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
import nl.coenvl.sam.variables.FixedPrecisionVariable;

/**
 * testVariable
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 4 feb. 2014
 *
 */
@SuppressWarnings("static-method")
public class FixedPrecisionVariableTest {

    private FixedPrecisionVariable var;

    @Before
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
            Assert.fail("An exception was expected");
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), InvalidDomainException.class);
        }

        try {
            t = new FixedPrecisionVariable(0, 1, 0);
            Assert.fail("An exception was expected");
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), InvalidDomainException.class);
        }
    }

    @Test
    public void testIterator() {
        Iterator<Double> it = this.var.iterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals(it.next(), new Double(0));
        Assert.assertEquals(it.next(), new Double(0.125));
        Assert.assertEquals(it.next(), new Double(0.250));
        Assert.assertEquals(it.next(), new Double(0.375));
        Assert.assertEquals(it.next(), new Double(0.5));
        Assert.assertEquals(it.next(), new Double(0.625));
        Assert.assertEquals(it.next(), new Double(0.750));
        Assert.assertEquals(it.next(), new Double(0.875));
        Assert.assertEquals(it.next(), new Double(1.0));
        Assert.assertFalse(it.hasNext());
        try {
            it.next();
            Assert.fail("An exception was expected");
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), NoSuchElementException.class);
        }
    }

    @Test
    public void testIteratorRemove() {
        Iterator<Double> it = this.var.iterator();
        try {
            it.remove();
            Assert.fail("An exception was expected");
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), UnsupportedOperationException.class);
        }
    }

    @Test
    public void testLowerBound() {
        Assert.assertEquals(this.var.getLowerBound(), new Double(0));
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
        for (Double iter : this.var) {
            System.out.println("Current value is: " + iter);
        }
    }

    @Test
    public void testUpperBound() {
        Assert.assertEquals(this.var.getUpperBound(), new Double(1));
    }

    @Test
    public void testValue() throws InvalidValueException, VariableNotSetException {
        this.var.setValue(0.875);
        Assert.assertEquals(this.var.getValue(), new Double(0.875));
    }
}
