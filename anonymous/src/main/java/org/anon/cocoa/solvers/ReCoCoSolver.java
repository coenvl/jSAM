/**
 * File GreedyCooperativeSolver.java
 *
 * This file is part of the jCoCoA project 2014.
 *
 * Copyright 2014 Anomymous
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
package org.anon.cocoa.solvers;

import org.anon.cocoa.agents.Agent;
import org.anon.cocoa.messages.Message;
import org.anon.cocoa.variables.AssignmentMap;
import org.anon.cocoa.variables.DiscreteVariable;

/**
 * GreedyCooperativeSolver
 *
 * @author Anomymous
 * @version 0.1
 * @since 11 apr. 2014
 *
 */
public class ReCoCoSolver<V> extends CoCoSolver<V> implements IterativeSolver {

	public ReCoCoSolver(Agent<DiscreteVariable<V>, V> parent) {
		super(parent);
	}

	@Override
	public synchronized void tick() {
		this.started = false;
		if (this.isRoot()) {
			this.sendInquireMsgs();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.anon.cocoa.solvers.Solver#push(org.anon.cocoa.messages.Message)
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
			// System.err.println(this.getClass().getName() + ": Unexpected message of type " + m.getType());
			return;
		}
	}

}
