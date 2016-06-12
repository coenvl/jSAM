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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.coenvl.sam.solvers.onetick;

import java.util.Random;
import java.util.UUID;

import nl.coenvl.sam.MailMan;
import nl.coenvl.sam.agents.Agent;
import nl.coenvl.sam.messages.HashMessage;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.solvers.AbstractSolver;
import nl.coenvl.sam.solvers.IterativeSolver;
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
@Deprecated
public class ACLSSolver<V> extends AbstractSolver<DiscreteVariable<V>, V> implements IterativeSolver {

	private static final String UPDATE_VALUE = "ACLS:UpdateValue";
	private static final String PROPOSED_UPDATE = "ACLS:ProposedUpdateValue";
	private static final String IMPACT_MESSAGE = "ACLS:ProposalImpact";
	private static final double UPDATE_PROBABILITY = 0.5;

	private final AssignmentMap<V> myProblemContext;
	private final AssignmentMap<V> neighborValues;
	private final CostMap<UUID> impactCosts;
	private String myProposal;

	public ACLSSolver(Agent<DiscreteVariable<V>, V> agent) {
		super(agent);
		this.myProblemContext = new AssignmentMap<>();
		this.neighborValues = new AssignmentMap<>();
		this.impactCosts = new CostMap<>();
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
	public synchronized void push(Message m) {
		final UUID source = m.getUUID("source");

		if (m.getType().equals(ACLSSolver.UPDATE_VALUE)) {
			@SuppressWarnings("unchecked")
			final V value = (V) m.getInteger("value");
			this.myProblemContext.put(source, value);
			this.neighborValues.put(source, value);

			if (this.neighborValues.size() >= super.numNeighbors()) {
				// Clear any message that will come the NEXT iteration
				this.impactCosts.clear();
				this.proposeAssignment();
			}
		} else if (m.getType().equals(ACLSSolver.PROPOSED_UPDATE)) {
			this.replyWithLocalCost(m);
		} else if (m.getType().equals(ACLSSolver.IMPACT_MESSAGE)) {
			if (!this.myProposal.isEmpty()) {
				final Double impact = m.getDouble("costImpact");
				this.impactCosts.put(source, impact);

				if (this.impactCosts.size() >= super.numNeighbors()) {
					// Clear any message that will come the NEXT iteration
					this.neighborValues.clear();
					this.decideAssignment();
				}
			}

		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nl.coenvl.sam.solvers.IterativeSolver#tick()
	 */
	@Override
	public synchronized void tick() {
		Message updateMsg = new HashMessage(ACLSSolver.UPDATE_VALUE);

		updateMsg.put("source", this.myVariable.getID());
		updateMsg.put("value", this.myVariable.getValue());

		super.sendToNeighbors(updateMsg);
	}

	/**
	 *
	 */
	private void proposeAssignment() {
		// Compute local reductions
		// this.myProblemContext = this.neighborValues.clone();
		AssignmentMap<V> temp = this.neighborValues.clone();
		temp.setAssignment(this.myVariable, this.myVariable.getValue());

		double currentCost = this.parent.getLocalCostIf(temp);

		// AssignmentMap<V> temp = this.myProblemContext.clone();
		// LocalProblemContext<Integer> temp = new LocalProblemContext<Integer>(this.parent);
		// temp.setAssignment(this.myProblemContext.getAssignment());

		// ArrayList<Integer> improvementSet = new ArrayList<Integer>();
		RandomAccessVector<V> improvementSet = new RandomAccessVector<>();
		for (V i : this.myVariable) {
			temp.setAssignment(this.myVariable, i);
			double val = this.parent.getLocalCostIf(temp);
			if (val < currentCost) {
				improvementSet.add(i);
			}
		}

		// Determine the proposal for this round
		if (improvementSet.isEmpty()) {
			this.myProposal = "";
		} else {
			this.myProposal = improvementSet.randomElement().toString();
		}

		// Send the proposal to all neighbors
		Message updateMsg = new HashMessage(ACLSSolver.PROPOSED_UPDATE);

		updateMsg.put("source", this.myVariable.getID());
		updateMsg.put("proposal", this.myProposal);

		super.sendToNeighbors(updateMsg);
	}

	/**
	 * @param m
	 */
	private void replyWithLocalCost(Message m) {
		// Compute current cost
		final UUID neighbor = m.getUUID("source");
		final String proposal = m.get("proposal");
		double impact;

		if (proposal.isEmpty()) {
			impact = 0.;
		} else {
			// LocalProblemContext<Integer> temp = new LocalProblemContext<Integer>(this.parent);
			// temp.setAssignment(this.myProblemContext.getAssignment());
			AssignmentMap<V> temp = this.myProblemContext.clone();
			temp.setAssignment(this.myVariable, this.myVariable.getValue());

			double currentCost = this.parent.getLocalCostIf(temp);

			// Compute cost after update
			@SuppressWarnings("unchecked")
			V proposedValue = (V) Integer.valueOf(proposal);
			temp.put(neighbor, proposedValue);

			impact = this.parent.getLocalCostIf(temp) - currentCost;
		}

		// And send back impact such that negative impact means improvement
		Message impactMsg = new HashMessage(ACLSSolver.IMPACT_MESSAGE);

		impactMsg.put("source", this.myVariable.getID());
		impactMsg.put("costImpact", impact);
		MailMan.sendMessage(neighbor, impactMsg);
	}

	/**
	 *
	 */
	private void decideAssignment() {
		// LocalProblemContext<Integer> temp = new LocalProblemContext<Integer>(this.parent);
		// temp.setAssignment(this.myProblemContext.getAssignment());
		AssignmentMap<V> temp = this.neighborValues.clone(); // this.myProblemContext.clone();
		temp.setAssignment(this.myVariable, this.myVariable.getValue());

		double currentCost = this.parent.getLocalCostIf(temp);

		@SuppressWarnings("unchecked")
		V proposedValue = (V) Integer.valueOf(this.myProposal);
		temp.setAssignment(this.myVariable, proposedValue);

		double totalImpact = this.parent.getLocalCostIf(temp) - currentCost;

		for (double impact : this.impactCosts.values()) {
			totalImpact += impact;
		}

		if (totalImpact < 0 && (new Random()).nextDouble() < ACLSSolver.UPDATE_PROBABILITY) {
			System.out.println("Setting value to " + proposedValue);
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
		this.myProblemContext.clear();
		this.neighborValues.clear();
		this.impactCosts.clear();
	}

}
