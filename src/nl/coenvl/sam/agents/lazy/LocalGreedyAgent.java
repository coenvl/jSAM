/**
 * File CooperativeAgent.java
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
package nl.coenvl.sam.agents.lazy;

import nl.coenvl.sam.agents.LocalSolverAgent;
import nl.coenvl.sam.costfunctions.LocalInequalityConstraintCostFunction;
import nl.coenvl.sam.solvers.GreedyLocalSolver;
import nl.coenvl.sam.variables.Variable;

/**
 * CooperativeAgent
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 11 apr. 2014
 * 
 */
public class LocalGreedyAgent extends LocalSolverAgent {

	private final LocalInequalityConstraintCostFunction costfun;

	/**
	 * @param name
	 * @param var
	 */
	public LocalGreedyAgent(String name, Variable<?> var) {
		super(name, var);

		this.costfun = new LocalInequalityConstraintCostFunction(this);
		this.setSolver(new GreedyLocalSolver(this, costfun), true);
	}

}
