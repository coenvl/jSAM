/**
 * File GreedySolver.java
 *
 * This file is part of the jSAM project 2014.
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

import java.util.ArrayList;
import java.util.SortedSet;

import nl.coenvl.sam.agents.Agent;
import nl.coenvl.sam.agents.OrderedAgent;
import nl.coenvl.sam.costfunctions.CostFunction;
import nl.coenvl.sam.exceptions.InvalidValueException;
import nl.coenvl.sam.messages.HashMessage;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.problemcontexts.IndexedProblemContext;
import nl.coenvl.sam.variables.IntegerVariable;
import nl.coenvl.sam.variables.IntegerVariable.IntegerVariableIterator;

/**
 * GreedySolver
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 11 apr. 2014
 * 
 */
public class GreedySolver implements Solver {

	private static final String PICK_VAR = "GreedySolver:PickVar";

	private final IndexedProblemContext<Integer> context;

	private final CostFunction costfun;

	private final IntegerVariable myVariable;

	private final OrderedAgent parent;

	public GreedySolver(OrderedAgent parent, CostFunction costfun) {
		this.myVariable = (IntegerVariable) parent.getVariable();
		this.parent = parent;
		this.costfun = costfun;
		this.context = new IndexedProblemContext<Integer>(
				this.parent.getSequenceID());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.Solver#init()
	 */
	@Override
	public void init() {
		if (this.parent.getParent() == null)
			this.pickVar();
	}

	/**
     * 
     */
	private void pickVar() {
		IntegerVariableIterator iter = myVariable.iterator();

		Integer bestAssignment = null;
		double minCost = Double.MAX_VALUE;
		while (iter.hasNext()) {
			Integer testAssignment = iter.next();
			context.setValue(testAssignment);

			double testCost = this.costfun.evaluate(context);
			if (testCost < minCost) {
				bestAssignment = testAssignment;
				minCost = testCost;
			}
		}

		context.setValue(bestAssignment);
		try {
			myVariable.setValue(bestAssignment);
		} catch (InvalidValueException e) {
			e.printStackTrace();
		}

		Message forwardMessage = new HashMessage(GreedySolver.PICK_VAR);
		forwardMessage.addContent("pa", this.context.getAssignment());

		SortedSet<Agent> nextAgents = this.parent.getChildren();
		if (!nextAgents.isEmpty())
			nextAgents.first().push(forwardMessage);
		else
			System.out.println("Found a solution!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.Solver#push(nl.coenvl.sam.Message)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void push(Message m) {
		ArrayList<Integer> pa = (ArrayList<Integer>) m.getContent("pa");
		this.context.setAssignment(pa);
		pickVar();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.Solver#reset()
	 */
	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

}
