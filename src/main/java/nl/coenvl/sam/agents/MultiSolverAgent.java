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
 * http://www.apache.org/licenses/LICENSE-2.0
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
import nl.coenvl.sam.solvers.IterativeSolver;
import nl.coenvl.sam.solvers.RootedIterativeSolverWrapper;
import nl.coenvl.sam.solvers.RootedSolverWrapper;
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
public class MultiSolverAgent<T extends Variable<V>, V> extends SolverAgent<T, V> {

    private Solver initSolver;
    private IterativeSolver iterativeSolver;

    /**
     * @param name
     * @param var
     */
    public MultiSolverAgent(final T var, final String name, final boolean synchronous, final boolean activation) {
        super(var, name, synchronous, activation);
    }

    /**
     * @param name
     * @param var
     */
    public MultiSolverAgent(final T var, final String name) {
        super(var, name);
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.Agent#init()
     */
    @Override
    public final synchronized void init() {
        // this.startThread();

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
    // public void startThread() {
    // if (this.synchronous) {
    // return;
    // }
    //
    // // Start the runner threads
    // if (this.initSolver != null) {
    // ((SolverRunner) this.initSolver).startThread();
    // }
    // if (this.iterativeSolver != null) {
    // ((SolverRunner) this.iterativeSolver).startThread();
    // }
    // }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.Agent#push(nl.coenvl.sam.Message)
     */
    @Override
    public final synchronized void push(final Message m) {
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

    public final void setInitSolver(final Solver solver) {
        if (solver == null) {
            this.initSolver = null;
        } else if (this.singleThreaded && !this.rootedActivation) {
            this.initSolver = solver;
        } else if (this.singleThreaded && this.rootedActivation) {
            this.initSolver = new RootedSolverWrapper<>(this, solver);
        } else if (!this.singleThreaded && !this.rootedActivation) {
            this.initSolver = new SolverRunner(solver);
        } else {
            this.initSolver = new SolverRunner(new RootedSolverWrapper<>(this, solver));
        }
    }

    public final void setIterativeSolver(final IterativeSolver solver) {
        if (solver == null) {
            this.iterativeSolver = null;
        } else if (this.singleThreaded && !this.rootedActivation) {
            this.iterativeSolver = solver;
        } else if (this.singleThreaded && this.rootedActivation) {
            this.iterativeSolver = new RootedIterativeSolverWrapper<>(this, solver);
        } else if (!this.singleThreaded && !this.rootedActivation) {
            this.iterativeSolver = new SolverRunner(solver);
        } else {
            this.iterativeSolver = new SolverRunner(new RootedIterativeSolverWrapper<>(this, solver));
        }
    }

    @Override
    public final void setSolver(final Solver solver) {
        if (solver instanceof IterativeSolver) {
            this.setIterativeSolver((IterativeSolver) solver);
        } else {
            this.setInitSolver(solver);
        }
    }

    @Override
    public boolean isFinished() {
        final boolean initSolverFinished = !(this.initSolver instanceof SolverRunner)
                || ((SolverRunner) this.initSolver).emptyQueue();
        final boolean iterSolverFinished = !(this.iterativeSolver instanceof SolverRunner)
                || ((SolverRunner) this.iterativeSolver).emptyQueue();
        return this.getVariable().isSet() && initSolverFinished && iterSolverFinished;
    }

}
