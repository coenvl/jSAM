/**
 * File MaxSumADVPVariableSolver.java
 *
 * This file is part of the jCoCoA project.
 *
 * Copyright 2016 Anonymous
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
package org.anon.cocoa.solvers;

import java.util.UUID;

import org.anon.cocoa.MailMan;
import org.anon.cocoa.agents.VariableAgent;
import org.anon.cocoa.messages.Message;
import org.anon.cocoa.variables.IntegerVariable;

/**
 * MaxSumADVPVariableSolver
 *
 * @author Anomymous
 * @version 0.1
 * @since 22 jan. 2016
 */
public class MaxSumADVPVariableSolver extends MaxSumADVariableSolver {

	private final static int START_VP_AFTER_SWITCHES = 2;

	private int switchCount;
	private boolean doVP;

	public MaxSumADVPVariableSolver(VariableAgent<IntegerVariable, Integer> agent) {
		super(agent);
		this.switchCount = 0;
		this.doVP = false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.anon.cocoa.solvers.IterativeSolver#tick()
	 */
	@Override
	public synchronized void tick() {
		this.iterCount++;
		if ((this.iterCount % MaxSumADVariableSolver.REVERSE_AFTER_ITERS) == 0) {
			this.direction = !this.direction;
			this.switchCount++;
			this.doVP = (this.switchCount >= MaxSumADVPVariableSolver.START_VP_AFTER_SWITCHES);
		}

		// Target represents function node f
		for (UUID target : this.variableAgent.getFunctionAdresses()) {
			if ((target.hashCode() > this.parent.hashCode()) == this.direction) {
				continue;
			}

			Message v2f = this.var2funMessage(target);
			if (this.doVP) {
				v2f.put("value", this.myVariable.getValue());
			}

			MailMan.sendMessage(target, v2f);
		}

		this.setMinimizingValue();
		// this.receivedCosts.clear();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.anon.cocoa.solvers.BiPartiteGraphSolver#getCounterPart()
	 */
	@Override
	public Class<? extends BiPartiteGraphSolver> getCounterPart() {
		return MaxSumADVPFunctionSolver.class;
	}

	@Override
	public void reset() {
		super.reset();
		this.switchCount = 0;
		this.doVP = false;
	}
}
