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
 *       http://www.apache.org/licenses/LICENSE-2.0
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

import nl.coenvl.sam.exceptions.InvalidDomainException;
import nl.coenvl.sam.exceptions.InvalidValueException;
import nl.coenvl.sam.exceptions.VariableNotSetException;

/**
 * IntegerVariable
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 4 feb. 2014
 * 
 */
public final class IntegerVariable implements DiscreteVariable<Integer> {

	/**
	 * IntegerVariableIterator
	 * 
	 * @author leeuwencjv
	 * @version 0.1
	 * @since 7 feb. 2014
	 * 
	 */
	public class IntegerVariableIterator implements Iterator<Integer> {

		private final Integer upperBound;

		private int value;

		/**
		 * Create a new IntegerVariableIterator to
		 * 
		 * @param ref
		 */
		private IntegerVariableIterator(IntegerVariable ref) {
			this.value = ref.getLowerBound();
			this.upperBound = ref.upperBound;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return value <= upperBound;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Integer next() {
			if (!this.hasNext())
				throw new NoSuchElementException();

			return this.value++;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	private static final int UNDEFINED = Integer.MIN_VALUE;

	private static int unnamedVariableSequence = 0;

	private final int lowerBound;

	private final String name;

	private boolean set = false;

	private final int upperBound;

	private int value;

	/**
	 * Creates a variable with a given lower bound and upper bound. Future calls
	 * to setValue will never be able to set the Variable value to something
	 * higher or lower than these bounds' values
	 * 
	 * @param lowerBound
	 *            the lower bound of the variable domain
	 * @param upperBound
	 *            the lower bound of the variable domain
	 * 
	 * @throws InvalidDomainException
	 *             exception is the lower bound is higher than the upper bound
	 * 
	 * @see #IntegerVariable(Integer, Integer, String)
	 */
	public IntegerVariable(int lowerBound, int upperBound)
			throws InvalidDomainException {
		if (lowerBound > upperBound)
			throw new InvalidDomainException();

		this.name = "MyIntegerVariable" + unnamedVariableSequence++;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	/**
	 * Copy constructor
	 * 
	 * Creates a copy of the variable by taking only the upper and lower bound
	 * of the other variable.
	 * 
	 * @param variable
	 * 
	 *            public IntegerVariable(IntegerVariable other) { this.name =
	 *            other.name + " (Copy)"; this.lowerBound = other.lowerBound;
	 *            this.upperBound = other.upperBound; }
	 */

	/**
	 * Creates a variable with a given lower bound and upper bound. Future calls
	 * to setValue will never be able to set the Variable value to something
	 * higher or lower than these bounds' values. Also provide the variable with
	 * a name which can be used to identify it later.
	 * 
	 * @param lowerBound
	 *            the lower bound of the variable domain
	 * @param upperBound
	 *            the lower bound of the variable domain
	 * @param name
	 *            The name of the variable
	 * 
	 * @throws InvalidDomainException
	 *             exception is the lower bound is higher than the upper bound
	 * 
	 * @see #IntegerVariable(Integer, Integer)
	 */
	public IntegerVariable(int lowerBound, int upperBound, String name)
			throws InvalidDomainException {
		if (lowerBound > upperBound)
			throw new InvalidDomainException();

		this.name = name;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.Variable#clear()
	 */
	@Override
	public void clear() {
		set = false;
		value = IntegerVariable.UNDEFINED;
	}

	@Override
	public IntegerVariable clone() {
		IntegerVariable ret = null;
		try {
			ret = new IntegerVariable(lowerBound, upperBound, name + " (clone)");
			ret.value = this.value;
		} catch (InvalidDomainException e) {
			e.printStackTrace();
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.DiscreteVariable#getLowerBound()
	 */
	@Override
	public Integer getLowerBound() {
		return this.lowerBound;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.variables.Variable#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer getRandomValue() {
		return this.lowerBound + (int) Math.floor(getRange() * Math.random());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getRange() {
		return this.upperBound - this.lowerBound + 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.DiscreteVariable#getUpperBound()
	 */
	@Override
	public Integer getUpperBound() {
		return this.upperBound;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.Variable#getValue()
	 */
	@Override
	public synchronized Integer getValue() throws VariableNotSetException {
		if (!this.set)
			throw new VariableNotSetException();

		return this.value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.Variable#isSet()
	 */
	@Override
	public boolean isSet() {
		return set;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.DiscreteVariable#getIterator()
	 */
	@Override
	public IntegerVariableIterator iterator() {
		return new IntegerVariableIterator(this);
	}

	/**
	 * Set the value of this variable to the given value. If the value lies out
	 * of the domain, and InvalidValueException will be thrown
	 * 
	 * @param value
	 *            the value to be assigned to this variable
	 * @return the variable with the newly assigned value
	 * @throws InvalidValueException
	 */
	@Override
	public synchronized IntegerVariable setValue(Integer value)
			throws InvalidValueException {
		if (value < this.lowerBound || value > this.upperBound)
			throw new InvalidValueException(value);
		this.set = true;
		this.value = value;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (set)
			return "" + this.name + ": " + this.value;
		else
			return "" + this.name + ": (unset)";
	}
}
