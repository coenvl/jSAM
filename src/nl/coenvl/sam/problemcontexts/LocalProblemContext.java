/**
 * File LocalProblemContext.java
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
package nl.coenvl.sam.problemcontexts;

import java.util.HashMap;
import java.util.Iterator;

import nl.coenvl.sam.agents.Agent;

/**
 * IndexedProblemContext
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 14 mrt. 2014
 * 
 */
public final class LocalProblemContext<T> implements ProblemContext<T> {

	/** The current problem description that defines the context of the agent */
	private HashMap<Agent, T> currentAssignment;

	/** Indicates the current problem's variable that is reassignable */
	private final Agent problemOwner;

	/**
	 * Create a IndexedProblemContext to keep the current partial assignment of
	 * the problem. This object is the argument of a cost function object. Only
	 * one entry of the IndexedProblemContext is modifiable. This is indicated
	 * by the index parameter.
	 * 
	 * @param index
	 *            the index of the value that this problem context can change
	 */
	public LocalProblemContext(Agent owner) {
		this.problemOwner = owner;

		/* Make sure there are at least i+1 items in the assignments */
		this.currentAssignment = new HashMap<Agent, T>();
	}

	/**
     * 
     */
	@Override
	public void clearValue() {
		this.setValue(null);
	}

	/**
	 * Get the current (partial) assignment.
	 * 
	 * @return
	 */
	public HashMap<Agent, T> getAssignment() {
		return currentAssignment;
	}

	/**
	 * Iterator to run through all assignments
	 * 
	 * @return
	 */
	@Override
	public Iterator<T> getIterator() {
		return currentAssignment.values().iterator();
	}

	/**
	 * Get the value of the modifiable entry
	 * 
	 * @return
	 */
	@Override
	public T getValue() {
		return this.getValue(problemOwner);
	}

	public T getValue(Agent neighbor) {
		return currentAssignment.get(neighbor);
	}

	/**
	 * Set all assignments. This function should be used when the problem
	 * context changes.
	 * 
	 * @param assignment
	 */
	public void setAssignment(HashMap<Agent, T> assignment) {
		// currentAssignment.clear();
		for (Agent a : assignment.keySet())
			currentAssignment.put(a, assignment.get(a));
		// if (!currentAssignment.containsKey(a))
		// currentAssignment.put(a, assignment.get(a));

	}

	public void setValue(Agent neighbor, T value) {
		this.currentAssignment.put(neighbor, value);
	}

	/**
	 * Set the value of it's modifiable entry.
	 * 
	 * @param value
	 *            the new value
	 */
	@Override
	public void setValue(T value) {
		this.setValue(problemOwner, value);
	}

	@Override
	public String toString() {
		return "LocalProblemContext " + this.currentAssignment;
	}
}
