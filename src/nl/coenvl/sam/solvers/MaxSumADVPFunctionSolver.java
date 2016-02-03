/**
 * File MaxSumFunctionSolver.java
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
import nl.coenvl.sam.costfunctions.CostFunction;
import nl.coenvl.sam.exceptions.InvalidValueException;
import nl.coenvl.sam.messages.HashMessage;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.problemcontexts.LocalProblemContext;
import nl.coenvl.sam.variables.IntegerVariable;

/**
 * MaxSumFunctionSolver
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 22 jan. 2016
 */
public class MaxSumADVPFunctionSolver implements IterativeSolver, BiPartiteGraphSolver {

	private final static int REVERSE_AFTER_ITERS = 400;
	
	private final LocalCommunicatingAgent parent;
	private final CostFunction costfun;
	
	private int iterCount;
	private boolean direction;

	private Map<Agent, Map<Integer, Double>> receivedCosts;
	private Map<Agent, Integer> knownValues;

	public MaxSumADVPFunctionSolver(LocalCommunicatingAgent parent, CostFunction costfun) {
		this.parent = parent;
		this.costfun = costfun;
		this.receivedCosts = new HashMap<Agent, Map<Integer, Double>>();
		this.knownValues = new HashMap<Agent, Integer>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.solvers.Solver#init()
	 */
	@Override
	public synchronized void init() {
		this.iterCount = 0;
		this.direction = true;
//		this.switchCount = 0;
//		this.doVP = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.solvers.Solver#push(nl.coenvl.sam.messages.Message)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public synchronized void push(Message m) {
		Agent neighbor = (Agent) m.getContent("source");
		Map<Integer, Double> costMap = (Map<Integer, Double>) m.getContent("costMap");
		this.receivedCosts.put(neighbor, costMap);
		
		if (m.hasContent("value"))
			this.knownValues.put(neighbor, (Integer) m.getContent("value"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.solvers.Solver#reset()
	 */
	@Override
	public void reset() {
		this.receivedCosts.clear();
	}

	/*
	 * A message sent from a function-node f to a variable-node x in iteration i
	 * includes for each possible value d \in Dx the minimal cost of any
	 * combination of assignments to the variables involved in f apart from x
	 * and the assignment of value d to variable x.
	 * 
	 * @see nl.coenvl.sam.solvers.IterativeSolver#tick()
	 */
	@Override
	public synchronized void tick() {
		iterCount++;
		if (iterCount % REVERSE_AFTER_ITERS == 0)
			this.direction = !this.direction;
		
		// Target represents variable node x
		// Only works for binary constraints
		assert (this.parent.getNeighborhood().size() == 2);

		for (Agent target : this.parent.getNeighborhood()) {
			if ((target.hashCode() > this.parent.hashCode()) == this.direction)
				continue;
			
			// For each target I create a problem context with MY parent as
			// owner, because that is what the cost function will look for
			LocalProblemContext<Integer> pc = new LocalProblemContext<Integer>(null);

			// For all values of variable
			Map<Integer, Double> costMap = new HashMap<Integer, Double>();
			IntegerVariable targetVar = (IntegerVariable) target.getVariable();

			for (Integer value : targetVar) {
				pc.setValue(target, value);

				double minCost = Double.MAX_VALUE;
				// Now we know there is only one other neighbor, so iterate for
				// him
				for (Agent other : this.parent.getNeighborhood()) {
					if (other == target)
						continue;

					if (minCost < Double.MAX_VALUE)
						throw new InvalidValueException(
								"The min cost could not be lowered already, more than one agent in constraint?");
					
					if (this.knownValues.containsKey(other)) {
						// For VP: Only consider known values
						Integer val2 = this.knownValues.get(other);
						pc.setValue(other, val2);
						
						double cost = this.costfun.evaluateFull(pc);
						if (this.receivedCosts.containsKey(other) && this.receivedCosts.get(other).containsKey(val2))
							cost += this.receivedCosts.get(other).get(val2);
						
						// I think this is redundant, because it will always be true
						if (cost < minCost)
							minCost = cost;
					} else {
						IntegerVariable otherVar = (IntegerVariable) other.getVariable();
						for (Integer val2 : otherVar) {
							pc.setValue(other, val2);
							double cost = this.costfun.evaluateFull(pc);
							
							if (this.receivedCosts.containsKey(other) && this.receivedCosts.get(other).containsKey(val2))
								cost += this.receivedCosts.get(other).get(val2);
							
							if (cost < minCost)
								minCost = cost;
						}
					}
				}

				costMap.put(value, minCost);
			}
			
			Message msg = new HashMessage("FUN2VAR");
			msg.addContent("source", this.parent);
			msg.addContent("costMap", costMap);
			
			target.push(msg);
		}

		// this.receivedCosts.clear();
	}

	/* (non-Javadoc)
	 * @see nl.coenvl.sam.solvers.BiPartiteGraphSolver#getCounterPart()
	 */
	@Override
	public Class<? extends BiPartiteGraphSolver> getCounterPart() {
		return MaxSumADVPVariableSolver.class;
	}

}
