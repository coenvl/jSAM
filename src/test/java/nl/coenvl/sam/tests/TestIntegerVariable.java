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
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package nl.coenvl.sam.tests;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import nl.coenvl.sam.exceptions.InvalidDomainException;
import nl.coenvl.sam.exceptions.InvalidValueException;
import nl.coenvl.sam.exceptions.VariableNotSetException;
import nl.coenvl.sam.variables.AssignmentMap;
import nl.coenvl.sam.variables.IntegerVariable;
import nl.coenvl.sam.variables.PublishableMap;
import nl.coenvl.sam.variables.RandomAccessVector;

/**
 * testVariable
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 4 feb. 2014
 *
 */
@SuppressWarnings("static-method")
public class TestIntegerVariable {

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
		} catch (Exception e) {
			Assert.assertEquals(e.getClass(), InvalidDomainException.class);
		}
	}

	@Test
	public void testIterator() {
		Iterator<Integer> it = this.var.iterator();
		Assert.assertTrue(it.hasNext());
		Assert.assertEquals(it.next(), new Integer(0));
		Assert.assertEquals(it.next(), new Integer(1));
		Assert.assertEquals(it.next(), new Integer(2));
		Assert.assertEquals(it.next(), new Integer(3));
		Assert.assertEquals(it.next(), new Integer(4));
		Assert.assertEquals(it.next(), new Integer(5));
		Assert.assertEquals(it.next(), new Integer(6));
		Assert.assertEquals(it.next(), new Integer(7));
		Assert.assertEquals(it.next(), new Integer(8));
		Assert.assertEquals(it.next(), new Integer(9));
		Assert.assertEquals(it.next(), new Integer(10));
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
		Iterator<Integer> it = this.var.iterator();
		try {
			it.remove();
			Assert.fail("An exception was expected");
		} catch (Exception e) {
			Assert.assertEquals(e.getClass(), UnsupportedOperationException.class);
		}
	}

	@Test
	public void testLowerBound() {
		Assert.assertEquals(this.var.getLowerBound(), new Integer(0));
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
		} catch (Exception e) {
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
		for (Integer iter : this.var) {
			System.out.println("Current value is: " + iter);
		}
	}

	@Test
	public void testUpperBound() {
		Assert.assertEquals(this.var.getUpperBound(), new Integer(10));
	}

	/*
	 * @Test public void testIteratorFrom() throws InvalidDomainException, InvalidValueException { IntegerVariable start
	 * = new IntegerVariable(1, 10); start.setValue(5); Iterator<Integer> it = start.getIterator();
	 * assertTrue(it.hasNext()); assertEquals(it.next(), new Integer(6)); assertEquals(it.next(), new Integer(7));
	 * assertEquals(it.next(), new Integer(8)); assertEquals(it.next(), new Integer(9)); assertEquals(it.next(), new
	 * Integer(10)); assertFalse(it.hasNext()); try { it.next(); fail("An exception was expected"); } catch (Exception
	 * e) { assertEquals(e.getClass(), NoSuchElementException.class); } }
	 */

	@Test
	public void testValue() throws InvalidValueException, VariableNotSetException {
		this.var.setValue(9);
		Assert.assertEquals(this.var.getValue(), new Integer(9));
	}

	@Test
	public void testValueCandidates() {
		RandomAccessVector<Integer> candidates = new RandomAccessVector<>();
		try {
			candidates.randomElement();
			Assert.fail("An exception was expected");
		} catch (Exception e) {
			Assert.assertEquals(NoSuchElementException.class, e.getClass());
		}

		Integer candidate = 4;
		candidates.add(candidate);
		for (int i = 1; i < 100; i++) {
			Assert.assertEquals(candidate, candidates.randomElement());
		}

		Integer candidate2 = 6;
		candidates.add(candidate2);

		boolean saw4 = false;
		boolean saw6 = false;

		for (int i = 1; i < 100; i++) {
			saw4 = saw4 || candidates.randomElement() == 4;
			saw6 = saw6 || candidates.randomElement() == 6;
		}

		Assert.assertTrue("Did not see one of the elements, p = 1.5e-30", saw4 && saw6);

	}

	@Test
	public void testProblemContext() {
		AssignmentMap<Integer> pc = new AssignmentMap<>();
		this.var.setValue(5);
		pc.setAssignment(this.var, this.var.getValue());
		String str = pc.toString();
		AssignmentMap<?> dp = (AssignmentMap<?>) PublishableMap.deserialize(str);
		Assert.assertEquals(this.var.getValue(), dp.get(this.var.getID()));
	}
}
