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
 * http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Set;
import java.util.UUID;

import nl.coenvl.sam.MailMan;
import nl.coenvl.sam.constraints.Constraint;
import nl.coenvl.sam.exceptions.VariableNotInvolvedException;
import nl.coenvl.sam.variables.AssignmentMap;
import nl.coenvl.sam.variables.Variable;

/**
 * The abstract agent defines some bare essentials for any agent to provide. It contains the name of the agent as well
 * as the variable it is assigned to.
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 4 feb. 2014
 *
 */
public abstract class AbstractAgent<T extends Variable<V>, V> extends AbstractPropertyOwner implements Agent<T, V> {

    private final Set<Constraint<T, V>> constraints;
    private final String name;
    private final T variable;

    protected AbstractAgent(T var, String name) {
        super();
        this.name = name;
        this.variable = var;
        this.constraints = new HashSet<>();
        MailMan.registerOwner(var, this);
    }

    protected AbstractAgent(T var) {
        this(var, "Anonymous agent");
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
        return "" + this.getClass().getSimpleName() + " " + this.name + " (" + this.getVariable() + ")";
    }

    @Override
    public final synchronized T getVariable() {
        return this.variable;
    }

    @Override
    public void reset() {
        if (this.variable != null) {
            this.variable.clear();
        }
    }

    @Override
    public void addConstraint(Constraint<T, V> c) {
        if (!c.getVariableIds().contains(this.variable.getID())) {
            throw new VariableNotInvolvedException(
                    "The variable of the agent " + this.name + " is not involved in the provided constraint");
        }

        this.constraints.add(c);
    }

    @Override
    public void removeConstraint(Constraint<T, V> c) {
        this.constraints.remove(c);
    }

    @Override
    public double getLocalCost() {
        double cost = 0;
        for (final Constraint<T, V> c : this.constraints) {
            cost += c.getCost(this.variable);
        }
        return cost;
    }

    @Override
    public double getLocalCostIf(AssignmentMap<V> valueMap) {
        double cost = 0;
        for (final Constraint<T, V> c : this.constraints) {
            cost += c.getCostIf(this.variable, valueMap);
        }
        return cost;
    }

    @Override
    public Set<UUID> getConstrainedVariableIds() {
        final Set<UUID> set = new HashSet<>();

        for (final Constraint<T, V> c : this.constraints) {
            set.addAll(c.getVariableIds());
        }
        set.remove(this.variable.getID());

        return set;
    }

    @Override
    public Constraint<T, V> getConstraintForAgent(UUID id) {
        for (final Constraint<T, V> c : this.constraints) {
            if (c.getVariableIds().contains(id)) {
                return c;
            }
        }
        return null;
    }
}
