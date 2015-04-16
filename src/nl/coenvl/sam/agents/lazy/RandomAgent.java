/**
 * File RandomAgent.java
 * 
 * This file is part of the jSAM project.
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
package nl.coenvl.sam.agents.lazy;

import nl.coenvl.sam.agents.AbstractAgent;
import nl.coenvl.sam.exceptions.InvalidValueException;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.variables.Variable;

/**
 * RandomAgent
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 24 okt. 2014
 *
 */
public class RandomAgent<T> extends AbstractAgent {

	private final Variable<T> var;

	/**
	 * @param name
	 * @param var
	 */
	public RandomAgent(String name, Variable<T> var) {
		super(name, var);
		this.var = var;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.agents.Agent#init()
	 */
	@Override
	public void init() {
		try {
			var.setValue(var.getRandomValue());
		} catch (InvalidValueException e) {
			System.err.println("Unable to set to random value");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.agents.Agent#push(nl.coenvl.sam.messages.Message)
	 */
	@Override
	public void push(Message m) {
		// Does nothing
	}

}
