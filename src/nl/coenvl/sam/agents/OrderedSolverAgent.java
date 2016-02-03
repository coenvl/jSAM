/**
 * File OrderedSolverAgent.java
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

import java.util.SortedSet;
import java.util.TreeSet;

import nl.coenvl.sam.exceptions.DuplicateChildException;
import nl.coenvl.sam.variables.Variable;

/**
 * This class provides the implementation of the OrderedAgent interface. Agents
 * of this type have a parent and a (sorted) set of children with whom they may
 * communicate.
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 11 apr. 2014
 * 
 */
public class OrderedSolverAgent extends AbstractSolverAgent implements
		OrderedAgent {

	public static int maxSequenceID = 0;

	private final SortedSet<Agent> children;

	private Agent parent;

	private final int sequenceID;

	/**
	 * @param name
	 */
	public OrderedSolverAgent(String name, Variable<?> var) {
		super(name, var);
		children = new TreeSet<Agent>();
		sequenceID = OrderedSolverAgent.maxSequenceID++;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.OrderedAgent#addChild(nl.coenvl.sam.OrderedAgent)
	 */
	@Override
	public final void addChild(Agent agent) throws DuplicateChildException {
		if (children.contains(agent))
			throw new DuplicateChildException(agent);

		children.add(agent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public final int compareTo(Agent other) {
		if (other instanceof OrderedAgent)
			return this.sequenceID - ((OrderedAgent) other).getSequenceID();
		//else
		return super.compareTo(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.OrderedAgent#getChildren()
	 */
	@Override
	public final SortedSet<Agent> getChildren() {
		return children;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.OrderedAgent#getParent()
	 */
	@Override
	public final Agent getParent() {
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.OrderedAgent#getSequenceID()
	 */
	@Override
	public final int getSequenceID() {
		return sequenceID;
	}

	/**
	 * Resets the parent and the children. To be called from the implenting
	 * class in the reset function
	 */
	@Override
	public void reset() {
		super.reset();
		parent = null;
		children.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.OrderedAgent#setParent(nl.coenvl.sam.OrderedAgent)
	 */
	@Override
	public final void setParent(Agent parent) {
		this.parent = parent;
	}

}
