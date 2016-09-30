/**
 * File Variable.java
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

import java.util.UUID;

import nl.coenvl.sam.agents.PropertyOwner;
import nl.coenvl.sam.exceptions.InvalidValueException;
import nl.coenvl.sam.exceptions.VariableNotSetException;

/**
 * Variable
 *
 * @author leeuwencjv
 * @version 0.2
 * @since 6 feb. 2014
 */
public interface Variable<T> extends PropertyOwner {

    /**
     * <p>
     * Clears the variable
     * </p>
     *
     * <p>
     * Clearing the variable makes sure that the variable is assigned no value. Subsequent calls to {@link #getValue()}
     * will return {@code null} and calls to {@link #isSet()} will return false.
     * </p>
     */
    public void clear();

    /**
     * <p>
     * Clone a variable when needed to avoid aliasing to other agents' variables.
     * </p>
     *
     * @return A clone of the variable which is initially exactly identical, but a different object all together.
     */
    public Variable<T> clone();

    /**
     * <p>
     * Return the lower bound of the variable
     * </p>
     *
     * <p>
     * The lower bound is the lowest possible value that the variable can have. In other words for every possible value
     * {@code v} of the Variable, the following should evaluate true: {@code v >= lowerBound}
     * </p>
     *
     * @return The lower bound of the Variable
     */
    public T getLowerBound();

    public String getName();

    /**
     * Return a random value that the variable may attain
     *
     * @return
     */
    public T getRandomValue();

    /**
     * <p>
     * Return the upper bound of the variable
     * </p>
     *
     * <p>
     * The upper bound is the highest possible value that the variable can have. In other words for every possible value
     * {@code v} of the Variable, the following should evaluate true: {@code v <= upperBound}
     * </p>
     *
     * @return The upper bound of the Variable
     */
    public T getUpperBound();

    /**
     * <p>
     * Obtain the value of the variable
     * </p>
     *
     * <p>
     * If the variable is not set this will return null
     * </p>
     *
     * @return The current value of the Variable
     * @throws VariableNotSetException
     */
    public T getValue() throws VariableNotSetException;

    /**
     * <p>
     * Checks whether the Variable is assigned a value or not
     * </p>
     *
     * <p>
     * If no value is assigned to the Variable this function will return {@code false}, and {@code true} otherwise.
     * </p>
     *
     * @return A boolean indicating whether a value is set for this Variable
     */
    public boolean isSet();

    /**
     * @param value
     * @return
     * @throws InvalidValueException
     */
    public Variable<T> setValue(T value) throws InvalidValueException;

    /**
     * Returns the unique id of the variable. Can be used as the key for a value map.
     */
    public UUID getID();

}
