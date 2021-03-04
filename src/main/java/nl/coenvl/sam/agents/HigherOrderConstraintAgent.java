/**
 * File ConstraintAgent.java
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
package nl.coenvl.sam.agents;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import nl.coenvl.sam.MailMan;
import nl.coenvl.sam.constraints.Constraint;
import nl.coenvl.sam.constraints.HigherOrderConstraint;
import nl.coenvl.sam.exceptions.InvalidPropertyException;
import nl.coenvl.sam.exceptions.InvalidValueException;
import nl.coenvl.sam.exceptions.PropertyNotSetException;
import nl.coenvl.sam.exceptions.VariableNotInvolvedException;
import nl.coenvl.sam.exceptions.VariableNotSetException;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.solvers.IterativeSolver;
import nl.coenvl.sam.solvers.SolverRunner;
import nl.coenvl.sam.variables.AssignmentMap;
import nl.coenvl.sam.variables.Variable;

/**
 * ConstraintAgent
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 8 apr. 2016
 */
public class HigherOrderConstraintAgent<T extends Variable<V>, V> extends AbstractPropertyOwner
        implements ConstraintAgent<T, V>, IterativeSolver {

    private final UUID address;
    private final String name;
    private final HigherOrderConstraint<T, V> myConstraint;
    private final Set<T> variables;

    private SolverRunner mySolver;

    public HigherOrderConstraintAgent(final String name,
            final HigherOrderConstraint<T, V> constraint,
            final Set<T> vars) {
        super();
        this.name = name;
        this.myConstraint = constraint;
        this.address = UUID.randomUUID();
        this.variables = vars;
        MailMan.register(this.address, this);
    }

    /**
     * @param name
     * @param var
     */
    public HigherOrderConstraintAgent(final String name, final HigherOrderConstraint<T, V> constraint, final T[] vars) {
        this(name, constraint, new HashSet<>(Arrays.asList(vars)));
    }

    /**
     * @param name
     * @param var
     */
    public HigherOrderConstraintAgent(final String name, final HigherOrderConstraint<T, V> constraint, final T var) {
        this(name, constraint, new HashSet<>(Arrays.asList(var)));
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.Agent#init()
     */
    @Override
    public final synchronized void init() {
        // this.mySolver.startThread();
        this.mySolver.init();
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.Agent#push(nl.coenvl.sam.Message)
     */
    @Override
    public final synchronized void push(final Message m) {
        this.mySolver.push(m);
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.solvers.Solver#tick()
     */
    @Override
    public void tick() {
        this.mySolver.tick();
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.Agent#reset()
     */
    @Override
    public void reset() {
        this.mySolver.reset();
    }

    public final void setSolver(final IterativeSolver solver) {
        this.mySolver = new SolverRunner(solver);
    }

    @Override
    public boolean isFinished() {
        return this.mySolver.emptyQueue();
    }

    @Override
    public T getVariableWithID(final UUID id) {
        for (final T var : this.variables) {
            if (var.getID().equals(id)) {
                return var;
            }
        }
        throw new VariableNotInvolvedException("Variable not part of Constraint!");
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.agents.Agent#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @return
     */
    @Override
    public UUID getID() {
        return this.address;
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.agents.Agent#getConstraintIds()
     */
    @Override
    public Set<UUID> getConstrainedVariableIds() {
        return this.myConstraint.getVariableIds();
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.agents.Agent#getLocalCost()
     */
    @Override
    public double getLocalCost() {
        double cost = 0;
        for (final T var : this.variables) {
            cost += this.myConstraint.getCost(var);
        }
        return cost;
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.agents.Agent#getLocalCostIf(nl.coenvl.sam.variables.AssignmentMap)
     */
    @Override
    public double getLocalCostIf(final AssignmentMap<V> valueMap) {
        double cost = 0;
        for (final T var : this.variables) {
            cost += this.myConstraint.getCostIf(var, valueMap);
        }
        return cost;
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.agents.Agent#getVariable()
     */
    @SuppressWarnings("unchecked")
    @Override
    public T getVariable() {
        return (T) new Variable<V>() {

            @Override
            public void clear() {
                throw new UnsupportedOperationException("Cannot use the variable of a constraint agent");
            }

            @Override
            public Variable<V> clone() {
                throw new UnsupportedOperationException("Cannot use the variable of a constraint agent");
            }

            @Override
            public V getLowerBound() {
                throw new UnsupportedOperationException("Cannot use the variable of a constraint agent");
            }

            @Override
            public String getName() {
                throw new UnsupportedOperationException("Cannot use the variable of a constraint agent");
            }

            @Override
            public V getRandomValue() {
                throw new UnsupportedOperationException("Cannot use the variable of a constraint agent");
            }

            @Override
            public V getUpperBound() {
                throw new UnsupportedOperationException("Cannot use the variable of a constraint agent");
            }

            @Override
            public V getValue() throws VariableNotSetException {
                throw new UnsupportedOperationException("Cannot use the variable of a constraint agent");
            }

            @Override
            public boolean isSet() {
                throw new UnsupportedOperationException("Cannot use the variable of a constraint agent");
            }

            @Override
            public Variable<V> setValue(final V value) throws InvalidValueException {
                throw new UnsupportedOperationException("Cannot use the variable of a constraint agent");
            }

            @Override
            public UUID getID() {
                throw new UnsupportedOperationException("Cannot use the variable of a constraint agent");
            }

            @Override
            public boolean has(final String key) {
                throw new UnsupportedOperationException(
                        "Cannot access properties of the variable of a constraint agent");
            }

            @Override
            public Object get(final String key) throws PropertyNotSetException {
                throw new UnsupportedOperationException(
                        "Cannot access properties of the variable of a constraint agent");
            }

            @Override
            public void set(final String key, final Object val) throws InvalidPropertyException {
                throw new UnsupportedOperationException(
                        "Cannot access properties of the variable of a constraint agent");
            }

        };
        // throw new UnsupportedOperationException("Cannot get the variable of ConstraintAgent");
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.agents.Agent#addConstraint(nl.coenvl.sam.constraints.Constraint)
     */
    @Override
    public void addConstraint(final Constraint<T, V> c) {
        throw new UnsupportedOperationException("Cannot add Constraints to ConstraintAgent");
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.agents.Agent#removeConstraint(nl.coenvl.sam.constraints.Constraint)
     */
    @Override
    public void removeConstraint(final Constraint<T, V> c) {
        throw new UnsupportedOperationException("Cannot remove Constraints to ConstraintAgent");
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.agents.Agent#getConstraintForAgent(java.util.UUID)
     */
    @Override
    public Constraint<T, V> getConstraintForAgent(final UUID target) {
        throw new UnsupportedOperationException("Cannot get Constraints from ConstraintAgent");
    }

    @Override
    public int compareTo(final Agent<T, V> o) {
        return this.getName().compareTo(o.getName());
    }

}
