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

import nl.coenvl.sam.agents.Agent;
import nl.coenvl.sam.agents.LocalCommunicatingAgent;
import nl.coenvl.sam.costfunctions.CostFunction;
import nl.coenvl.sam.exceptions.InvalidValueException;
import nl.coenvl.sam.exceptions.VariableNotSetException;
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

	private CostFunction myCostFunction;

	private LocalProblemContext<Integer> myProblemContext;

	private IntegerVariable myVariable;

	private LocalCommunicatingAgent parent;

	private HashMap<Agent, Integer> valueList;
	
	private HashMap<Agent, Double> constraintCost;
	
	private HashMap<Agent, Double> lastReduction;

	private double bestLocalReduction;

	private Integer bestLocalAssignment;

	public MCSMGMSolver(LocalCommunicatingAgent agent, CostFunction costfunction) {
		this.parent = agent;
		this.myCostFunction = costfunction;
		this.myVariable = (IntegerVariable) this.parent.getVariable();
	}

	@Override
	public void init() {
		this.myProblemContext = new LocalProblemContext<Integer>(this.parent);
		this.valueList = new HashMap<Agent, Integer>();
		this.lastReduction = new HashMap<Agent, Double>();

		try {
			this.myVariable.setValue(this.myVariable.getRandomValue());
		} catch (InvalidValueException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void push(Message m) {
		if (m.getType().equals(MCSMGMSolver.UPDATE_VALUE)) {
			Agent source = (Agent) m.getContent("source");
			Integer value = (Integer) m.getContent("value");
			this.valueList.put(source, value);

			if (this.valueList.size() == parent.getNeighborhood().size()) {
				this.constraintCost.clear();
				this.updateConstraints();
			}
		} else if (m.getType().equals(MCSMGMSolver.IMPACT_VALUE)) {
			Agent source = (Agent) m.getContent("source");
			Double delta = (Double) m.getContent("delta");
			
			this.constraintCost.put(source, delta);

			if (this.constraintCost.size() == parent.getNeighborhood().size()) {
				this.lastReduction.clear();
				this.computeLocalReductions();
			}
		} else if (m.getType().equals(MCSMGMSolver.LOCAL_REDUCTION)) {
			Agent source = (Agent) m.getContent("source");
			Double minCost = (Double) m.getContent("minCost");
			
			this.lastReduction.put(source, minCost);

			if (this.lastReduction.size() == parent.getNeighborhood().size()) {
				this.valueList.clear();
				this.computeLocalReductions();
			}
		}
	}

	@Override
	public void tick() {
		try {
			Message updateMsg = new HashMessage(MCSMGMSolver.UPDATE_VALUE);

			updateMsg.addContent("value", this.myVariable.getValue());
			updateMsg.addContent("source", this.parent);

			for (Agent n : this.parent.getNeighborhood())
				n.push(updateMsg);

		} catch (VariableNotSetException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void updateConstraints() {
		for (Agent n : this.parent.getNeighborhood()) {
			// And this is why we do not immediately update the local problem context
			double before = this.myCostFunction.evaluate(this.myProblemContext);
			this.myProblemContext.setValue(n, this.valueList.get(n));
			double after = this.myCostFunction.evaluate(this.myProblemContext);
			
			double delta = after - before; // So compute the local decrease due to the change
			if (delta < this.lastReduction.get(n)) {
				HashMessage m = new HashMessage(MCSMGMSolver.IMPACT_VALUE);
				m.addContent("source", this.parent);
				m.addContent("delta", delta);
				n.push(m);
			}
		}
	}
	

	/**
	 * 
	 */
	private void computeLocalReductions() {
		// By now the problem context should be updated with all the neighbors' values
		double minCost = Double.MAX_VALUE;
		Integer bestAssignment = null;
		
		for (Integer i : this.myVariable) {
			this.myProblemContext.setValue(i);
			double val = this.myCostFunction.evaluate(myProblemContext);
			if (val < minCost) {
				minCost = val;
				bestAssignment = i;
			}
		}
		
		this.bestLocalReduction = minCost;
		this.bestLocalAssignment = bestAssignment;
		
		Message lrMsg = new HashMessage(MCSMGMSolver.LOCAL_REDUCTION);

		lrMsg.addContent("source", this.parent);
		lrMsg.addContent("minCost", minCost);

		for (Agent n : this.parent.getNeighborhood())
			n.push(lrMsg);
	}
	
	@Override
	public void reset() {
		this.myVariable.clear();
	}

}
