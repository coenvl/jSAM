/**
 * File MailMan.java
 * 
 * This file is part of the jSAM project.
 *
 * Copyright 2016 TNO
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
package nl.coenvl.sam;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import nl.coenvl.sam.agents.Agent;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.variables.Variable;

/**
 * MailMan
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 4 mrt. 2016
 */
public final class MailMan {

	private static Map<UUID, Agent<?,?>> ownerMap = new HashMap<UUID, Agent<?,?>>();

	private static HashMap<String, Integer> messageCounterMap = new HashMap<String, Integer>();

	private static int sentMessages = 0;
	
	private MailMan() {
		// Private constructor
	}
	
	/**
	 * @param var
	 * @param abstractAgent
	 */
	public static void registerOwner(Variable<?> var, Agent<?, ?> agent) {
		ownerMap.put(var.getID(), agent);
	}

	public static void sendMessage(UUID id, Message m) {
		if (!messageCounterMap.containsKey(m.getType()))
			messageCounterMap.put(m.getType(), 1);
		else
			messageCounterMap.put(m.getType(),
					messageCounterMap.get(m.getType()) + 1);

		MailMan.sentMessages++;

		ownerMap.get(id).push(m);
	}
	
	public static Map<String, Integer> getSentMessages() {
		return messageCounterMap;
	}

	public static int getTotalSentMessages() {
		return sentMessages;
	}

	public static void resetMessageCount() {
		messageCounterMap.clear();
		sentMessages = 0;
	}
	
}
