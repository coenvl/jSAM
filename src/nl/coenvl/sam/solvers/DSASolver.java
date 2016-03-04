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

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.Vector;

import nl.coenvl.sam.MailMan;
import nl.coenvl.sam.agents.SolverAgent;
import nl.coenvl.sam.messages.HashMessage;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.variables.DiscreteVariable;

/**
 * DSASolver
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 11 dec. 2014
 *
 */
public class DSASolver<T extends DiscreteVariable<V>, V extends Number> implements IterativeSolver<T,V> {

	public static final double CHANGE_TO_EQUAL_PROB = 0.5;

	public static final double CHANGE_TO_IMPROVE_PROB = 1;

	public static final String UPDATE_VALUE = "DSASolver:Value";
	
	public static final String KEY_VARID = "varID";
	
	public static final String KEY_VARVALUE = "value";

	private final T myVar;

	private final SolverAgent<T, V> parent;
	
	private HashMap<UUID, V> context;

	/**
	 * @param dsaAgent
	 * @param costfun
	 */
	public DSASolver(SolverAgent<T, V> parent) {
		this.parent = parent;
		this.myVar = this.parent.getVariable();
		this.context = new HashMap<UUID, V>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.anon.cocoa.solvers.Solver#init()
	 */
	@Override
	public synchronized void init() {
		updateMyValue(myVar.getRandomValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.anon.cocoa.solvers.Solver#push(org.anon.cocoa.messages.Message)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public synchronized void push(Message m) {
		if (m.getType().equals(DSASolver.UPDATE_VALUE)) {
			UUID varId = UUID.fromString(m.getString(KEY_VARID));
			V newValue = (V) m.getNumber(KEY_VARVALUE);
			this.context.put(varId, newValue);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.anon.cocoa.solvers.Solver#reset()
	 */
	@Override
	public void reset() {
		this.context = new HashMap<UUID, V>();
		this.myVar.clear();
	}

	/**
	 * 
	 */
	@Override
	public synchronized void tick() {
		double bestCost = Double.MAX_VALUE;
		Vector<V> bestAssignment = new Vector<V>();

		context.put(this.myVar.getID(), this.myVar.getValue());
		double oldCost = this.parent.getLocalCostIf(context);
		
		for (V value : this.myVar) {
			context.put(this.myVar.getID(), value);

			double localCost = this.parent.getLocalCostIf(context);

			if (localCost < bestCost) {
				bestCost = localCost;
				bestAssignment.clear();
			}

			if (localCost <= bestCost) {
				bestAssignment.add(value);
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
		V assign;
		if (bestAssignment.size() > 1) {
			int i = (new Random()).nextInt(bestAssignment.size());
			assign = bestAssignment.elementAt(i);
		} else {
			assign = bestAssignment.elementAt(0);
		}

		if (assign != this.myVar.getValue())
			updateMyValue(assign);
	}

	/**
	 * @param assign
	 */
	private void updateMyValue(V assign) {
		this.myVar.setValue(assign);

		HashMessage nextMessage = new HashMessage(DSASolver.UPDATE_VALUE);
		nextMessage.addString(KEY_VARID, this.myVar.getID().toString());
		nextMessage.addNumber(KEY_VARVALUE, assign.intValue());

		for (UUID id : this.parent.getConstraintIds())
			MailMan.sendMessage(id, nextMessage);
	}

}
