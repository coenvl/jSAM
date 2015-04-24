/**
 * File AFBAgent.java
 *
 * This file is part of the DCOP project 2014.
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

import nl.coenvl.sam.agents.InequalityConstraintSolvingAgent;
import nl.coenvl.sam.agents.OrderedSolverAgent;
import nl.coenvl.sam.costfunctions.InequalityConstraintCostFunction;
import nl.coenvl.sam.solvers.FBSolver;
import nl.coenvl.sam.variables.IntegerVariable;

/**
 * AFBAgent
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 4 feb. 2014
 * 
 */
@Deprecated
public final class AFBAgent extends OrderedSolverAgent implements
		InequalityConstraintSolvingAgent {

	private InequalityConstraintCostFunction costFunction;

	/**
	 * @param name
	 */
	public AFBAgent(String name, IntegerVariable var) {
		super(name, var);

		this.costFunction = new InequalityConstraintCostFunction(
				this.getSequenceID());
		this.setSolver(new FBSolver(this, this.costFunction), true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.IntegerInequalitySolvingAgent#addConstraint(int)
	 */
	@Override
	public void addConstraint(int constraintIdx) {
		this.costFunction.addConstraintIndex(constraintIdx);
	}
}
