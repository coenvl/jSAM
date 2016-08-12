/**
 * File MaxSumADFunctionSolver.java
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

import java.util.UUID;

import nl.coenvl.sam.MailMan;
import nl.coenvl.sam.agents.ConstraintAgent;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.variables.IntegerVariable;

/**
 * MaxSumADFunctionSolver
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 22 jan. 2016
 */
public class MaxSumADFunctionSolver extends MaxSumFunctionSolver {

	protected final static int REVERSE_AFTER_ITERS = 100;

	protected int iterCount;
	protected boolean direction;

	public MaxSumADFunctionSolver(ConstraintAgent<IntegerVariable, Integer> agent) {
		super(agent);
		this.iterCount = 0;
		this.direction = true;
	}

	/*
	 * A message sent from a function-node f to a variable-node x in iteration i includes for each possible value d \in
	 * Dx the minimal cost of any combination of assignments to the variables involved in f apart from x and the
	 * assignment of value d to variable x.
	 *
	 * @see nl.coenvl.sam.solvers.IterativeSolver#tick()
	 */
	@Override
	public synchronized void tick() {
		this.iterCount++;
		if ((this.iterCount % MaxSumADFunctionSolver.REVERSE_AFTER_ITERS) == 0) {
			this.direction = !this.direction;
		}

		// Only works for binary constraints
		assert (super.numNeighbors() == 2);

		for (UUID target : this.parent.getConstrainedVariableIds()) {
			if ((target.hashCode() > this.parent.hashCode()) == this.direction) {
				continue;
			}

			Message f2v = this.fun2varmessage(target);
			MailMan.sendMessage(target, f2v);
		}

		// this.receivedCosts.clear();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nl.coenvl.sam.solvers.BiPartiteGraphSolver#getCounterPart()
	 */
	@Override
	public Class<? extends BiPartiteGraphSolver> getCounterPart() {
		return MaxSumADVariableSolver.class;
	}

	@Override
	public void reset() {
		super.reset();
		this.iterCount = 0;
		this.direction = true;
	}

}
