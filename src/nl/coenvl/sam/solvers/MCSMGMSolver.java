/**
 * File MCSMGMSolver.java
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
import java.util.Map;
import java.util.UUID;

import nl.coenvl.sam.MailMan;
import nl.coenvl.sam.agents.Agent;
import nl.coenvl.sam.messages.HashMessage;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.variables.AssignmentMap;
import nl.coenvl.sam.variables.CostMap;
import nl.coenvl.sam.variables.DiscreteVariable;

/**
 * MCSMGMSolver
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 17 okt. 2014
 *
 */
public class MCSMGMSolver<V> extends MGMSolver<V> {

	private enum State {
		SENDVALUE,
		SENDIMPACT,
		SENDGAIN,
		PICKVALUE
	}

	private static final String IMPACT_VALUE = "MCSMGM:ImpactValue";

	// Add keeping track of neighbor impacts
	private State algoState;
	private final CostMap<UUID> neighborImpacts;
	private final Map<ConstraintKey, ConstraintCost> constraintChanges;

	public MCSMGMSolver(Agent<DiscreteVariable<V>, V> agent) {
		super(agent);
		this.neighborImpacts = new CostMap<>();
		this.constraintChanges = new HashMap<>();
		this.algoState = State.SENDVALUE;
	}

	@Override
	public synchronized void push(final Message m) {
		super.push(m);

		if (m.getType().equals(MCSMGMSolver.IMPACT_VALUE)) {
			this.neighborImpacts.put(m.getSource(), m.getDouble("delta"));
		}
	}

	@Override
	public synchronized void tick() {
		switch (this.algoState) {
		case SENDIMPACT:
			this.sendImpact();
			this.algoState = State.SENDGAIN;
			break;

		case SENDGAIN:
			this.sendGain();
			this.algoState = State.PICKVALUE;
			break;

		case PICKVALUE:
			this.pickValue();
			this.algoState = State.SENDVALUE;
			break;

		default:
		case SENDVALUE:
			this.sendValue();
			this.algoState = State.SENDIMPACT;
			break;
		}
	}

	/**
	 * Compute the impact of the newly received messages
	 */
	private void sendImpact() {

		for (UUID target : this.parent.getConstraintIds()) {
			AssignmentMap<V> pa = new AssignmentMap<>();
			pa.setAssignment(this.myVariable, this.myVariable.getValue());
			pa.put(target, this.myProblemContext.get(target));

			// Compute the cost INCREASE due to the update
			double delta = this.parent.getLocalCostIf(pa);

			// See if we have any modified problem
			ConstraintKey key =
					new ConstraintKey(target, this.myVariable.getValue(), this.myProblemContext.get(target));
			if (!this.constraintChanges.containsKey(key)) {
				this.constraintChanges.put(key, new ConstraintCost());
			}

			ConstraintCost r = this.constraintChanges.get(key);
			if (r.localCost == null) {
				r.localCost = delta;
			}

			// Inform the neighbors
			HashMessage m = new HashMessage(this.myVariable.getID(), MCSMGMSolver.IMPACT_VALUE);

			// Only add delta if we want to propagate back our cost
			if (delta > 0) {
				m.put("delta", delta);
				r.localCost = 0.;
			} else {
				m.put("delta", 0);
			}

			this.constraintChanges.put(key, r);
			MailMan.sendMessage(target, m);
		}
	}

	/**
	 *
	 */
	private void sendGain() {
		// Get current costs (without impact)
		this.myProblemContext.setAssignment(this.myVariable, this.myVariable.getValue());
		double before = this.parent.getLocalCostIf(this.myProblemContext);

		// First process all the received impact messages
		for (UUID a : this.parent.getConstraintIds()) {
			// Can we indeed assume that myProblemContext is still up to date?
			ConstraintKey key = new ConstraintKey(a, this.myVariable.getValue(), this.myProblemContext.get(a));

			if (!this.constraintChanges.containsKey(key)) {
				this.constraintChanges.put(key, new ConstraintCost());
			}

			// The remote impact should be in the neighborImpact list
			this.constraintChanges.get(key).remoteCost = this.neighborImpacts.get(a);
			before += this.neighborImpacts.get(a);
		}

		// Now compute the best local reduction
		double bestCost = before;
		V bestAssignment = null;

		AssignmentMap<V> temp = this.myProblemContext.clone();
		for (V assignment : this.myVariable) {
			temp.setAssignment(this.myVariable, assignment);
			double localCost = 0;

			// for (Agent a : this.parent.getNeighborhood()) {
			for (UUID a : this.parent.getConstraintIds()) {
				ConstraintKey key = new ConstraintKey(a, assignment, temp.get(a));
				if (this.constraintChanges.containsKey(key)) {
					// Add remote cost of neighbor)
					ConstraintCost r = this.constraintChanges.get(key);
					localCost += (r.remoteCost == null ? 0 : r.remoteCost);
				}
			}

			// Although this will influence less and less since temp will become empty
			localCost += this.parent.getLocalCostIf(temp);

			if (localCost < bestCost) {
				bestCost = localCost;
				bestAssignment = assignment;
			}
		}

		// Get the REDUCTION after changing the value
		this.bestLocalReduction = before - bestCost;
		this.bestLocalAssignment = bestAssignment;

		Message lrMsg = new HashMessage(this.myVariable.getID(), MGMSolver.LOCAL_REDUCTION);
		lrMsg.put("LR", this.bestLocalReduction);

		this.sendToNeighbors(lrMsg);
	}

	@Override
	public void reset() {
		super.reset();
		this.neighborImpacts.clear();
		this.constraintChanges.clear();
		this.algoState = State.SENDVALUE;
	}

	private class ConstraintKey {
		public final V myValue;
		public final V hisValue;
		public final UUID neighbor;

		public ConstraintKey(UUID neighbor, V myValue, V hisValue) {
			this.neighbor = neighbor;
			this.myValue = myValue;
			this.hisValue = hisValue;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + ((this.hisValue == null) ? 0 : this.hisValue.hashCode());
			result = (prime * result) + ((this.myValue == null) ? 0 : this.myValue.hashCode());
			result = (prime * result) + ((this.neighbor == null) ? 0 : this.neighbor.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (this.getClass() != obj.getClass()) {
				return false;
			}
			@SuppressWarnings("unchecked")
			ConstraintKey other = (ConstraintKey) obj;

			if (this.hisValue == null) {
				if (other.hisValue != null) {
					return false;
				}
			} else if (!this.hisValue.equals(other.hisValue)) {
				return false;
			}

			if (this.myValue == null) {
				if (other.myValue != null) {
					return false;
				}
			} else if (!this.myValue.equals(other.myValue)) {
				return false;
			}

			if (this.neighbor == null) {
				if (other.neighbor != null) {
					return false;
				}
			} else if (!this.neighbor.equals(other.neighbor)) {
				return false;
			}

			return true;
		}

		@Override
		public String toString() {
			return this.myValue + "&" + this.neighbor + "=" + this.hisValue;
		}

	}

	private class ConstraintCost {
		public Double remoteCost; // Double because I want to check for null
		public Double localCost;

		public ConstraintCost() {
			this.remoteCost = null;
			this.localCost = null;
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
