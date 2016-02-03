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
import java.util.Map;

import nl.coenvl.sam.agents.Agent;
import nl.coenvl.sam.agents.LocalCommunicatingAgent;
import nl.coenvl.sam.costfunctions.CostFunction;
import nl.coenvl.sam.exceptions.InvalidValueException;
import nl.coenvl.sam.exceptions.VariableNotSetException;
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
public class TickCFLSolver implements IterativeSolver {

	private static final double A = 0.05;

	private static final double B = 0.05;

	private CostFunction myCostFunction;

	private LocalProblemContext<Integer> myProblemContext;

	private IntegerVariable myVariable;

	private LocalCommunicatingAgent parent;

	private Map<Integer, Double> valueProb;

	public TickCFLSolver(LocalCommunicatingAgent agent,
			CostFunction costfunction) {
		this.parent = agent;
		this.myCostFunction = costfunction;
		this.myVariable = (IntegerVariable) this.parent.getVariable();
		this.valueProb = new HashMap<Integer, Double>();
	}

	@Override
	public void init() {
		for (Integer iter : this.myVariable)
			valueProb.put(iter, 1.0 / myVariable.getRange());

		this.myProblemContext = new LocalProblemContext<Integer>(this.parent);

		try {
			this.myVariable.setValue(this.myVariable.getRandomValue());
		} catch (InvalidValueException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void push(Message m) {
		// This function should never be called
	}
	
	@Override
	public void tick() {
		for (int y = 1; y < 10; y++) {

			try {
				Thread.sleep(10);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			double randvar = Math.random();

			// Get a random value according to the distribution
			double hi = 0;
			Integer match = null;
			for (Integer i : this.valueProb.keySet()) {
				hi += this.valueProb.get(i);
				if (randvar <= hi) {
					match = i;
					break;
				}
			}

			// Should happen only in the extremely rare condition that the
			// valueProb map had rounding issues, and the randvar is extremely
			// high
			if (match == null)
				match = myVariable.getUpperBound();

			try {
				this.myVariable.setValue(match);
			} catch (InvalidValueException e) {
				e.printStackTrace();
			}

			HashMap<Agent, Integer> cpa = new HashMap<Agent, Integer>();
			for (Agent a : this.parent.getNeighborhood())
				try {
					cpa.put(a, (Integer) a.getVariable().getValue());
				} catch (VariableNotSetException e) {
					cpa.put(a, null);
				}

			cpa.put(this.parent, match);
			this.myProblemContext.setAssignment(cpa);

			double cost = this.myCostFunction.evaluate(this.myProblemContext);

			if (cost == 0) {
				for (Integer iter : this.valueProb.keySet())
					this.valueProb.put(iter, 0.0);
				this.valueProb.put(match, 1.0);

			} else {
				double W = myVariable.getRange() - 1 + (A / B);
				for (Integer iter : this.valueProb.keySet())
					if (iter == match)
						this.valueProb.put(match,
								(1.0 - B) * valueProb.get(match) + (A / W));
					else
						this.valueProb.put(iter,
								(1.0 - B) * valueProb.get(iter) + (B / W));
			}
		}
	}

	@Override
	public void reset() {
		this.myVariable.clear();
	}

}
