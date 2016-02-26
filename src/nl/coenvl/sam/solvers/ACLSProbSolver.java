/**
 * File ACLSUBSolver.java
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
package nl.coenvl.sam.solvers;

import java.util.HashMap;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

import nl.coenvl.sam.agents.Agent;
import nl.coenvl.sam.agents.LocalCommunicatingAgent;
import nl.coenvl.sam.costfunctions.CostFunction;
import nl.coenvl.sam.exceptions.VariableNotSetException;
import nl.coenvl.sam.messages.HashMessage;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.problemcontexts.LocalProblemContext;
import nl.coenvl.sam.variables.IntegerVariable;

/**
 * ACLSSolver
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 11 dec. 2015
 */
public class ACLSProbSolver implements IterativeSolver {

	private static final String UPDATE_VALUE = "ACLSProb:UpdateValue";
	private static final String PROPOSED_UPDATE = "ACLSProb:ProposedUpdateValue";
	private static final String IMPACT_MESSAGE = "ACLSProb:ProposalImpact";
	private static final double UPDATE_PROBABILITY = 0.5;

	private final LocalCommunicatingAgent parent;
	private final CostFunction myCostFunction;
	private final IntegerVariable myVariable;

	private LocalProblemContext<Integer> myProblemContext;
	private Integer myProposal;
	private HashMap<Agent, Integer> neighborValues;
	private HashMap<Agent, Double> impactCosts;
	
	public ACLSProbSolver(LocalCommunicatingAgent agent, CostFunction costfunction) {
		this.parent = agent;
		this.myCostFunction = costfunction;
		this.myVariable = (IntegerVariable) this.parent.getVariable();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.solvers.Solver#init()
	 */
	@Override
	public synchronized void init() {
		this.myProblemContext = new LocalProblemContext<Integer>(this.parent);
		this.neighborValues = new HashMap<Agent, Integer>();
		this.impactCosts = new HashMap<Agent, Double>();
		this.myVariable.setValue(this.myVariable.getRandomValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.solvers.Solver#push(nl.coenvl.sam.messages.Message)
	 */
	@Override
	public synchronized void push(Message m) {
		final Agent source = (Agent) m.getContent("source");
		
		if (m.getType().equals(ACLSProbSolver.UPDATE_VALUE)) {
			final Integer value = (Integer) m.getContent("value");
			this.neighborValues.put(source, value);

			if (this.neighborValues.size() == parent.getNeighborhood().size()) {
				// Clear any message that will come the NEXT iteration
				this.impactCosts.clear();
				this.proposeAssignment();
			}
		} else if (m.getType().equals(ACLSProbSolver.PROPOSED_UPDATE)) {
			replyWithLocalCost(m);
		} else if (m.getType().equals(ACLSProbSolver.IMPACT_MESSAGE)) {
			
			if (myProposal != null) {
				final Double impact = (Double) m.getContent("costImpact");
				
				this.impactCosts.put(source, impact);
	
				if (this.impactCosts.size() == parent.getNeighborhood().size()) {
					// Clear any message that will come the NEXT iteration
					this.neighborValues.clear();
					this.decideAssignment();
				}
			}
			
		}
	}

	/**
	 * 
	 */
	private void proposeAssignment() {
		// Compute local reductions
		this.myProblemContext.setAssignment(neighborValues);
		this.myProblemContext.setValue(this.myVariable.getValue());
		//double currentCost = this.myCostFunction.evaluate(this.myProblemContext);

		LocalProblemContext<Integer> temp = new LocalProblemContext<Integer>(parent);
		temp.setAssignment(myProblemContext.getAssignment());
		NavigableMap<Double, Integer> improvementMap = new TreeMap<Double, Integer>();
		double total = 0;
		for (Integer i : this.myVariable) {
			temp.setValue(i);
			double val = this.myCostFunction.evaluate(temp);
			total += val;
			improvementMap.put(total, new Integer(i));
		}

		// Determine the proposal for this round
		double r = (new Random()).nextDouble() * total;
		this.myProposal = improvementMap.ceilingEntry(r).getValue();
				
		// Send the proposal to all neighbors
		Message updateMsg = new HashMessage(ACLSProbSolver.PROPOSED_UPDATE);

		updateMsg.addContent("source", this.parent);
		updateMsg.addContent("proposal", myProposal);

		for (Agent n : this.parent.getNeighborhood())
			n.push(updateMsg);
	}
	
	/**
	 * @param m
	 */
	private void replyWithLocalCost(Message m) {
		// Compute current cost
		final Agent neighbor = (Agent) m.getContent("source");
		final Integer proposal = (Integer) m.getContent("proposal");
		Double impact;
		
		if (proposal == null) {
			impact = 0.;
		} else {
			LocalProblemContext<Integer> temp = new LocalProblemContext<Integer>(parent);
			temp.setAssignment(myProblemContext.getAssignment());
			
			temp.setValue(this.myVariable.getValue());
			double currentCost = this.myCostFunction.evaluate(temp);
			
			// Compute cost after update
			temp.setValue(neighbor, proposal);
			impact = this.myCostFunction.evaluate(temp) - currentCost;
		}
		
		// And send back impact such that negative impact means improvement
		Message impactMsg = new HashMessage(ACLSProbSolver.IMPACT_MESSAGE);

		impactMsg.addContent("source", this.parent);
		impactMsg.addContent("costImpact", new Double(impact));
		neighbor.push(impactMsg);
	}

	/**
	 * 
	 */
	private void decideAssignment() {
		LocalProblemContext<Integer> temp = new LocalProblemContext<Integer>(parent);
		temp.setAssignment(myProblemContext.getAssignment());
		
		temp.setValue(this.myVariable.getValue());
		double currentCost = this.myCostFunction.evaluate(temp);
		
		temp.setValue(myProposal);
		
		double totalImpact = this.myCostFunction.evaluate(temp) - currentCost;
		
		for (Double impact : this.impactCosts.values())
			totalImpact += impact;
		
		if (totalImpact < 0 && (new Random()).nextDouble() < UPDATE_PROBABILITY)
			this.myVariable.setValue(myProposal);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.solvers.IterativeSolver#tick()
	 */
	@Override
	public synchronized void tick() {
		try {
			Message updateMsg = new HashMessage(ACLSProbSolver.UPDATE_VALUE);

			updateMsg.addContent("source", this.parent);
			updateMsg.addContent("value", this.myVariable.getValue());

			for (Agent n : this.parent.getNeighborhood())
				n.push(updateMsg);

		} catch (VariableNotSetException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.solvers.Solver#reset()
	 */
	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

}
