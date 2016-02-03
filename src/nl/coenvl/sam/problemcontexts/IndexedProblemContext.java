/**
 * File IndexedProblemContext.java
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

import java.util.ArrayList;

/**
 * IndexedProblemContext
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 14 mrt. 2014
 * 
 */
public final class IndexedProblemContext<T> implements ProblemContext<T> {

	/** The current problem description that defines the context of the agent */
	private ArrayList<T> currentAssignment;

	/** Indicates the current problem's variable that is reassignable */
	private final int variableIndex;

	/**
	 * Create a IndexedProblemContext to keep the current partial assignment of
	 * the problem. This object is the argument of a cost function object. Only
	 * one entry of the IndexedProblemContext is modifiable. This is indicated
	 * by the index parameter.
	 * 
	 * @param index
	 *            the index of the value that this problem context can change
	 */
	public IndexedProblemContext(int index) {
		this.variableIndex = index;

		/* Make sure there are at least i+1 items in the assignments */
		this.currentAssignment = new ArrayList<T>();
		for (int i = 0; i <= variableIndex; i++)
			this.currentAssignment.add(null);
	}

	/**
     * 
     */
	//@Override
	//public void clearValue() {
	//	this.setValue(null);
	//}

	/**
	 * Get the current (partial) assignment.
	 * 
	 * @return To prevent the possibility of aliasing the return type is an
	 *         array of primitive variable values.
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<T> getAssignment() {
		return (ArrayList<T>) currentAssignment.clone();
	}

	/**
	 * Iterator to run through all assignments
	 * 
	 * @return
	 */
	//@Override
	//public Iterator<T> getIterator() {
	//	return currentAssignment.iterator();
	//}

	/**
	 * Get the value of the modifiable entry
	 * 
	 * @return
	 */
	//@Override
	//public T getValue() {
	//	return currentAssignment.get(variableIndex);
	//}

	public T getValue(int index) {
		return currentAssignment.get(index);
	}

	/**
	 * Set all assignments. This function should be used when the problem
	 * context changes.
	 * 
	 * @param objects
	 *            To prevent the possibility of aliasing the argument type
	 *            should be an array of primitive variable values.
	 */
	public void setAssignment(ArrayList<T> assignment) {
		// currentAssignment.clear();
		for (int i = 0; i < assignment.size(); i++) {
			if (currentAssignment.size() <= i)
				currentAssignment.add(i, assignment.get(i));
			else
				currentAssignment.set(i, assignment.get(i));
			// currentAssignment.set(i, assignment.get(i));
		}
	}

	public void setValue(int index, T value) {
		if (currentAssignment.size() <= index)
			currentAssignment.add(index, value);
		else
			currentAssignment.set(index, value);
	}

	/**
	 * Set the value of it's modifiable entry.
	 * 
	 * @param value
	 *            the new value
	 */
	@Override
	public void setValue(T value) {
		setValue(variableIndex, value);
	}

	@Override
	public String toString() {
		return "IndexedProblemContext " + this.currentAssignment;
	}
}
