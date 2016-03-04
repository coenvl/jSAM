/**
 * File RandomSolver.java
 * 
 * This file is part of the jSAM project.
 *
 * Copyright 2016 TNO
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
package nl.coenvl.sam.solvers;

import nl.coenvl.sam.agents.Agent;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.variables.DiscreteVariable;

/**
 * RandomSolver
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 12 feb. 2016
 */
public class RandomSolver<T extends DiscreteVariable<V>, V> implements Solver<T,V> {

	private final T myVariable;
	
	public RandomSolver(Agent<T, V> agent) {
		this.myVariable = agent.getVariable();
	}
	
	/* (non-Javadoc)
	 * @see nl.coenvl.sam.solvers.Solver#init()
	 */
	@Override
	public void init() {
		this.myVariable.setValue(this.myVariable.getRandomValue());
	}

	/* (non-Javadoc)
	 * @see nl.coenvl.sam.solvers.Solver#push(nl.coenvl.sam.messages.Message)
	 */
	@Override
	public void push(Message m) {
		// Nothing to do here
	}

	/* (non-Javadoc)
	 * @see nl.coenvl.sam.solvers.Solver#reset()
	 */
	@Override
	public void reset() {
		this.myVariable.clear();
	}

}
