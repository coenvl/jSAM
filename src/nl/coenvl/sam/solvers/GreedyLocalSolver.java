/**
 * File GreedyCooperativeSolver.java
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import nl.coenvl.sam.agents.Agent;
import nl.coenvl.sam.agents.LocalCommunicatingAgent;
import nl.coenvl.sam.costfunctions.CostFunction;
import nl.coenvl.sam.exceptions.InvalidValueException;
import nl.coenvl.sam.messages.HashMessage;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.problemcontexts.LocalProblemContext;
import nl.coenvl.sam.variables.IntegerVariable;

/**
 * GreedyCooperativeSolver
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 11 apr. 2014
 * 
 */
@SuppressWarnings("unchecked")
public class GreedyLocalSolver implements Solver {

	private static final String ASSIGN_VAR = "GreedyLocalSolver:AssignVariable";

	private LocalProblemContext<Integer> context;

	private final CostFunction costfun;

	private final IntegerVariable myVariable;

	private final LocalCommunicatingAgent parent;

	public GreedyLocalSolver(LocalCommunicatingAgent parent,
			CostFunction costfun) {
		this.parent = parent;
		this.costfun = costfun;
		// Assume we DO always have an integer variable
		this.myVariable = (IntegerVariable) parent.getVariable();

		this.context = new LocalProblemContext<Integer>(this.parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.Solver#init()
	 */
	@Override
	public void init() {
		// HashMap<Agent, Integer> cpa = new HashMap<Agent, Integer>();
		// this.pickVar(cpa);
	}

	/**
	 * @param m
	 */
	private synchronized void pickVar(HashMap<Agent, Integer> pa) {
		double bestCost = Double.MAX_VALUE;

		Iterator<Integer> iter = myVariable.iterator();

		context.setAssignment(pa);

		Vector<Integer> bestAssignment = new Vector<Integer>();
		while (iter.hasNext()) {
			Integer iterAssignment = iter.next();
			context.setValue(iterAssignment);

			double localCost = costfun.evaluate(context);

			if (localCost < bestCost) {
				bestCost = localCost;
				bestAssignment.clear();
			}

			if (localCost <= bestCost) {
				bestAssignment.add(iterAssignment);
			}
		}

		Integer assign;
		if (bestAssignment.size() > 1) {
			int i = (new Random()).nextInt(bestAssignment.size());
			assign = bestAssignment.elementAt(i);
		} else {
			assign = bestAssignment.elementAt(0);
		}

		try {
			this.myVariable.setValue(assign);
			context.setValue(assign);
		} catch (InvalidValueException e) {
			throw new RuntimeException(e);
		}

		HashMessage nextMessage = new HashMessage(GreedyLocalSolver.ASSIGN_VAR);
		nextMessage.addContent("cpa", context.getAssignment());

		// Maybe it would be better if I would send the update message 1 by 1.
		for (Agent neighbor : this.parent.getNeighborhood())
			neighbor.push(nextMessage.clone());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.anon.cocoa.solvers.Solver#push(org.anon.cocoa.messages.Message)
	 */
	@Override
	public synchronized void push(Message m) {
		if (m.getType().equals(GreedyLocalSolver.ASSIGN_VAR)) {
			if (myVariable.isSet())
				return;

			HashMap<Agent, Integer> pa;
			if (m.hasContent("cpa"))
				pa = (HashMap<Agent, Integer>) m.getContent("cpa");
			else
				pa = new HashMap<Agent, Integer>();

			this.pickVar(pa);
		} else {
			System.err.println("Unexpected message of type " + m.getType());
			return;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.solvers.Solver#reset()
	 */
	@Override
	public void reset() {
		this.myVariable.clear();
	}

}
