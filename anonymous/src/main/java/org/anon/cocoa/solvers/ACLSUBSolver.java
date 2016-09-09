/**
 * File ACLSSolver.java
 *
 * This file is part of the jCoCoA project.
 *
 * Copyright 2015 Anonymous
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

import org.anon.cocoa.agents.Agent;
import org.anon.cocoa.messages.HashMessage;
import org.anon.cocoa.messages.Message;
import org.anon.cocoa.variables.DiscreteVariable;

/**
 * ACLSSolver
 *
 * @author Anomymous
 * @version 0.1
 * @since 11 dec. 2015
 */
public class ACLSUBSolver<V> extends ACLSSolver<V> {

	/**
	 * @param agent
	 */
	public ACLSUBSolver(Agent<DiscreteVariable<V>, V> agent) {
		super(agent);
	}

	/**
	 *
	 */
	@Override
	protected void proposeAssignment() {
		// As simple as this
		this.myProposal = this.myVariable.getRandomValue().toString();

		// Send the proposal to all neighbors
		Message updateMsg = new HashMessage(this.myVariable.getID(), ACLSSolver.PROPOSED_UPDATE);

		updateMsg.put("proposal", this.myProposal);

		super.sendToNeighbors(updateMsg);
	}

}
