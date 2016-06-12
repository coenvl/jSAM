/**
 * File MultiSolverAgent.java
 *
 * This file is part of the jSAM project 2016.
 *
 * Copyright 2016 Coen van Leeuwen
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
 * MultiSolverAgent
 *
 * An agent that contains multiple solvers, in an attempt to utilize the effects of multiple solver types.
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 10 jun. 2016
 */
public class MultiSolverAgent<T extends Variable<V>, V> extends AbstractAgent<T, V> {

	private SolverRunner initSolver;
	private SolverRunner iterativeSolver;

	/**
	 * @param name
	 * @param var
	 */
	public MultiSolverAgent(T var, String name) {
		super(var, name);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nl.coenvl.sam.Agent#init()
	 */
	@Override
	public final synchronized void init() {
		this.startThread();

		if (this.initSolver != null) {
			this.initSolver.init();
		} else if (this.iterativeSolver != null) {
			this.iterativeSolver.init();
		} else {
			throw new RuntimeException("Either initSolver or IterativeSolver must be set!");
		}
	}

	/**
	 *
	 */
	private void startThread() {
		// Start the runner threads
		if (this.initSolver != null) {
			this.initSolver.startThread();
		}
		if (this.iterativeSolver != null) {
			this.iterativeSolver.startThread();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nl.coenvl.sam.Agent#push(nl.coenvl.sam.Message)
	 */
	@Override
	public final synchronized void push(Message m) {
		if (this.initSolver != null) {
			this.initSolver.push(m);
		}
		if (this.iterativeSolver != null) {
			this.iterativeSolver.push(m);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nl.coenvl.sam.solvers.Solver#tick()
	 */
	@Override
	public void tick() {
		if (this.iterativeSolver != null) {
			this.iterativeSolver.tick();
		} else {
			// Do nothing
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nl.coenvl.sam.Agent#reset()
	 */
	@Override
	public void reset() {
		super.reset();
		if (this.initSolver != null) {
			this.initSolver.reset();
		}
		if (this.iterativeSolver != null) {
			this.iterativeSolver.reset();
		}
	}

	public final void setInitSolver(Solver solver) {
		this.initSolver = new SolverRunner(solver);
	}

	public final void setIterativeSolver(Solver solver) {
		this.iterativeSolver = new SolverRunner(solver);
	}

	@Override
	public boolean isFinished() {
		return this.initSolver.emptyQueue() && this.iterativeSolver.emptyQueue();
	}

}
