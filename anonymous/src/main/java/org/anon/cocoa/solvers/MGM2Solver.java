/**
 * File MGM2Solver.java
 *
 * Copyright 2014 Anomymous
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
package org.anon.cocoa.solvers;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.gson.Gson;

import org.anon.cocoa.MailMan;
import org.anon.cocoa.agents.Agent;
import org.anon.cocoa.exceptions.InvalidValueException;
import org.anon.cocoa.messages.HashMessage;
import org.anon.cocoa.messages.Message;
import org.anon.cocoa.variables.AssignmentMap;
import org.anon.cocoa.variables.IntegerVariable;
import org.anon.cocoa.variables.PublishableMap;
import org.anon.cocoa.variables.RandomAccessVector;

/**
 * MGM2Solver
 *
 * Based on the paper 'Distributed Algorithms for DCOP: A Graphical-Game-Based Approach' of Maheswaran, Pearce and
 * Tambe.
 *
 * @author Anomymous
 * @version 0.1
 * @since 2 april 2015
 *
 */
public class MGM2Solver extends AbstractSolver<IntegerVariable, Integer> implements IterativeSolver {

    /*
     * Note: For now I cannot make it generic since GSON won't properly encode the Offer lists.
     */

    /**
     * State
     *
     * @author Anomymous
     * @version 0.1
     * @since 10 apr. 2015
     *
     */
    public enum State {
        Value,
        Offer,
        AcceptReject,
        Gain,
        GoNoGo
    }

    private static final double OFFER_PROBABILITY = 0.5; // (Q in paper)
    // private static final double ACTIVATION_PROBABILITY = 0.5; // (P in paper)

    // private static final double EQUAL_UPDATE_PROBABILITY = 0.5;
    private static final String UPDATE_VALUE = "MGM2:UpdateValue";
    private static final String OFFER = "MGM2:MoveOffer";
    private static final String ACCEPT = "MGM2:AcceptOffer";
    private static final String GAIN = "MGM2:UtilityGain";
    private static final String GO = "MGM2:GO";

    private AssignmentMap<Integer> myProblemContext;
    private State algoState;
    private List<Offer> receivedOffers;
    private AssignmentMap<Double> neighborGains;
    private boolean isOfferer;
    private Offer committedOffer;
    private Integer bestLocalAssignment;
    private double bestLocalReduction;

    public MGM2Solver(Agent<IntegerVariable, Integer> agent) {
        super(agent);

        this.algoState = State.Value;
        this.myProblemContext = new AssignmentMap<>();
        this.receivedOffers = new LinkedList<>();
        this.neighborGains = new AssignmentMap<>();
    }

    @Override
    public synchronized void init() {
        this.myVariable.setValue(this.myVariable.getRandomValue());
    }

    @Override
    public synchronized void push(Message m) {
        final UUID source = m.getSource();

        if (m.getType().equals(MGM2Solver.UPDATE_VALUE)) {
            final Integer value = m.getInteger("value");

            this.myProblemContext.put(source, value);
        } else if (m.getType().equals(MGM2Solver.OFFER)) {

            // Any OFFER message should contain this...
            if (m.containsKey("offers")) {
                @SuppressWarnings("unchecked")
                Set<String> jsonOffers = (Set<String>) m.getMap("offers").keySet();
                for (String json : jsonOffers) {
                    Offer o = (new Gson()).fromJson(json, Offer.class);
                    this.receivedOffers.add(o);
                }
            }

        } else if (m.getType().equals(MGM2Solver.ACCEPT)) {

            this.committedOffer = MGM2Solver.fromJson(m.get("offer"));

        } else if (m.getType().equals(MGM2Solver.GAIN)) {

            // Any ACCEPT message should contain this...
            final Double gain = m.getDouble("gain");
            this.neighborGains.put(source, gain);

        } else if (m.getType().equals(MGM2Solver.GO)) {

            if (this.committedOffer == null) {
                System.err.println("Warning, something stinky is going on!");
                return;
            }

            if (this.isOfferer) {
                assert (source.equals(this.committedOffer.receiver));
                assert (this.myVariable.getID().equals(this.committedOffer.offerer));

                // System.out.println(this.parent.getName() + " is go as offerer");

                this.myVariable.setValue(this.committedOffer.offererValue);
            } else {
                assert (source.equals(this.committedOffer.offerer));
                assert (this.myVariable.getID().equals(this.committedOffer.receiver));

                // System.out.println(this.parent.getName() + " is go as receiver");

                this.myVariable.setValue(this.committedOffer.receiverValue);
            }

            this.committedOffer = null;

        } else {
            // throw new RuntimeException("Unexpected message: " + m.getType());
        }
    }

    @Override
    public synchronized void tick() {
        switch (this.algoState) {
        case Offer:
            this.sendOffer();
            this.algoState = State.AcceptReject;
            break;

        case AcceptReject:
            this.sendAccept();
            this.algoState = State.Gain;
            break;

        case Gain:
            this.sendGain();
            this.algoState = State.GoNoGo;
            break;

        case GoNoGo:
            this.sendGo();
            this.algoState = State.Value;
            break;

        default:
        case Value:
            this.sendValue();
            this.committedOffer = null;
            this.algoState = State.Offer;
            break;
        }
    }

    /**
     * This function is exactly the same as MGMSolver.sendValue()
     */
    private void sendValue() {
        final Message updateMsg = new HashMessage(this.myVariable.getID(), MGM2Solver.UPDATE_VALUE);
        updateMsg.put("value", this.myVariable.getValue());

        this.sendToNeighbors(updateMsg);
    }

    /**
     * Decides IF this neighbor is an offerer or a receiver. If we are an offerer: send a list of offers to the neighbor
     * with local cost reductions. If receiver, do nothing.
     */
    private void sendOffer() {
        // First determine whether we will offer or receive
        if (Math.random() > MGM2Solver.OFFER_PROBABILITY) {
            this.isOfferer = true;

            // Select a random neighbor
            RandomAccessVector<UUID> list = new RandomAccessVector<>();
            list.addAll(this.parent.getConstrainedVariableIds());
            UUID neighbor = list.randomElement();

            assert (neighbor != null);

            // Get all offers that reduce the local cost
            PublishableMap<String, Integer> offerList = new PublishableMap<>();

            this.myProblemContext.setAssignment(this.myVariable, this.myVariable.getValue());
            double before = this.parent.getLocalCostIf(this.myProblemContext);

            AssignmentMap<Integer> temp = this.myProblemContext.clone();
            for (Integer i : this.myVariable) {
                temp.setAssignment(this.myVariable, i);
                for (Integer j : this.myVariable) {
                    temp.put(neighbor, j);
                    double val = this.parent.getLocalCostIf(temp);
                    if (val < before) {
                        Offer o = new Offer(this.myVariable.getID(), neighbor, i, j, before - val);
                        offerList.put((new Gson()).toJson(o), i);
                    }
                }
            }

            // Send the offers to the randomly selected neighbor
            Message offerMessage = new HashMessage(this.myVariable.getID(), MGM2Solver.OFFER);
            offerMessage.put("offers", offerList);

            MailMan.sendMessage(neighbor, offerMessage);
        } else {
            this.isOfferer = false;
        }

    }

    /**
     *
     */
    private void sendAccept() {
        // Only if this Agent did not offer to anyone else...
        if (!this.isOfferer) {
            // Get current costs
            double before = this.parent.getLocalCostIf(this.myProblemContext);

            AssignmentMap<Integer> temp;

            Offer bestOffer = null;
            double bestGain = Double.MIN_VALUE;
            for (Offer suggestedOffer : this.receivedOffers) {
                temp = this.myProblemContext.clone();

                assert (suggestedOffer.receiver.equals(this.myVariable.getID()));

                temp.put(suggestedOffer.offerer, suggestedOffer.offererValue);
                temp.put(suggestedOffer.receiver, suggestedOffer.receiverValue);

                double val = this.parent.getLocalCostIf(temp);
                suggestedOffer.receiverReduction = before - val;
                double globalReduction = MGM2Solver.computeGlobalGain(suggestedOffer.offererReduction,
                        suggestedOffer.receiverReduction);
                if ((globalReduction > 0) && (globalReduction > bestGain)) {
                    bestGain = globalReduction;
                    bestOffer = suggestedOffer;
                }
            }

            // Send accept if there is a global reduction
            if (bestOffer != null) {
                Message accept = new HashMessage(this.myVariable.getID(), MGM2Solver.ACCEPT);
                accept.put("offer", bestOffer.toJson());

                // System.out.println(this.parent.getName() + " accepts offer from " + bestOffer.offerer.getName());
                // bestOffer.offerer.push(accept);
                MailMan.sendMessage(bestOffer.offerer, accept);

                // Set the value now
                this.committedOffer = bestOffer;
                this.myVariable.setValue(bestOffer.receiverValue);
            }
        }

        // Clear offers for the next round
        this.receivedOffers.clear();
    }

    private void sendGain() {
        // System.out.println(this.parent.getName() + " sending gain messages");
        Message gainMessage = new HashMessage(this.myVariable.getID(), MGM2Solver.GAIN);

        if (this.committedOffer != null) {

            this.bestLocalReduction = MGM2Solver.computeGlobalGain(this.committedOffer.offererReduction,
                    this.committedOffer.receiverReduction);
            gainMessage.put("gain", this.bestLocalReduction);

        } else {
            this.myProblemContext.setAssignment(this.myVariable, this.myVariable.getValue());
            double before = this.parent.getLocalCostIf(this.myProblemContext);

            AssignmentMap<Integer> temp = this.myProblemContext.clone();

            double bestCost = before; // Double.MAX_VALUE;
            Integer bestAssignment = null;

            for (Integer assignment : this.myVariable) {
                temp.setAssignment(this.myVariable, assignment);

                double localCost = this.parent.getLocalCostIf(temp);

                if (localCost < bestCost) {
                    bestCost = localCost;
                    bestAssignment = assignment;
                }
            }

            this.bestLocalReduction = before - bestCost;
            this.bestLocalAssignment = bestAssignment;

            gainMessage.put("gain", this.bestLocalReduction);
        }

        this.sendToNeighbors(gainMessage);
    }

    private void sendGo() {
        if (this.committedOffer == null) {
            // If there was no better solution skip this step
            if (this.bestLocalAssignment == null) {
                return;
            }

            Double bestNeighborReduction = Double.MIN_VALUE;
            UUID bestNeighbor = null;
            for (UUID id : this.parent.getConstrainedVariableIds()) {
                if (this.neighborGains.containsKey(id) && (this.neighborGains.get(id) > bestNeighborReduction)) {
                    bestNeighborReduction = this.neighborGains.get(id);
                    bestNeighbor = id;
                }
            }

            // If this solution is better than any of the neighbors, do the
            // update
            try {
                if (this.bestLocalReduction > bestNeighborReduction) {
                    this.myVariable.setValue(this.bestLocalAssignment);
                }
                if ((this.bestLocalReduction == bestNeighborReduction)
                        && (this.myVariable.getID().compareTo(bestNeighbor) < 0)) {
                    this.myVariable.setValue(this.bestLocalAssignment);
                }
            } catch (InvalidValueException e) {
                e.printStackTrace();
            }

        } else {
            UUID partner;
            if (this.isOfferer) {
                assert (this.committedOffer.offerer == this.myVariable.getID());
                partner = this.committedOffer.receiver;
            } else {
                assert (this.committedOffer.receiver == this.myVariable.getID());
                partner = this.committedOffer.offerer;
            }

            Double bestNeighborReduction = Double.MIN_VALUE;
            for (UUID id : this.parent.getConstrainedVariableIds()) {
                if ((id != partner) && (this.neighborGains.get(id) > bestNeighborReduction)) {
                    bestNeighborReduction = this.neighborGains.get(id);
                }
            }

            if (this.bestLocalReduction > bestNeighborReduction) {
                Message goMessage = new HashMessage(this.myVariable.getID(), MGM2Solver.GO);
                MailMan.sendMessage(partner, goMessage);
            }

            // committedOffer = null;
        }

        this.neighborGains.clear();
    }

    @Override
    public void reset() {
        this.myVariable.clear();

        this.algoState = State.Value;
        this.myProblemContext.clear();
        this.receivedOffers.clear();
        this.neighborGains.clear();
    }

    private static double computeGlobalGain(double localGain, double remoteGain) {
        return (localGain + remoteGain) - Math.abs(localGain - remoteGain);
    }

    public static Offer fromJson(String str) {
        return (new Gson()).fromJson(str, Offer.class);
    }

    private class Offer {
        public final UUID offerer;
        public final UUID receiver;

        public final Integer offererValue;
        public final Integer receiverValue;

        public final double offererReduction;
        public double receiverReduction;

        public Offer(UUID offerer, UUID receiver, Integer i, Integer j, Double offererReduction) {
            this.offerer = offerer;
            this.receiver = receiver;
            this.offererValue = i;
            this.receiverValue = j;
            this.offererReduction = offererReduction;
        }

        public String toJson() {
            return (new Gson()).toJson(this);
        }
    }

}
