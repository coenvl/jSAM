/**
 * File LinkedAgent.java
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
package org.anon.cocoa.agents;

import java.util.UUID;

import org.anon.cocoa.variables.Variable;

/**
 * LinkedAgent
 *
 * @author Anomymous
 * @version 0.1
 * @since 18 mrt. 2016
 */
public class LinkedAgent<T extends Variable<V>, V> extends SolverAgent<T, V> {

	private LinkedAgent<T, V> prev;
	private LinkedAgent<T, V> next;

	/**
	 * @param var
	 * @param name
	 */
	public LinkedAgent(T var, String name) {
		super(var, name);
	}

	public UUID prev() {
		if (this.prev == null) {
			return null;
		}

		return this.prev.getVariable().getID();
	}

	public UUID next() {
		if (this.next == null) {
			return null;
		}

		return this.next.getVariable().getID();
	}

	public void setNext(LinkedAgent<T, V> la) {
		// If we put one in between
		if (this.next != null) {
			la.next = this.next;
		}

		this.next = la;
		la.prev = this;
	}

}
