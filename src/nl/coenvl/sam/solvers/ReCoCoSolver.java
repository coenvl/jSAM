/**
 * File GreedyCooperativeSolver.java
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

import nl.coenvl.sam.agents.Agent;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.variables.AssignmentMap;
import nl.coenvl.sam.variables.DiscreteVariable;

/**
 * GreedyCooperativeSolver
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 11 apr. 2014
 *
 */
public class ReCoCoSolver<V> extends CoCoSolver<V> implements IterativeSolver {

	private boolean isRoot;

	public ReCoCoSolver(Agent<DiscreteVariable<V>, V> parent) {
		super(parent);
	}

	@Override
	public synchronized void tick() {
		this.started = false;
		if (this.isRoot) {
			this.sendInquireMsgs();
		}
	}

	public synchronized void setRoot() {
		this.isRoot = true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nl.coenvl.sam.solvers.Solver#push(nl.coenvl.sam.messages.Message)
	 */
	@Override
	public void push(Message m) {
		if (m.containsKey("cpa")) {
			@SuppressWarnings("unchecked")
			AssignmentMap<V> cpa = (AssignmentMap<V>) m.getMap("cpa");
			this.context.putAll(cpa);
		}

		if (m.getType().equals(CoCoSolver.ASSIGN_VAR)) {
			if (!this.started) {
				this.sendInquireMsgs();
			}
		} else if (m.getType().equals(CoCoSolver.INQUIRE_MSG)) {
			this.respond(m);
		} else if (m.getType().equals(CoCoSolver.COST_MSG)) {
			this.processCostMessage(m);
		} else {
			System.err.println(this.getClass().getName() + ": Unexpected message of type " + m.getType());
			return;
		}
	}

}
