/**
 * File CFLSolver.java
 *
 * Copyright 2014 Coen van Leeuwen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package nl.coenvl.sam.solvers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nl.coenvl.sam.agents.Agent;
import nl.coenvl.sam.agents.LocalCommunicatingAgent;
import nl.coenvl.sam.costfunctions.CostFunction;
import nl.coenvl.sam.messages.HashMessage;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.problemcontexts.LocalProblemContext;
import nl.coenvl.sam.variables.IntegerVariable;

/**
 * TickCFLSolver
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 17 okt. 2014
 *
 */
public class MCSMGMSolver implements IterativeSolver {

	private static final String UPDATE_VALUE = "MCSMGM:UpdateValue";
	private static final String IMPACT_VALUE = "MCSMGM:ImpactValue";
	private static final String LOCAL_REDUCTION = "MCSMGM:BestLocalReduction";
	private static final double EQUAL_UPDATE_PROBABILITY = 0.5;

	private CostFunction myCostFunction;
	//private LocalProblemContext<Integer> myProblemContext;
	private IntegerVariable myVariable;
	private LocalCommunicatingAgent parent;

	// These keep track of received messages
	private Set<Agent> receivedValues;
	private Set<Agent> receivedImpacts;
	private Set<Agent> receivedReductions;
	
	// Keep track of intermediate results
	private Map<Agent, Integer> neighborValues;
	private Map<Agent, Double> neighborImpacts;
	private Map<Agent, Double> neighborReductions;
	
	// These update the problem definition (they are not cleared after init)
	//private HashMap<Agent, Double> localCosts;
	//private HashMap<Integer, HashMap<Agent, Double>> addedCosts;
	private Map<ConstraintKey, ConstraintCost> constraintChanges;
	
	private double bestLocalReduction;
	private Integer bestLocalAssignment;

	public MCSMGMSolver(LocalCommunicatingAgent agent, CostFunction costfunction) {
		this.parent = agent;
		this.myCostFunction = costfunction;
		this.myVariable = (IntegerVariable) this.parent.getVariable();
	}

	@Override
	public void init() {
		this.myVariable.setValue(this.myVariable.getRandomValue());
		//this.myProblemContext = new LocalProblemContext<Integer>(this.parent);
		//this.myProblemContext.setValue(this.myVariable.getValue());
		
		this.receivedValues = new HashSet<Agent>();
		this.receivedImpacts = new HashSet<Agent>();
		this.receivedReductions = new HashSet<Agent>();
		
		this.neighborValues = new HashMap<Agent, Integer>();
		this.neighborImpacts = new HashMap<Agent, Double>();
		this.neighborReductions = new HashMap<Agent, Double>();

		this.constraintChanges = new HashMap<ConstraintKey, ConstraintCost>();
	}

	@Override
	public void push(final Message m) {
		final Agent source = (Agent) m.getContent("source");
		int numNeighbors = parent.getNeighborhood().size();
		
		if (m.getType().equals(MCSMGMSolver.UPDATE_VALUE)) {
			// On update value, store the result
			final Integer value = (Integer) m.getContent("value");
			this.neighborValues.put(source, value);
			this.receivedValues.add(source);

			// If all updates are received, continue
			if (this.receivedValues.size() == numNeighbors) {
				this.receivedValues.clear();
				this.sendImpactMessages();
			}
		} else if (m.getType().equals(MCSMGMSolver.IMPACT_VALUE)) {
			// Store the impact value
			final Double delta = (Double) m.getContent("delta");
			this.neighborImpacts.put(source, delta);
			this.receivedImpacts.add(source);

			// If all impact messages are received, continue
			if (this.receivedImpacts.size() == numNeighbors) {
				this.receivedImpacts.clear();
				this.computeLocalReductions();
			}
		} else if (m.getType().equals(MCSMGMSolver.LOCAL_REDUCTION)) {
			final Double reduction = (Double) m.getContent("LR");
			this.neighborReductions.put(source, reduction);
			this.receivedReductions.add(source);
			
			// If all reductions are received, continue
			if (this.receivedReductions.size() == numNeighbors) {
				this.receivedReductions.clear();
				this.pickValue();
			}
		}
	}

	@Override
	public void tick() {
		Message updateMsg = new HashMessage(MCSMGMSolver.UPDATE_VALUE);

		updateMsg.addContent("value", this.myVariable.getValue());
		updateMsg.addContent("source", this.parent);

		for (Agent n : this.parent.getNeighborhood())
			n.push(updateMsg);
	}
	
	/**
	 * Compute the impact of the newly received messages
	 */
	private void sendImpactMessages() {
		//this.myProblemContext.setValue(this.myVariable.getValue());
		
		//for (Agent n : neighborValues.keySet()) {
		for (Agent n : this.parent.getNeighborhood()) {
			// And this is why we do not immediately update the local problem
			// context
			LocalProblemContext<Integer> temp = new LocalProblemContext<Integer>(parent);
			temp.setValue(this.myVariable.getValue());
			temp.setValue(n, this.neighborValues.get(n));
			//double before = this.myCostFunction.evaluate(this.myProblemContext);
			//this.myProblemContext.setValue(n, this.neighborValues.get(n));
			//double after = this.myCostFunction.evaluate(this.myProblemContext);				
			
			// Compute the cost INCREASE due to the update
			double delta = this.myCostFunction.evaluate(temp); //after - before;
			
			// See if we have any modified problem
			ConstraintKey key = new ConstraintKey(n, this.myVariable.getValue(), this.neighborValues.get(n));
			if (!this.constraintChanges.containsKey(key))
				this.constraintChanges.put(key, new ConstraintCost());
			
			ConstraintCost r = this.constraintChanges.get(key);
			//if (r.originalLocalCost == null)
			//	r.originalLocalCost = delta;
			
			if (r.localCost == null)
				r.localCost = delta;

			// Inform the neighbors
			HashMessage m = new HashMessage(MCSMGMSolver.IMPACT_VALUE);
			m.addContent("source", this.parent);
			// Only add delta if we want to propagate back our cost
			//if (this.lastReduction.containsKey(n) && delta > this.lastReduction.get(n)) {
			if (delta > 0) {
				m.addContent("delta", new Double(delta));
				r.localCost = 0.;
			} else {
				m.addContent("delta", 0.);
			}

			// TODO: Check if this is necessary, or that r is already a reference
			this.constraintChanges.put(key, r);
			n.push(m);
		}
	}
	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void computeLocalReductions() {
		// Get current costs (without impact)
		int myCurrentValue = this.myVariable.getValue();
		//this.myProblemContext.setValue(myCurrentValue);
		LocalProblemContext<Integer> myProblemContext = new LocalProblemContext<Integer>(parent);
		myProblemContext.setValue(myCurrentValue);
		myProblemContext.setAssignment((HashMap<Agent, Integer>) ((HashMap<Agent, Integer>) this.neighborValues).clone());
		
		double before = this.myCostFunction.evaluate(myProblemContext);
		
		// First process all the received impact messages
		for (Agent a : this.parent.getNeighborhood()) {
			// Can we indeed assume that myProblemContext is still up to date?
			//ConstraintKey key = new ConstraintKey(a, myCurrentValue, this.myProblemContext.getValue(a));
			ConstraintKey key = new ConstraintKey(a, myCurrentValue, this.neighborValues.get(a));

			if (!this.constraintChanges.containsKey(key))
				this.constraintChanges.put(key, new ConstraintCost());
			
			// The remote impact should be in the neighborImpact list
			this.constraintChanges.get(key).remoteCost = this.neighborImpacts.get(a);
			before += this.neighborImpacts.get(a);
		}

		// Now compute the best local reduction
		double bestCost = before; //Double.MAX_VALUE; //
		Integer bestAssignment = null;

		LocalProblemContext<Integer> temp = new LocalProblemContext<Integer>(this.parent);
				
		for (Integer assignment : this.myVariable) {
			//temp.setAssignment((HashMap<Agent, Integer>) this.myProblemContext.getAssignment().clone());
			temp.setAssignment((HashMap<Agent, Integer>) ((HashMap<Agent, Integer>) this.neighborValues).clone());
			temp.setValue(assignment);
			double localCost = 0;
			
			for (Agent a : this.parent.getNeighborhood()) {
				ConstraintKey key = new ConstraintKey(a, assignment, temp.getValue(a));
				if (this.constraintChanges.containsKey(key)) {
					ConstraintCost r = this.constraintChanges.get(key);

					// Add remote cost of neighbor)					localCost += (r.remoteCost == null ? 0 : r.remoteCost);
					
					// Remove influence of local neighbor
					//if (r.localCost != null && r.localCost == 0)
					//	temp.clearValue(a);
				}
			}
		
			// Although this will influence less and less since temp will become empty	
			localCost += this.myCostFunction.evaluate(temp);
			
			if (localCost < bestCost) {
				bestCost = localCost;
				bestAssignment = assignment;
			}
		}

		// Get the REDUCTION after changing the value
		this.bestLocalReduction = before - bestCost;
		this.bestLocalAssignment = bestAssignment;

		Message lrMsg = new HashMessage(MCSMGMSolver.LOCAL_REDUCTION);

		lrMsg.addContent("source", this.parent);
		lrMsg.addContent("LR", this.bestLocalReduction);

		for (Agent n : this.parent.getNeighborhood())
			n.push(lrMsg);
	}

	/**
	 * 
	 */
	private void pickValue() {
		Double bestNeighborReduction = Double.MIN_VALUE;
		Agent bestNeighbor = null;
		for (Agent n : this.parent.getNeighborhood())
			if (this.neighborReductions.get(n) > bestNeighborReduction) {
				bestNeighborReduction = this.neighborReductions.get(n);
				bestNeighbor = n;
			}

		if (this.bestLocalReduction > bestNeighborReduction)
			this.myVariable.setValue(bestLocalAssignment);
		if (this.bestLocalReduction == bestNeighborReduction
				&& this.parent.getName().compareTo(bestNeighbor.getName()) < 0) //Math.random() > EQUAL_UPDATE_PROBABILITY)
			this.myVariable.setValue(bestLocalAssignment);

		this.neighborReductions.clear();
	}
	
	@Override
	public void reset() {
		this.myVariable.clear();
	}

	private class ConstraintKey {
		public final Integer myValue;
		public final Integer hisValue;
		public final Agent neighbor;
		
		public ConstraintKey(Agent neighbor, Integer myValue, Integer hisValue) {
			this.neighbor = neighbor;
			this.myValue = myValue;
			this.hisValue = hisValue;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((hisValue == null) ? 0 : hisValue.hashCode());
			result = prime * result + ((myValue == null) ? 0 : myValue.hashCode());
			result = prime * result + ((neighbor == null) ? 0 : neighbor.hashCode());
			return result;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ConstraintKey other = (ConstraintKey) obj;
			
			if (hisValue == null) {
				if (other.hisValue != null)
					return false;
			} else if (!hisValue.equals(other.hisValue))
				return false;
			
			if (myValue == null) {
				if (other.myValue != null)
					return false;
			} else if (!myValue.equals(other.myValue))
				return false;
			
			if (neighbor == null) {
				if (other.neighbor != null)
					return false;
			} else if (!neighbor.equals(other.neighbor))
				return false;
			
			return true;
		}
		
		@Override
		public String toString() {
			return this.myValue + "&" + this.neighbor.getName() + "=" + this.hisValue; 
		}
		
	}
	
	private class ConstraintCost {
		public Double remoteCost; // Double because I want to check for null
		public Double localCost;
		//public Double originalLocalCost;
		// Empty constructor
		public ConstraintCost() {
			this.remoteCost = null;
			this.localCost = null;
			//this.originalLocalCost = null;
		}
		
		@Override
		public String toString() {
			return "l" + this.localCost + "r" + this.remoteCost; 
		}
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " for Agent " + this.parent.getName();
	}
}
