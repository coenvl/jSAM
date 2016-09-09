/**
 * File RandomSolver.java
 *
 * This file is part of the jCoCoA project.
 *
 * Copyright 2016 Anonymous
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.anon.cocoa.solvers;

import org.anon.cocoa.agents.Agent;
import org.anon.cocoa.messages.Message;
import org.anon.cocoa.variables.Variable;

/**
 * RandomSolver
 *
 * @author Anomymous
 * @version 0.1
 * @since 12 feb. 2016
 */
public class RandomSolver<T> implements Solver {

	private final Variable<T> myVariable;

	public RandomSolver(Agent<? extends Variable<T>, T> agent) {
		this.myVariable = agent.getVariable();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.anon.cocoa.solvers.Solver#init()
	 */
	@Override
	public void init() {
		this.myVariable.setValue(this.myVariable.getRandomValue());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.anon.cocoa.solvers.Solver#push(org.anon.cocoa.messages.Message)
	 */
	@Override
	public void push(Message m) {
		// Nothing to do here
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.anon.cocoa.solvers.Solver#reset()
	 */
	@Override
	public void reset() {
		this.myVariable.clear();
	}

}
