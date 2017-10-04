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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.coenvl.sam;

import java.util.HashMap;
import java.util.LinkedHashMap;
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

    private static final Map<UUID, Agent<?, ?>> ownerMap = new LinkedHashMap<>();
    private static final Map<String, Integer> messageCounterMap = new HashMap<>();
    private static int sentMessages = 0;

    private MailMan() {
        // Private constructor
    }

    public static void registerOwner(final Variable<?> var, final Agent<?, ?> agent) {
        MailMan.register(var.getID(), agent);
    }

    public static void register(final UUID address, final Agent<?, ?> agent) {
        MailMan.ownerMap.put(address, agent);
    }

    public static void sendMessage(final UUID id, final Message m) {
        if (!MailMan.messageCounterMap.containsKey(m.getType())) {
            MailMan.messageCounterMap.put(m.getType(), 1);
        } else {
            MailMan.messageCounterMap.put(m.getType(), MailMan.messageCounterMap.get(m.getType()) + 1);
        }

        final Agent<?, ?> owner = MailMan.ownerMap.get(id);

        if (owner != null) {
            MailMan.sentMessages++;
            owner.push(m);
        } else {
            // Do nothing
        }
    }

    public static void broadCast(final Message msg) {
        for (final UUID id : MailMan.ownerMap.keySet()) {
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
        for (final Agent<?, ?> a : MailMan.ownerMap.values()) {
            a.reset();
        }

        MailMan.ownerMap.clear();
        MailMan.messageCounterMap.clear();
        MailMan.sentMessages = 0;
    }

    public static String stateString(final Variable<?> var1,
            final Object value1,
            final Variable<?> var2,
            final Object value2) {
        String ret = "";
        for (final Agent<?, ?> k : MailMan.ownerMap.values()) {
            final Variable<?> v = k.getVariable();
            if (v.equals(var1)) {
                ret += value1 == null ? "-" : value1;
            } else if (v.equals(var2)) {
                ret += value2 == null ? "-" : value2;
            } else {
                ret += v.isSet() ? v.getValue() : "-";
            }
        }
        return ret;
    }

}
