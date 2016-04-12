/**
 * File DiscreteVariable.java
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

/**
 * DiscreteVariable
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 6 feb. 2014
 *
 */
public interface DiscreteVariable<V> extends Variable<V>, Iterable<V> {

	/**
	 * Returns the range of the Discrete Variable, i.e. the number of possible values it may attain.
	 *
	 * @return An Integer representing the number of values this variable may attain
	 */
	public int getRange();

	/**
	 * <p>
	 * Get an iterator to run through all possible values the variable can attain
	 * </p>
	 *
	 * <p>
	 * The iterator will initially refer to a DiscreteVariable with a value equal to the lower bound of the variable if
	 * the variable is not set, and equal to the value of the variable if it is set. For each call to the
	 * {@linkplain java.util.Iterator#next} call it will give the next possible value until it is equal to the upper
	 * bound. After that {@linkplain java.util.Iterator#hasNext} will return false.
	 * </p>
	 *
	 * @return an {@linkplain Iterator} to the first possible value of the DiscreteVariable
	 */
	@Override
	public Iterator<V> iterator();

}
