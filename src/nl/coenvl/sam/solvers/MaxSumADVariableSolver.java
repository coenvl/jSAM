/**
 * File MaxSumVariableSolver.java
 * 
 * This file is part of the jSAM project.
 *
 * Copyright 2016 TNO
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
import java.util.Map;

import nl.coenvl.sam.agents.Agent;
import nl.coenvl.sam.agents.LocalCommunicatingAgent;
import nl.coenvl.sam.messages.HashMessage;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.variables.IntegerVariable;

/**
 * MaxSsumVariableSolver
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 22 jan. 2016
 */
public class MaxSumADVariableSolver implements IterativeSolver, BiPartiteGraphSolver {

	private final static int REVERSE_AFTER_ITERS = 100;
	
	private final LocalCommunicatingAgent parent;
	private final IntegerVariable var;
	
	private int iterCount;
	private boolean direction;
	
	private Map<Agent, Map<Integer, Double> > receivedCosts;
	
	public MaxSumADVariableSolver(LocalCommunicatingAgent parent) {
		this.parent = parent;
		this.var = (IntegerVariable) parent.getVariable();
		this.receivedCosts = new HashMap<Agent, Map<Integer, Double> >();
	}
	
	/* (non-Javadoc)
	 * @see nl.coenvl.sam.solvers.Solver#init()
	 */
	@Override
	public synchronized void init() {
		this.iterCount = 0;
		this.direction = true;
	}
	
	/* (non-Javadoc)
	 * @see nl.coenvl.sam.solvers.Solver#push(nl.coenvl.sam.messages.Message)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public synchronized void push(Message m) {
		Agent neighbor = (Agent) m.getContent("source");
		Map<Integer, Double> costMap = (Map<Integer, Double>) m.getContent("costMap");
		this.receivedCosts.put(neighbor, costMap);
	}

	/* (non-Javadoc)
	 * @see nl.coenvl.sam.solvers.Solver#reset()
	 */
	@Override
	public void reset() {
		this.receivedCosts.clear();
	}

	/* (non-Javadoc)
	 * @see nl.coenvl.sam.solvers.IterativeSolver#tick()
	 */
	@Override
	public synchronized void tick() {
		iterCount++;
		if (iterCount % REVERSE_AFTER_ITERS == 0)
			this.direction = !this.direction;
		
		// Target represents function node f
		for (Agent target : this.parent.getNeighborhood()) {
			if ((target.hashCode() > this.parent.hashCode()) == this.direction)
				continue;			
			
			// For all values of variable
			Map<Integer, Double> costMap = new HashMap<Integer, Double>();
			double totalCost = 0;
			
			for (Integer value : this.var) {
				double valueCost = 0;
				
				// The sum of costs for this value it received from all function neighbors apart from f in iteration i − 1.
				for (Agent neighbor : this.parent.getNeighborhood())
					if (neighbor != target) {
						if (this.receivedCosts.containsKey(neighbor) && this.receivedCosts.get(neighbor).containsKey(value))
							valueCost += this.receivedCosts.get(neighbor).get(value);
					}
				
				totalCost += valueCost;
				costMap.put(value, valueCost);
			}
			
			// Normalize to avoid increasingly large values
			double avg = totalCost / costMap.size();
			for (Integer value : this.var)
				costMap.put(value, costMap.get(value) - avg);
						
			Message msg = new HashMessage("VAR2FUN");
			msg.addContent("source", this.parent);
			msg.addContent("costMap", costMap);
			
			target.push(msg);
		}
		
		// And like an afterthought, pick lowest value
		double minCost = Double.MAX_VALUE;
		Integer bestAssignment = null;
		
		for (Integer value : this.var) {
			double valueCost = 0;
			for (Agent neighbor : this.parent.getNeighborhood()) {
				if (this.receivedCosts.containsKey(neighbor) && this.receivedCosts.get(neighbor).containsKey(value))
					valueCost += this.receivedCosts.get(neighbor).get(value);
			}
			
			if (valueCost < minCost) {
				minCost = valueCost;
				bestAssignment = value;
			}
		}
		this.var.setValue(bestAssignment);
		
		// this.receivedCosts.clear();
	}

	/* (non-Javadoc)
	 * @see nl.coenvl.sam.solvers.BiPartiteGraphSolver#getCounterPart()
	 */
	@Override
	public Class<? extends BiPartiteGraphSolver> getCounterPart() {
		return MaxSumADFunctionSolver.class;
	}
}
