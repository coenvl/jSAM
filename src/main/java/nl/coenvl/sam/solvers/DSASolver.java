/**
 * File DSASolver.java
 *
 * This file is part of the jSAM project.
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
 */
package nl.coenvl.sam.solvers;

import java.util.UUID;

import nl.coenvl.sam.agents.Agent;
import nl.coenvl.sam.messages.HashMessage;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.variables.AssignmentMap;
import nl.coenvl.sam.variables.DiscreteVariable;
import nl.coenvl.sam.variables.RandomAccessVector;

/**
 * DSASolver
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 11 dec. 2014
 *
 */
public class DSASolver<V> extends AbstractSolver<DiscreteVariable<V>, V> implements IterativeSolver {

    private enum State {
        SENDVALUE,
        PICKVALUE
    }

    public static final double CHANGE_TO_EQUAL_PROB = 0.5;
    public static final double CHANGE_TO_IMPROVE_PROB = 0.5;

    public static final String UPDATE_VALUE = "DSASolver:Value";
    public static final String KEY_VARVALUE = "value";

    private final AssignmentMap<V> context;

    private boolean sendUpdate;

    private State state;

    /**
     * @param dsaAgent
     * @param costfun
     */
    public DSASolver(final Agent<DiscreteVariable<V>, V> agent) {
        super(agent);
        this.context = new AssignmentMap<>();
        this.state = State.SENDVALUE;
        this.sendUpdate = true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.anon.cocoa.solvers.Solver#init()
     */
    @Override
    public synchronized void init() {
        this.myVariable.setValue(this.myVariable.getRandomValue());
        this.sendUpdate = true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.anon.cocoa.solvers.Solver#push(org.anon.cocoa.messages.Message)
     */
    @Override
    public synchronized void push(final Message m) {
        if (m.getType().equals(DSASolver.UPDATE_VALUE)) {
            final UUID varId = m.getSource();

            @SuppressWarnings("unchecked")
            final V newValue = (V) m.getNumber(DSASolver.KEY_VARVALUE);

            this.context.put(varId, newValue);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.anon.cocoa.solvers.Solver#reset()
     */
    @Override
    public void reset() {
        super.reset();
        this.context.clear();
    }

    /**
     *
     */
    @Override
    public synchronized void tick() {
        switch (this.state) {
        case PICKVALUE:
            this.pickValue();
            this.state = State.SENDVALUE;
            break;

        default:
        case SENDVALUE:
            this.updateMyValue();
            this.state = State.PICKVALUE;
            break;
        }
    }

    public void pickValue() {
        double bestCost = Double.MAX_VALUE;

        final RandomAccessVector<V> bestAssignment = new RandomAccessVector<>();
        this.context.setAssignment(this.myVariable, this.myVariable.getValue());
        final double oldCost = this.parent.getLocalCostIf(this.context);

        for (final V value : this.myVariable) {
            this.context.setAssignment(this.myVariable, value);

            final double localCost = this.parent.getLocalCostIf(this.context);

            if (localCost < bestCost) {
                bestCost = localCost;
                bestAssignment.clear();
            }

            if (localCost <= bestCost) {
                bestAssignment.add(value);
            }
        }

        if (bestCost > oldCost) {
            return;
        }

        if ((bestCost == oldCost) && (Math.random() > DSASolver.CHANGE_TO_EQUAL_PROB)) {
            return;
        }

        if ((bestCost < oldCost) && (Math.random() > DSASolver.CHANGE_TO_IMPROVE_PROB)) {
            return;
        }

        // Chose any of the "best" assignments
        final V assign = bestAssignment.randomElement();

        if (assign != this.myVariable.getValue()) {
            this.myVariable.setValue(assign);
            this.sendUpdate = true;
        } else {
            this.sendUpdate = false;
        }
    }

    /**
     * @param assign
     */
    private void updateMyValue() {
        if (this.sendUpdate) {
            final HashMessage nextMessage = new HashMessage(this.myVariable.getID(), DSASolver.UPDATE_VALUE);
            nextMessage.put(DSASolver.KEY_VARVALUE, this.myVariable.getValue());

            this.sendToNeighbors(nextMessage);
        }
    }

}
