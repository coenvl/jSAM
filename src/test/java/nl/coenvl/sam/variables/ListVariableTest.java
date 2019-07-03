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

import java.util.Arrays;
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
public class ListVariableTest {

    private ListVariable<String> var;

    @Before
    public void init() throws InvalidDomainException {
        this.var = new ListVariable<>(Arrays.asList("een", "twee", "drie", "vijf", "drie", "vijf"));
    }

    @Test
    public void testIterator() {
        final Iterator<String> it = this.var.iterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("een", it.next());
        Assert.assertEquals("twee", it.next());
        Assert.assertEquals("drie", it.next());
        Assert.assertEquals("vijf", it.next());
        Assert.assertEquals("drie", it.next());
        Assert.assertEquals("vijf", it.next());
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
        final Iterator<String> it = this.var.iterator();
        try {
            it.remove();
            Assert.fail("An exception was expected");
        } catch (final Exception e) {
            Assert.assertEquals(e.getClass(), UnsupportedOperationException.class);
        }
    }

    @Test
    public void testLowerBound() {
        Assert.assertEquals("een", this.var.getLowerBound());
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
        for (final String iter : this.var) {
            System.out.println("Current value is: " + iter);
        }
    }

    @Test
    public void testUpperBound() {
        Assert.assertEquals("vijf", this.var.getUpperBound());
    }

    @Test
    public void testValue() throws InvalidValueException, VariableNotSetException {
        this.var.setValue("drie");
        Assert.assertEquals("drie", this.var.getValue());
    }

    @Test
    public void testProblemContext() {
        final AssignmentMap<String> pc = new AssignmentMap<>();
        this.var.setValue("twee");
        pc.setAssignment(this.var, this.var.getValue());
        final String str = pc.serialize();
        final AssignmentMap<?> dp = (AssignmentMap<?>) PublishableMap.deserialize(str);
        Assert.assertEquals(this.var.getValue(), dp.get(this.var.getID()));
    }
}
