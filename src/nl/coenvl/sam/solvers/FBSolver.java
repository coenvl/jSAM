/**
 * File SFBSolver.java
 *
 * This file is part of the jSAM project 2014.
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

import java.util.ArrayList;

import nl.coenvl.sam.agents.AbstractAgent;
import nl.coenvl.sam.agents.OrderedAgent;
import nl.coenvl.sam.costfunctions.CostFunction;
import nl.coenvl.sam.exceptions.InvalidValueException;
import nl.coenvl.sam.messages.HashMessage;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.problemcontexts.IndexedProblemContext;
import nl.coenvl.sam.variables.IntegerVariable;
import nl.coenvl.sam.variables.IntegerVariable.IntegerVariableIterator;

/**
 * SFBSolver
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 19 mrt. 2014
 * 
 */
@Deprecated
public class FBSolver implements Solver {

	public static final String CPA_MSG = "FBSO:CPA_MSG";
	public static final String NEW_SOLUTION = "FBSolver:NEW_SOLUTION";
	public static final String TERMINATE = "FBSolver:TERMINATE";

	private Integer bestValue;
	private ArrayList<Integer> exploredValues;
	private CostFunction myCostFunction;
	private IndexedProblemContext<Integer> myProblemContext;

	private IntegerVariable myVariable;
	/* These are values for the IntegerProblemSolver */
	private OrderedAgent parent;
	private double pastCost;

	/* These are more implementation specific */
	private volatile double upperBound;

	/**
	 * @param agent
	 */
	public FBSolver(OrderedAgent agent, CostFunction costfunction) {
		this.parent = agent;
		this.myCostFunction = costfunction;
		this.myVariable = (IntegerVariable) this.parent.getVariable();
	}

	/**
     * 
     */
	private void assign_CPA() {
		int i = this.parent.getSequenceID();

		IntegerVariableIterator iter = myVariable.iterator();

		Integer assignment = null;
		double paCost = Double.MAX_VALUE;

		// Make a local copy to mess around with
		IndexedProblemContext<Integer> pa = new IndexedProblemContext<Integer>(
				i);
		pa.setAssignment(myProblemContext.getAssignment());

		while (iter.hasNext()) {
			Integer iterAssignment = iter.next();

			if (exploredValues.contains(iterAssignment))
				continue;

			pa.setValue(iterAssignment);

			double iterCost = this.pastCost + myCostFunction.evaluate(pa);
			if (iterCost < paCost && iterCost < upperBound) {
				paCost = iterCost;
				assignment = iterAssignment;
			}
		}

		if (assignment == null || paCost >= upperBound) {
			// No new solution found backtrack...
			this.backtrack();
		} else {
			// Assign this agent with the new value
			myProblemContext.setValue(assignment);
			exploredValues.add(assignment);

			// Forward the current assignment to the next child, or broadcast
			// new solution if there is none
			ArrayList<Integer> msgPa = myProblemContext.getAssignment();
			if (this.parent.getChildren().isEmpty()) {
				Message msg = new HashMessage(FBSolver.NEW_SOLUTION);
				msg.addContent("pa", msgPa.clone());
				msg.addContent("paCost", paCost);
				AbstractAgent.broadCast(msg);

				this.bestValue = assignment;
				this.upperBound = paCost;

				backtrack();
			} else {
				Message msg = new HashMessage(FBSolver.CPA_MSG);

				msg.addContent("pa", msgPa.clone());
				msg.addContent("paCost", paCost);

				this.parent.getChildren().first().push(msg);
			}
		}
	}

	/**
     * 
     */
	private void backtrack() {
		if (this.parent.getParent() == null)
			AbstractAgent.broadCast(new HashMessage(FBSolver.TERMINATE));
		else {
			Message msg = new HashMessage(FBSolver.CPA_MSG);
			this.parent.getParent().push(msg);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.Agent#init()
	 */
	@Override
	public void init() {
		this.upperBound = Double.MAX_VALUE;
		this.exploredValues = new ArrayList<Integer>();
		this.myProblemContext = new IndexedProblemContext<Integer>(
				this.parent.getSequenceID());
		myProblemContext.setValue(null);

		if (this.parent.getParent() == null)
			this.assign_CPA();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.anon.cocoa.Solver#push(org.anon.cocoa.Message)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void push(Message msg) {

		// Based on the received message we do different stuff
		if (msg.getType().equals(FBSolver.CPA_MSG)) {

			if (msg.hasContent("pa"))
				myProblemContext.setAssignment((ArrayList<Integer>) msg
						.getContent("pa"));

			// Check to see if it is a new branch we need to research instead of
			// backtrack
			if (msg.hasContent("paCost")) {
				this.pastCost = (Double) msg.getContent("paCost");
				throw new RuntimeException("Don't use this function anymore");
				//myProblemContext.clearValue();
				//this.exploredValues.clear();
			}

			double cost = myCostFunction.evaluate(myProblemContext);
			if (cost >= this.upperBound) // cost is too high, start backtrack
				this.backtrack();
			else
				this.assign_CPA();

		} else if (msg.getType().equals(FBSolver.TERMINATE)) {
			try {
				myVariable.setValue(bestValue);
			} catch (InvalidValueException e) {
				e.printStackTrace();
			}
		} else if (msg.getType().equals(FBSolver.NEW_SOLUTION)) {
			ArrayList<Integer> solution = (ArrayList<Integer>) msg
					.getContent("pa");
			this.bestValue = solution.get(this.parent.getSequenceID());
			this.upperBound = (Double) msg.getContent("paCost");
		} else {
			System.err.println("Unexpected message of type " + msg.getType());
		}
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
