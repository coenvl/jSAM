/**
 * File CFLAgent.java
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
import nl.coenvl.sam.solvers.CFLSolver;
import nl.coenvl.sam.variables.Variable;

/**
 * CFLAgent
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 17 okt. 2014
 *
 */
public class CFLAgent extends LocalSolverAgent {

	private LocalInequalityConstraintCostFunction costFunction;

	public CFLAgent(String name, Variable<?> var) {
		super(name, var);

		this.costFunction = new LocalInequalityConstraintCostFunction(this);
		this.setSolver(new CFLSolver(this, this.costFunction), true);
	}

}
