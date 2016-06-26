/**
 * File FBSolver.java
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

import nl.coenvl.sam.MailMan;
import nl.coenvl.sam.agents.LinkedAgent;
import nl.coenvl.sam.exceptions.InvalidValueException;
import nl.coenvl.sam.messages.HashMessage;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.variables.AssignmentMap;
import nl.coenvl.sam.variables.DiscreteVariable;

/**
 * FBSolver
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 19 mrt. 2014
 *
 */
public class FBSolver<V> implements Solver {

	public static final String CPA_MSG = "FBSolver:CPA_MSG";
	public static final String NEW_SOLUTION = "FBSolver:NEW_SOLUTION";
	public static final String TERMINATE = "FBSolver:TERMINATE";

	private V bestValue;
	private AssignmentMap<V> context;
	private ArrayList<V> exploredValues;

	private DiscreteVariable<V> myVariable;
	private LinkedAgent<? extends DiscreteVariable<V>, V> parent;
	private double pastCost;

	/* These are more implementation specific */
	private volatile double upperBound;

	/**
	 * @param agent
	 */
	public FBSolver(LinkedAgent<? extends DiscreteVariable<V>, V> agent) {
		this.parent = agent;
		this.myVariable = this.parent.getVariable();
		this.upperBound = Double.MAX_VALUE;
		this.exploredValues = new ArrayList<>();
		this.context = new AssignmentMap<>();
	}

	/**
	 *
	 */
	private void assign_CPA() {
		AssignmentMap<V> pa = this.context.clone();

		V assignment = null;
		double paCost = Double.MAX_VALUE;
		for (V iterAssignment : this.myVariable) {
			if (this.exploredValues.contains(iterAssignment)) {
				continue;
			}

			pa.setAssignment(this.myVariable, iterAssignment);
			double iterCost = this.pastCost + this.parent.getLocalCostIf(pa);

			if ((iterCost < paCost) && (iterCost < this.upperBound)) {
				paCost = iterCost;
				assignment = iterAssignment;
			}
		}

		if ((assignment == null) || (paCost >= this.upperBound)) {
			// No new solution found backtrack...
			this.backtrack();
		} else {
			// Assign this agent with the new value
			// myProblemContext.setValue(assignment);
			this.context.setAssignment(this.myVariable, assignment);
			this.exploredValues.add(assignment);

			// Forward the current assignment to the next child, or broadcast
			// new solution if there is none
			if (this.parent.next() == null) {
				Message msg = new HashMessage(this.myVariable.getID(), FBSolver.NEW_SOLUTION);
				msg.put("pa", this.context);
				msg.put("paCost", paCost);
				MailMan.broadCast(msg);

				this.bestValue = assignment;
				this.upperBound = paCost;

				this.backtrack();
			} else {
				Message msg = new HashMessage(this.myVariable.getID(), FBSolver.CPA_MSG);

				msg.put("pa", this.context);
				msg.put("paCost", paCost);

				MailMan.sendMessage(this.parent.next(), msg);
			}
		}
	}

	/**
	 *
	 */
	private void backtrack() {
		if (this.parent.prev() == null) {
			MailMan.broadCast(new HashMessage(this.myVariable.getID(), FBSolver.TERMINATE));
		} else {
			MailMan.sendMessage(this.parent.prev(), new HashMessage(this.myVariable.getID(), FBSolver.CPA_MSG));
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nl.coenvl.sam.Agent#init()
	 */
	@Override
	public void init() {
		if (this.parent.prev() == null) {
			this.assign_CPA();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.anon.cocoa.Solver#push(org.anon.cocoa.Message)
	 */
	@Override
	public void push(Message msg) {

		// Based on the received message we do different stuff
		if (msg.getType().equals(FBSolver.CPA_MSG)) {

			if (msg.containsKey("pa")) {
				@SuppressWarnings("unchecked")
				AssignmentMap<V> cpa = (AssignmentMap<V>) msg.getMap("pa");
				this.context.putAll(cpa);
			}

			// Check to see if it is a new branch we need to research instead of
			// backtrack
			if (msg.containsKey("paCost")) {
				this.pastCost = msg.getDouble("paCost");
				this.context.removeAssignment(this.myVariable);
				this.exploredValues.clear();
			}

			double cost = this.parent.getLocalCostIf(this.context);
			if (cost >= this.upperBound) {
				this.backtrack();
			} else {
				this.assign_CPA();
			}

		} else if (msg.getType().equals(FBSolver.TERMINATE)) {
			try {
				this.myVariable.setValue(this.bestValue);
			} catch (InvalidValueException e) {
				e.printStackTrace();
			}
		} else if (msg.getType().equals(FBSolver.NEW_SOLUTION)) {
			@SuppressWarnings("unchecked")
			AssignmentMap<V> solution = (AssignmentMap<V>) msg.getMap("pa");
			this.bestValue = solution.getAssignment(this.myVariable);
			this.upperBound = msg.getDouble("paCost");
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
		this.myVariable.clear();
		this.upperBound = Double.MAX_VALUE;
		this.exploredValues.clear();
		this.context.clear();
	}

}
