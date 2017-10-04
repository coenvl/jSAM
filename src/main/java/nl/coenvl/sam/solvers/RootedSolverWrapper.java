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
public class RootedSolverWrapper<T extends Variable<V>, V> extends AbstractSolver<T, V> implements Solver {

    private static final String INIT_NEXT = "ROOTED_SOLVER:INIT_NEXT";
    private static final String COUNTFIELD = "currentCount";

    private final Solver wrappedSolver;
    private int counter;

    /**
     *
     */
    public RootedSolverWrapper(final Agent<T, V> agent, final Solver solver) {
        super(agent);
        this.wrappedSolver = solver;
        this.counter = 0;
    }

    @Override
    public void init() {
        if (this.isRoot()) {
            this.wrappedSolver.init();

            this.counter++;
            final Message initMessage = new HashMessage(this.myVariable.getID(), RootedSolverWrapper.INIT_NEXT);
            initMessage.put(RootedSolverWrapper.COUNTFIELD, this.counter);
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
        if (m.containsKey(RootedSolverWrapper.COUNTFIELD)) {
            final int mcount = (int) m.get(RootedSolverWrapper.COUNTFIELD);
            if (mcount > this.counter) {
                this.counter = mcount;
            } else {
                return; // Ignore the entire message
            }
        }

        if (m.getType().equals(RootedSolverWrapper.INIT_NEXT)) {
            this.wrappedSolver.init();
            super.sendToNeighbors(m); // Forward the message (the source will not be correct, but whatever...)
        } else {
            // Act as if we were not here...
            this.wrappedSolver.push(m);
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
