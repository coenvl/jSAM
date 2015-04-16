/**
 * File AbstractAgent.java
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
package nl.coenvl.sam.agents;

import java.util.HashSet;

import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.variables.Variable;

/**
 * The abstract agent defines some bare essentials for any agent to provide. It
 * contains the name of the agent as well as the variable it is assigned to.
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 4 feb. 2014
 * 
 */
public abstract class AbstractAgent implements Agent, Comparable<Agent> {

	private final String name;

	private final Variable<?> variable;

	private final static HashSet<Agent> allAgents = new HashSet<Agent>();

	protected AbstractAgent(String name, Variable<?> var) {
		this.name = name;
		this.variable = var;
		allAgents.add(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.Agent#getName()
	 */
	@Override
	public final String getName() {
		return this.name;
	}

	@Override
	public final String toString() {
		return "" + this.getClass().getSimpleName() + " " + this.name + " ("
				+ this.getVariable() + ")";
	}

	@Override
	public final synchronized Variable<?> getVariable() {
		return this.variable;
	}

	@Override
	public void reset() {
		this.variable.clear();
	}

	/**
     * 
     */
	public final static void broadCast(Message m) {
		for (Agent a : allAgents) {
			a.push(m.clone());
		}
	}

	public final static void destroyAgents() {
		for (Agent a : allAgents) {
			a.getVariable().clear();
			a.reset();
		}
		allAgents.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Agent o) {
		return this.name.compareTo(o.getName());
	}
}
