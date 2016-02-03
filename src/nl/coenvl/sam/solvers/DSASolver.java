/**
 * File DSASolver.java
 * 
 * This file is part of the jSAM project.
 *
 * Copyright 2014 Coen van Leeuwen
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

import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

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
 * DSASolver
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 11 dec. 2014
 *
 */
public class DSASolver implements IterativeSolver {

	public static final double CHANGE_TO_EQUAL_PROB = 0.5;

	public static final double CHANGE_TO_IMPROVE_PROB = 1;

	public static final String UPDATE_VALUE = "DSASolver:Value";

	private volatile LocalProblemContext<Integer> context;

	private CostFunction costfun;

	private IntegerVariable myVar;

	private LocalCommunicatingAgent parent;

	/**
	 * @param dsaAgent
	 * @param costfun
	 */
	public DSASolver(LocalCommunicatingAgent parent, CostFunction costfun) {
		this.parent = parent;
		this.costfun = costfun;
		this.myVar = (IntegerVariable) this.parent.getVariable();
		this.context = new LocalProblemContext<Integer>(parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.anon.cocoa.solvers.Solver#init()
	 */
	@Override
	public synchronized void init() {
		//this.context = new LocalProblemContext<Integer>(parent);
		updateMyValue(myVar.getRandomValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.anon.cocoa.solvers.Solver#push(org.anon.cocoa.messages.Message)
	 */
	@Override
	public synchronized void push(Message m) {
		if (m.getType().equals(DSASolver.UPDATE_VALUE))
			this.updateContext(m);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.anon.cocoa.solvers.Solver#reset()
	 */
	@Override
	public void reset() {
		// TODO Auto-generated method stub
	}

	/**
	 * 
	 */
	@Override
	public synchronized void tick() {
		double bestCost = Double.MAX_VALUE;
		Vector<Integer> bestAssignment = new Vector<Integer>();

		Iterator<Integer> iter = this.myVar.iterator();

		if (this.myVar.isSet()) {
			try {
				context.setValue(this.myVar.getValue());
			} catch (VariableNotSetException e) {
				throw new RuntimeException(e);
			}
		}
		double oldCost = this.costfun.evaluate(context);

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

		if (bestCost > oldCost)
			return;

		if (bestCost == oldCost
				&& Math.random() > DSASolver.CHANGE_TO_EQUAL_PROB)
			return;

		if (bestCost < oldCost
				&& Math.random() > DSASolver.CHANGE_TO_IMPROVE_PROB)
			return;

		// Chose any of the "best" assignments
		Integer assign;
		if (bestAssignment.size() > 1) {
			int i = (new Random()).nextInt(bestAssignment.size());
			assign = bestAssignment.elementAt(i);
		} else {
			assign = bestAssignment.elementAt(0);
		}

		try {
			if (assign != this.myVar.getValue()) {
				updateMyValue(assign);
			}
		} catch (VariableNotSetException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param m
	 */
	private void updateContext(Message m) {
		LocalCommunicatingAgent neighbor = (LocalCommunicatingAgent) m
				.getContent("agent");
		Integer newValue = (Integer) m.getContent("value");
		this.context.setValue(neighbor, newValue);
	}

	/**
	 * @param assign
	 */
	private void updateMyValue(Integer assign) {
		try {
			this.myVar.setValue(assign);
		} catch (InvalidValueException e) {
			throw new RuntimeException(e);
		}

		HashMessage nextMessage = new HashMessage(DSASolver.UPDATE_VALUE);
		nextMessage.addContent("agent", this.parent);
		nextMessage.addContent("value", assign);

		for (Agent neighbor : this.parent.getNeighborhood())
			neighbor.push(nextMessage.clone());
	}

}
