/**
 * File TestAFBAgent.java
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

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.coenvl.sam.agents.Agent;
import nl.coenvl.sam.agents.OrderedSolverAgent;
import nl.coenvl.sam.agents.SolverAgent;
import nl.coenvl.sam.exceptions.InvalidDomainException;
import nl.coenvl.sam.exceptions.InvalidValueException;
import nl.coenvl.sam.solvers.DSASolver;
import nl.coenvl.sam.variables.IntegerVariable;

/**
 * TestAFBAgent
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 4 feb. 2014
 * 
 */
@SuppressWarnings("static-method")
public class TestAFBAgent {

	@Test
	public void test() throws InvalidValueException,
			InvalidDomainException {
		IntegerVariable var = new IntegerVariable(1, 10);
		SolverAgent<IntegerVariable,Integer> alice = new SolverAgent<IntegerVariable,Integer>(var, "Alice");
		alice.setSolver(new AFBSolver<IntegerVariable, Integer>());
		
		System.out.println(alice);
		alice.reset();
		assertTrue(true); // Just for formality

		System.out.println(alice);
	}

}
