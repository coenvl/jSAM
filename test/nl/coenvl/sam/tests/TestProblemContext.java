/**
 * File TestProblemContext.java
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
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.HashMap;

import nl.coenvl.sam.agents.Agent;
import nl.coenvl.sam.agents.LocalSolverAgent;
import nl.coenvl.sam.exceptions.InvalidDomainException;
import nl.coenvl.sam.exceptions.InvalidValueException;
import nl.coenvl.sam.problemcontexts.IndexedProblemContext;
import nl.coenvl.sam.problemcontexts.LocalProblemContext;
import nl.coenvl.sam.variables.IntegerVariable;

import org.junit.Test;

/**
 * TestProblemContext
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 19 mrt. 2014
 * 
 */
public class TestProblemContext {

	private Integer testValue = 1;
	private Integer testIndex = 5;

	@Test
	public void testIndexedContext() throws InvalidValueException {
		IndexedProblemContext<Integer> pc = new IndexedProblemContext<Integer>(
				testIndex);

		pc.setValue(testValue);

		ArrayList<Integer> cpa = pc.getAssignment();
		assertEquals(testValue, cpa.get(testIndex));
		assertEquals(testValue, pc.getValue());

		IndexedProblemContext<Integer> pc2 = new IndexedProblemContext<Integer>(
				0);
		pc2.setAssignment(cpa);
		ArrayList<Integer> cpa2 = pc2.getAssignment();
		assertEquals(testValue, cpa2.get(testIndex));
		assertNotEquals(testValue, pc2.getValue());
	}

	@Test
	public void testLocalContext() throws InvalidValueException,
			InvalidDomainException {
		Agent testAgent = new LocalSolverAgent("TestAgent",
				new IntegerVariable(0, 10));
		LocalProblemContext<Integer> pc = new LocalProblemContext<Integer>(
				testAgent);
		
		pc.setValue(testValue);
		HashMap<Agent, Integer> cpa = pc.getAssignment();
		
		assertEquals(testValue, cpa.get(testAgent));
		assertEquals(testValue, pc.getValue());

		Agent neighborAgent = new LocalSolverAgent("NeighborAgent",
				new IntegerVariable(0, 10));
		LocalProblemContext<Integer> pc2 = new LocalProblemContext<Integer>(
				neighborAgent);
		
		pc2.setAssignment(cpa);
		HashMap<Agent, Integer> cpa2 = pc2.getAssignment();
		
		assertEquals(testValue, cpa2.get(testAgent));
		assertNotEquals(testValue, pc2.getValue());
	}

}
