/**
 * File LocalSolverAgent.java
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
package nl.coenvl.sam.agents;

import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.solvers.Solver;
import nl.coenvl.sam.solvers.SolverRunner;
import nl.coenvl.sam.variables.Variable;

/**
 * In another layer of hierarchy the AbstractSolverAgent provides the
 * functionality that any agent has that contains a solver. The function of this
 * class is to hide the Solving functionality from the rest of the agent.
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 11 apr. 2014
 * 
 */
public class SolverAgent<T extends Variable<V>, V> extends AbstractAgent<T, V> {

	private Solver<T,V> mySolver;
	
	/**
	 * @param name
	 * @param var
	 */
	public SolverAgent(T var, String name) {
		super(var, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.Agent#init()
	 */
	@Override
	public final synchronized void init() {
		this.mySolver.init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.Agent#push(nl.coenvl.sam.Message)
	 */
	@Override
	public final synchronized void push(Message m) {
		//if (Math.random() < AbstractSolverAgent.MESSAGE_SUCCESS_PROBABILITY)
		this.mySolver.push(m);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.Agent#reset()
	 */
	@Override
	public void reset() {
		super.reset();
		this.mySolver.reset();
	}

	public final void setSolver(Solver<T, V> solver) {
		this.mySolver = new SolverRunner<T,V>(solver);
	}

	public final void setSolver(Solver<T,V> solver, boolean asynchronous) {
		if (asynchronous)
			this.mySolver = new SolverRunner<T,V>(solver);
		else {
			// System.err.println("Warning: You are using a synchronous solver!");
			this.mySolver = solver;
		}
	}

}