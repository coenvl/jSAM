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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.NoSuchElementException;

import nl.coenvl.sam.exceptions.InvalidDomainException;
import nl.coenvl.sam.exceptions.InvalidValueException;
import nl.coenvl.sam.exceptions.VariableNotSetException;
import nl.coenvl.sam.variables.IntegerVariable;

import org.junit.Before;
import org.junit.Test;

/**
 * testVariable
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 4 feb. 2014
 * 
 */
public class TestVariable {

	private IntegerVariable var;

	@Before
	public void init() throws InvalidDomainException {
		var = new IntegerVariable(0, 10);
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
			fail("An exception was expected");
		} catch (Exception e) {
			assertEquals(e.getClass(), InvalidDomainException.class);
		}
	}

	@Test
	public void testIterator() {
		Iterator<Integer> it = var.iterator();
		assertTrue(it.hasNext());
		assertEquals(it.next(), new Integer(0));
		assertEquals(it.next(), new Integer(1));
		assertEquals(it.next(), new Integer(2));
		assertEquals(it.next(), new Integer(3));
		assertEquals(it.next(), new Integer(4));
		assertEquals(it.next(), new Integer(5));
		assertEquals(it.next(), new Integer(6));
		assertEquals(it.next(), new Integer(7));
		assertEquals(it.next(), new Integer(8));
		assertEquals(it.next(), new Integer(9));
		assertEquals(it.next(), new Integer(10));
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("An exception was expected");
		} catch (Exception e) {
			assertEquals(e.getClass(), NoSuchElementException.class);
		}
	}

	@Test
	public void testIteratorRemove() {
		Iterator<Integer> it = var.iterator();
		try {
			it.remove();
			fail("An exception was expected");
		} catch (Exception e) {
			assertEquals(e.getClass(), UnsupportedOperationException.class);
		}
	}

	@Test
	public void testLowerBound() {
		assertEquals(var.getLowerBound(), new Integer(0));
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
			fail("An exception was expected");
		} catch (Exception e) {
			assertEquals(e.getClass(), InvalidDomainException.class);
		}
	}

	@Test
	public void testRandomValue() throws InvalidValueException {
		for (int i = 0; i < 5000; i++) {
			this.var.setValue(var.getRandomValue());
			System.out.println("Current value: " + this.var);
		}
	}

	@Test
	public void testShortHandIter() {
		for (Integer iter : var)
			System.out.println("Current value is: " + iter);
	}

	@Test
	public void testUpperBound() {
		assertEquals(var.getUpperBound(), new Integer(10));
	}

	/*
	 * @Test public void testIteratorFrom() throws InvalidDomainException,
	 * InvalidValueException { IntegerVariable start = new IntegerVariable(1,
	 * 10); start.setValue(5); Iterator<Integer> it = start.getIterator();
	 * assertTrue(it.hasNext()); assertEquals(it.next(), new Integer(6));
	 * assertEquals(it.next(), new Integer(7)); assertEquals(it.next(), new
	 * Integer(8)); assertEquals(it.next(), new Integer(9));
	 * assertEquals(it.next(), new Integer(10)); assertFalse(it.hasNext()); try
	 * { it.next(); fail("An exception was expected"); } catch (Exception e) {
	 * assertEquals(e.getClass(), NoSuchElementException.class); } }
	 */

	@Test
	public void testValue() throws InvalidValueException,
			VariableNotSetException {
		var.setValue(9);
		assertEquals(var.getValue(), new Integer(9));
	}
}
