/**
 * File UniqueFirstCooperativeSolver.java
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
package nl.coenvl.sam.solvers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import nl.coenvl.sam.MailMan;
import nl.coenvl.sam.agents.Agent;
import nl.coenvl.sam.messages.HashMessage;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.variables.AssignmentMap;
import nl.coenvl.sam.variables.CostMap;
import nl.coenvl.sam.variables.DiscreteVariable;
import nl.coenvl.sam.variables.RandomAccessVector;

/**
 * UniqueFirstCooperativeSolver
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 11 apr. 2014
 *
 */
public class CoCoASolver<V> extends AbstractSolver<DiscreteVariable<V>, V> implements Solver {

    protected static enum State {
        ACTIVE,
        DONE,
        HOLD,
        IDLE
    }

    public static final String ROOTNAME_PROPERTY = "isRoot";

    private static final String ASSIGN_VAR = "CoCoASolver:PickAVar";
    private static final String COST_MSG = "CoCoASolver:CostOfAssignments";
    private static final String CURRENT_STATE = "CoCoASolver:StateChanged";
    private static final String INQUIRE_MSG = "CoCoASolver:InquireAssignment";

    protected final AssignmentMap<State> neighborStates;
    private final AssignmentMap<V> context;

    private volatile State currentState;
    private volatile List<CostMap<V>> receivedMaps;

    private int uniquenessBound;

    public CoCoASolver(final Agent<DiscreteVariable<V>, V> agent) {
        super(agent);
        this.neighborStates = new AssignmentMap<>();
        this.context = new AssignmentMap<>();
        this.currentState = State.IDLE;
        this.uniquenessBound = 1;
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.Solver#init()
     */
    @Override
    public void init() {
        if (this.isRoot()) {
            this.push(new HashMessage(null, CoCoASolver.ASSIGN_VAR));
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.solvers.Solver#push(nl.coenvl.sam.messages.Message)
     */
    @Override
    public synchronized void push(final Message m) {

        if (m.containsKey("cpa")) {
            @SuppressWarnings("unchecked")
            final AssignmentMap<V> cpa = (AssignmentMap<V>) m.get("cpa");
            this.context.putAll(cpa);
        }

        if (m.getType().equals(CoCoASolver.ASSIGN_VAR)) {
            // Check if we are not currently busy or on hold, start
            if ((this.currentState == State.IDLE) || (this.currentState == State.HOLD)) {
                this.updateLocalState(State.ACTIVE);
                this.sendInquireMsgs();
            }
        } else if (m.getType().equals(CoCoASolver.INQUIRE_MSG)) {
            this.respond(m);
        } else if (m.getType().equals(CoCoASolver.COST_MSG)) {
            if (this.currentState != State.ACTIVE) {
                System.err.println("This never ought to happen!");
                return;
            }

            this.processCostMessage(m);
        } else if (m.getType().equals(CoCoASolver.CURRENT_STATE)) {
            this.updateRemoteState(m);
        } else {
            // System.err.println(this.getClass().getName() + ": Unexpected message of type " + m.getType());
            return;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.solvers.Solver#reset()
     */
    @Override
    public void reset() {
        super.reset();
        this.neighborStates.clear();
        this.context.clear();
        this.receivedMaps = null;
        this.currentState = State.IDLE;
    }

    protected synchronized void sendInquireMsgs() {
        // Create a map for storing incoming costmap messages
        this.receivedMaps = new ArrayList<>();

        final Message m = new HashMessage(this.myVariable.getID(), CoCoASolver.INQUIRE_MSG);
        m.put("cpa", this.context);
        m.put("domain", this.myVariable.getDomain());

        this.sendToNeighbors(m);
    }

    /**
     * Iterate over all possible assignments for the source
     *
     * @param m
     */
    private synchronized void respond(final Message m) {
        final CostMap<V> costMap = new CostMap<>();

        final UUID source = m.getSource();
        final AssignmentMap<V> pa = this.context.clone(); // Should by now already include the CPA of the source

        final Collection<V> domain = (Collection<V>) m.get("domain");
        // Build the cost map making the strong assumption that I have the same
        // type of variable as the source
        for (final V iterAssignment : domain) {
            pa.put(source, iterAssignment);

            Double iterCost = Double.MAX_VALUE;
            if (this.myVariable.isSet()) {
                iterCost = this.parent.getLocalCostIf(pa);
            } else {
                // Now the internal loop to optimize MY value
                final AssignmentMap<V> spa = pa.clone();

                for (final V siterAssignment : this.myVariable) {
                    spa.setAssignment(this.myVariable, siterAssignment);
                    final double siterCost = this.parent.getLocalCostIf(spa);

                    if (siterCost < iterCost) {
                        iterCost = siterCost;
                    }
                }
            }

            costMap.put(iterAssignment, iterCost);
        }

        // Respond to source
        final Message response = new HashMessage(this.myVariable.getID(), CoCoASolver.COST_MSG);
        response.put("costMap", costMap);
        response.put("cpa", this.context);

        MailMan.sendMessage(source, response);
    }

    /**
     * The neighbors will respond with cost Messages, in this function such a message is handled. If the cost message is
     * the last one to be received, continue picking one assignment by calling the {@link #pickValue()} function
     *
     * @param m
     */
    private synchronized void processCostMessage(final Message m) {
        @SuppressWarnings("unchecked")
        final CostMap<V> costMap = (CostMap<V>) m.get("costMap");
        this.receivedMaps.add(costMap);

        if (this.receivedMaps.size() >= this.numNeighbors()) {
            this.pickValue();
        }
    }

    /**
     * This function is called when all cost messages have arrived and I can now make a decision on how to assign the
     * variable
     */
    protected void pickValue() {
        // Gather all of the results and get the best assignment for me
        double bestCost = Double.MAX_VALUE;
        final RandomAccessVector<V> bestAssignment = new RandomAccessVector<>();

        // Create a problemContext to play around with to see which assignment is optimal
        final AssignmentMap<V> pa = this.context.clone();

        for (final V iterAssignment : this.myVariable) {
            // Sum the total cost of this partial assignment that all neighbors will incur
            double totalCost = 0;
            for (final CostMap<V> neighborMap : this.receivedMaps) {
                final Double neighborCost = neighborMap.get(iterAssignment);
                totalCost += neighborCost;
            }

            // Add my OWN cost
            pa.setAssignment(this.myVariable, iterAssignment);
            totalCost += this.parent.getLocalCostIf(pa);

            // Store the best value, and maintain it's uniqueness
            if (totalCost < bestCost) {
                bestCost = totalCost;
                bestAssignment.clear();
            }

            if (totalCost <= bestCost) {
                bestAssignment.add(iterAssignment);
            }
        }

        // See if we are ready to set this value
        if (bestAssignment.size() > this.uniquenessBound) {
            if (this.getActiveNeighbors() > 0) {
                this.updateLocalState(State.HOLD);
                return;
            }
        }

        // Set the value
        final V assign = bestAssignment.randomElement();

        this.myVariable.setValue(assign);
        this.context.setAssignment(this.myVariable, assign);

        this.updateLocalState(State.DONE);
        this.activateNeighbors();
    }

    /**
     * Update my state and inform my neighbors
     *
     * @param newState
     */
    private void updateLocalState(final State newState) {
        this.currentState = newState;

        final Message updateMessage = new HashMessage(this.myVariable.getID(), CoCoASolver.CURRENT_STATE);
        updateMessage.put("state", newState.name());
        updateMessage.put("cpa", this.context);

        this.sendToNeighbors(updateMessage);
    }

    private void updateRemoteState(final Message m) {
        final UUID source = m.getSource();
        final State newState = State.valueOf((String) m.get("state"));
        this.neighborStates.put(source, newState);

        if ((newState == State.HOLD) && (this.currentState == State.HOLD) && (this.getActiveNeighbors() < 1)) {
            this.receivedMaps.clear();
            this.uniquenessBound++;
            // System.err.println("Increasing the uniqueness bound!");
            this.updateLocalState(State.ACTIVE); // break the deadlock
            this.sendInquireMsgs();
        } else if ((newState == State.DONE) && (this.currentState == State.HOLD)) {
            this.receivedMaps.clear();
            this.updateLocalState(State.ACTIVE);
            this.sendInquireMsgs();
        } else if ((newState == State.DONE) && (this.currentState == State.DONE)) {
            this.activateNeighbors();
        }

    }

    /**
     * Send an activation message (ASSIGN_VAR) to the non-active neighbors
     */
    private void activateNeighbors() {
        final HashMessage nextMessage = new HashMessage(this.myVariable.getID(), CoCoASolver.ASSIGN_VAR);
        nextMessage.put("cpa", this.context);

        // Iterate over the set until we found a non-activated neighbor
        for (final UUID neighborid : this.parent.getConstrainedVariableIds()) {
            // neighbor.push(nextMessage);
            if (!this.neighborStates.containsKey(neighborid) || ((this.neighborStates.get(neighborid) != State.ACTIVE)
                    && (this.neighborStates.get(neighborid) != State.DONE))) {
                MailMan.sendMessage(neighborid, nextMessage);
                return;
            }
        }
    }

    /**
     * Subroutine to count the number of neighbors that are in an ACTIVE state
     *
     * @return the number of neighbors that are active
     */
    private int getActiveNeighbors() {
        int activeNeighbors = 0;

        for (final State neighborState : this.neighborStates.values()) {
            if (neighborState == State.ACTIVE) {
                activeNeighbors++;
            }
        }

        return activeNeighbors;
    }

    protected boolean isRoot() {
        return this.parent.has(CoCoSolver.ROOTNAME_PROPERTY) && (Boolean) this.parent.get(CoCoSolver.ROOTNAME_PROPERTY);
    }

}
