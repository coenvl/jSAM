/**
 * File MailMan.java
 *
 * This file is part of the jCoCoA project.
 *
 * Copyright 2016 Anonymous
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.anon.cocoa;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.anon.cocoa.agents.Agent;
import org.anon.cocoa.messages.Message;
import org.anon.cocoa.variables.Variable;

/**
 * MailMan
 *
 * @author Anomymous
 * @version 0.1
 * @since 4 mrt. 2016
 */
public final class MailMan {

    private static final Map<UUID, Agent<?, ?>> ownerMap = new HashMap<>();
    private static final Map<String, Integer> messageCounterMap = new HashMap<>();
    private static int sentMessages = 0;

    private MailMan() {
        // Private constructor
    }

    public static void registerOwner(Variable<?> var, Agent<?, ?> agent) {
        MailMan.register(var.getID(), agent);
    }

    public static void register(UUID address, Agent<?, ?> agent) {
        MailMan.ownerMap.put(address, agent);
    }

    public static void sendMessage(UUID id, Message m) {
        if (!MailMan.messageCounterMap.containsKey(m.getType())) {
            MailMan.messageCounterMap.put(m.getType(), 1);
        } else {
            MailMan.messageCounterMap.put(m.getType(), MailMan.messageCounterMap.get(m.getType()) + 1);
        }

        Agent<?, ?> owner = MailMan.ownerMap.get(id);

        if (owner != null) {
            MailMan.sentMessages++;
            owner.push(m);
        } else {
            // Do nothing
        }
    }

    public static void broadCast(Message msg) {
        for (UUID id : MailMan.ownerMap.keySet()) {
            MailMan.sendMessage(id, msg);
        }
    }

    public static Map<String, Integer> getSentMessages() {
        return MailMan.messageCounterMap;
    }

    public static int getTotalSentMessages() {
        return MailMan.sentMessages;
    }

    public static void reset() {
        for (Agent<?, ?> a : MailMan.ownerMap.values()) {
            a.reset();
        }

        MailMan.ownerMap.clear();
        MailMan.messageCounterMap.clear();
        MailMan.sentMessages = 0;
    }

}
