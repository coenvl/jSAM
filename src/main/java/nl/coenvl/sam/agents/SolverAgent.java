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
import nl.coenvl.sam.solvers.Solver;
import nl.coenvl.sam.solvers.SolverRunner;
import nl.coenvl.sam.variables.Variable;

/**
 * In another layer of hierarchy the SolverAgent provides the functionality that any agent has that contains a solver.
 * The function of this class is to hide the Solving functionality from the rest of the agent.
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 11 apr. 2014
 *
 */
public class SolverAgent<T extends Variable<V>, V> extends AbstractAgent<T, V> implements IterativeSolver {

    private SolverRunner mySolver;

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
        this.mySolver.startThread();
        this.mySolver.init();
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.Agent#push(nl.coenvl.sam.Message)
     */
    @Override
    public final synchronized void push(Message m) {
        this.mySolver.push(m);
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.solvers.Solver#tick()
     */
    @Override
    public void tick() {
        this.mySolver.tick();
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

    /**
     * Updates the solver of the Agent. If a solver was already running, it will try to stop that running solver and
     * update it with the new one. Note that the {@link Solver#init()} function is not called on the solver during this
     * process.
     *
     * @param solver The new solver to be used by this agent
     */
    public final void setSolver(Solver solver) {
        if (this.mySolver != null && this.mySolver.started()) {
            // If a solver was started, clean it up nicely first
            if (!this.mySolver.emptyQueue()) {
                try {
                    System.out.println("Warning queue is not empty of old solver!");
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.mySolver.stopThread();

            this.mySolver = new SolverRunner(solver);

            // And then start it again
            this.mySolver.startThread();
        } else {
            // If not, just set it
            this.mySolver = new SolverRunner(solver);
        }
    }

    @Deprecated
    public final void setSolver(Solver solver, boolean asynchronous) {
        if (asynchronous) {
            this.mySolver = new SolverRunner(solver);
        } else {
            // System.err.println("Warning: You are using a synchronous solver!");
            // this.mySolver = solver;
            throw new RuntimeException("Playtime is over");
        }
    }

    @Override
    public boolean isFinished() {
        return this.mySolver.emptyQueue();
    }

}
