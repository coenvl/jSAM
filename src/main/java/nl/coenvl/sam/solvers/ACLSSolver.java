/**
 * File ACLSSolver.java
 *
 * This file is part of the jSAM project.
 *
 * Copyright 2015 TNO
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

import java.util.Random;
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
 * ACLSSolver
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 11 dec. 2015
 */
public class ACLSSolver<V> extends AbstractSolver<DiscreteVariable<V>, V> implements IterativeSolver {

    protected enum State {
        SENDVALUE,
        PROPOSE,
        ASSIGN
    }

    // protected static final double UPDATE_PROBABILITY = 0.1;

    protected static final String UPDATE_VALUE = "ACLS:UpdateValue";
    protected static final String PROPOSED_UPDATE = "ACLS:ProposedUpdateValue";
    protected static final String IMPACT_MESSAGE = "ACLS:ProposalImpact";

    private final AssignmentMap<V> myProblemContext;
    private final CostMap<UUID> impactCosts;
    private State currentState;
    protected V myProposal;

    public ACLSSolver(final Agent<DiscreteVariable<V>, V> agent) {
        super(agent);
        this.myProblemContext = new AssignmentMap<>();
        this.impactCosts = new CostMap<>();
        this.currentState = State.SENDVALUE;
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.solvers.Solver#init()
     */
    @Override
    public synchronized void init() {
        this.myVariable.setValue(this.myVariable.getRandomValue());
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.solvers.Solver#push(nl.coenvl.sam.messages.Message)
     */
    @Override
    public synchronized void push(final Message m) {
        final UUID source = m.getSource();

        if (m.getType().equals(ACLSSolver.UPDATE_VALUE)) {
            @SuppressWarnings("unchecked")
            final V value = (V) m.get("value");
            this.myProblemContext.put(source, value);
        } else if (m.getType().equals(ACLSSolver.PROPOSED_UPDATE)) {
            this.replyWithLocalCost(m);
        } else if (m.getType().equals(ACLSSolver.IMPACT_MESSAGE)) {
            this.impactCosts.put(source, (Double) m.get("costImpact"));
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.solvers.IterativeSolver#tick()
     */
    @Override
    public synchronized void tick() {
        switch (this.currentState) {
        case PROPOSE:
            this.proposeAssignment();
            this.currentState = State.ASSIGN;
            break;

        case ASSIGN:
            this.decideAssignment();
            this.currentState = State.SENDVALUE;
            break;

        default:
        case SENDVALUE:
            this.sendValue();
            this.currentState = State.PROPOSE;
            break;
        }
    }

    private void sendValue() {
        this.impactCosts.clear();
        final Message updateMsg = new HashMessage(this.myVariable.getID(), ACLSSolver.UPDATE_VALUE);

        updateMsg.put("value", this.myVariable.getValue());

        super.sendToNeighbors(updateMsg);
    }

    /**
     *
     */
    protected void proposeAssignment() {
        // Compute local reductions
        final AssignmentMap<V> temp = this.myProblemContext.clone();
        temp.setAssignment(this.myVariable, this.myVariable.getValue());

        final double currentCost = this.parent.getLocalCostIf(temp);

        final RandomAccessVector<V> improvementSet = new RandomAccessVector<>();
        for (final V i : this.myVariable) {
            temp.setAssignment(this.myVariable, i);
            final double val = this.parent.getLocalCostIf(temp);
            if (val < currentCost) {
                improvementSet.add(i);
            }
        }

        // Determine the proposal for this round
        if (improvementSet.isEmpty()) {
            this.myProposal = null;
        } else {
            this.myProposal = improvementSet.randomElement(); // .toString();
        }

        // Send the proposal to all neighbors
        final Message updateMsg = new HashMessage(this.myVariable.getID(), ACLSSolver.PROPOSED_UPDATE);

        updateMsg.put("source", this.myVariable.getID());
        updateMsg.put("proposal", this.myProposal);

        super.sendToNeighbors(updateMsg);
    }

    /**
     * @param m
     */
    private void replyWithLocalCost(final Message m) {
        // Compute current cost
        final UUID neighbor = m.getSource();
        final V proposal = (V) m.get("proposal");
        double impact;

        if (proposal == null) { // .isEmpty()) {
            impact = 0.;
        } else {
            final AssignmentMap<V> temp = this.myProblemContext.clone();
            temp.setAssignment(this.myVariable, this.myVariable.getValue());

            final double currentCost = this.parent.getLocalCostIf(temp);

            // Compute cost after update
            // @SuppressWarnings("unchecked")
            // final V proposedValue = (V) Double.valueOf(proposal);
            temp.put(neighbor, proposal);

            impact = this.parent.getLocalCostIf(temp) - currentCost;
        }

        // And send back impact such that negative impact means improvement
        final Message impactMsg = new HashMessage(this.myVariable.getID(), ACLSSolver.IMPACT_MESSAGE);
        impactMsg.put("costImpact", impact);
        MailMan.sendMessage(neighbor, impactMsg);
    }

    /**
     *
     */
    private void decideAssignment() {
        if (this.myProposal == null) { // .isEmpty()) {
            return;
        }

        final AssignmentMap<V> temp = this.myProblemContext.clone();
        temp.setAssignment(this.myVariable, this.myVariable.getValue());

        final double currentCost = this.parent.getLocalCostIf(temp);

        // @SuppressWarnings("unchecked")
        final V proposedValue = this.myProposal; // (V) Double.valueOf(this.myProposal);
        temp.setAssignment(this.myVariable, proposedValue);

        double totalImpact = this.parent.getLocalCostIf(temp) - currentCost;

        for (final double impact : this.impactCosts.values()) {
            totalImpact += impact;
        }

        if ((totalImpact < 0) && ((new Random()).nextDouble() < this.getUpdateProbability())) {
            this.myVariable.setValue(proposedValue);
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
        this.myProblemContext.clear();
        this.impactCosts.clear();
        this.currentState = State.SENDVALUE;
        this.myProposal = null;
    }

}
