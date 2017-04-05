/**
 * File AbstractSolver.java
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
package nl.coenvl.sam.solvers;

import java.util.Set;
import java.util.UUID;

import nl.coenvl.sam.MailMan;
import nl.coenvl.sam.agents.Agent;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.variables.Variable;

/**
 * AbstractSolver
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 1 apr. 2016
 */
public abstract class AbstractSolver<T extends Variable<V>, V> {

    protected final Agent<T, V> parent;
    protected final T myVariable;

    protected AbstractSolver(Agent<T, V> agent) {
        this.parent = agent;
        this.myVariable = agent.getVariable();
    }

    protected void sendToNeighbors(Message m) {
        Set<UUID> set = this.parent.getConstrainedVariableIds();
        for (UUID target : set) {
            MailMan.sendMessage(target, m);
        }
    }

    protected int numNeighbors() {
        return this.parent.getConstrainedVariableIds().size();
    }

    protected void reset() {
        this.myVariable.clear();
    }
}
