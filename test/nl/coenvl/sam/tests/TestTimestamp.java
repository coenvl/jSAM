/**
 * File TestTimestamp.java
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
import nl.coenvl.sam.Timestamp;

import org.junit.Test;

/**
 * TestTimestamp
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 11 apr. 2014
 * 
 */
public class TestTimestamp {

	@Test
	public void test() {
		Timestamp one = new Timestamp();
		one.add(10);

		Timestamp other = new Timestamp();
		other.add(8);
		other.copy(one);

		assertEquals(one.compareTo(other), 0);

		one.add(6);
		assertEquals(one.compareTo(other), 1);
		assertEquals(other.compareTo(one), -1);

		other.add(3);
		assertEquals(one.compareTo(other), -1);
		assertEquals(other.compareTo(one), 1);
	}

}
