/**
 * File SolverRunner.java
 *
 * This file is part of the jSAM project 2014.
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
package nl.coenvl.sam.solvers;

import nl.coenvl.sam.agents.Agent;
import nl.coenvl.sam.messages.HashMessage;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.variables.Variable;

/**
 * SolverRunner
 *
 * Wrapper for around a solver to make it run asynchronously
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 4 apr. 2014
 *
 */
public class RootedIterativeSolverWrapper<T extends Variable<V>, V> extends AbstractSolver<T, V>
        implements IterativeSolver {

    private static final String INIT_NEXT = "ROOTED_ITER_SOLVER:INIT_NEXT";
    private static final String TICK_NEXT = "ROOTED_ITER_SOLVER:TICK_NEXT";
    private static final String COUNTFIELD = "currentCount";

    private final IterativeSolver wrappedSolver;
    private int counter;

    /**
     *
     */
    public RootedIterativeSolverWrapper(final Agent<T, V> agent, final IterativeSolver solver) {
        super(agent);
        this.wrappedSolver = solver;
        this.counter = 0;
    }

    @Override
    public void init() {
        if (this.isRoot()) {
            this.wrappedSolver.init();

            this.counter++;
            final Message initMessage = new HashMessage(this.myVariable.getID(),
                    RootedIterativeSolverWrapper.INIT_NEXT);
            initMessage.put(RootedIterativeSolverWrapper.COUNTFIELD, this.counter);
            super.sendToNeighbors(initMessage);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.solvers.Solver#push(nl.coenvl.sam.messages.Message)
     */
    @Override
    public synchronized void push(final Message m) {
        if (m.containsKey(RootedIterativeSolverWrapper.COUNTFIELD)) {
            final int mcount = (int) m.get(RootedIterativeSolverWrapper.COUNTFIELD);
            if (mcount > this.counter) {
                this.counter = mcount;
            } else {
                return; // Ignore the entire message
            }
        }

        if (m.getType().equals(RootedIterativeSolverWrapper.INIT_NEXT)) {
            this.wrappedSolver.init();
            super.sendToNeighbors(m); // Forward the message (the source will not be correct, but whatever...)
        } else if (m.getType().equals(RootedIterativeSolverWrapper.TICK_NEXT)) {
            this.wrappedSolver.tick();
            super.sendToNeighbors(m);
        } else {
            // Act as if we were not here...
            this.wrappedSolver.push(m);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.solvers.IterativeSolver#tick()
     */
    @Override
    public void tick() {
        if (this.isRoot()) {
            this.wrappedSolver.tick();

            this.counter++;
            final Message tickMessage = new HashMessage(this.myVariable.getID(),
                    RootedIterativeSolverWrapper.TICK_NEXT);
            tickMessage.put(RootedIterativeSolverWrapper.COUNTFIELD, this.counter);
            super.sendToNeighbors(tickMessage);
        }
    }

    protected boolean isRoot() {
        return this.parent.has(CoCoSolver.ROOTNAME_PROPERTY) && (Boolean) this.parent.get(CoCoSolver.ROOTNAME_PROPERTY);
    }

    @Override
    public void reset() {
        super.reset();
        this.wrappedSolver.reset();
        this.counter = 0;
    }

}
