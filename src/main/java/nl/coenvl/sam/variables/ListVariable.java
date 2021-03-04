/**
 * File SetVariable.java
 *
 * Copyright 2019 TNO
 */
package nl.coenvl.sam.variables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import nl.coenvl.sam.agents.AbstractPropertyOwner;
import nl.coenvl.sam.exceptions.InvalidValueException;
import nl.coenvl.sam.exceptions.VariableNotSetException;

/**
 * ListVariable
 *
 * @author coenvl
 * @version 0.1
 * @since Jul 3, 2019
 */
public class ListVariable<T> extends AbstractPropertyOwner implements DiscreteVariable<T> {

    public static List<String> updateTrail = Collections.synchronizedList(new ArrayList<>());

    private static int unnamedVariableSequence = 0;

    private final UUID id;
    private final String name;

    private final List<T> domain;
    private boolean set = false;
    private T value;

    public ListVariable(final List<T> domain) {
        this(domain, "MyListVariable" + ListVariable.unnamedVariableSequence++);
    }

    public ListVariable(final List<T> domain, final String name) {
        this.domain = domain;
        this.name = name;
        this.id = UUID.randomUUID();
    }

    @Override
    public void clear() {
        this.value = null;
        this.set = false;
    }

    @Override
    public Variable<T> clone() {
        return new ListVariable<>(this.domain, this.name + " (clone)");
    }

    @Override
    public T getLowerBound() {
        return this.domain.get(0);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public T getRandomValue() {
        return this.domain.get((new Random()).nextInt(this.domain.size()));
    }

    @Override
    public T getUpperBound() {
        return this.domain.get(this.domain.size() - 1);
    }

    @Override
    public T getValue() throws VariableNotSetException {
        if (!this.set) {
            throw new VariableNotSetException();
        }

        return this.value;
    }

    @Override
    public boolean isSet() {
        return this.set;
    }

    @Override
    public Variable<T> setValue(final T value) throws InvalidValueException {
        if (!this.domain.contains(value)) {
            throw new InvalidValueException(value);
        }
        this.set = true;
        this.value = value;
        ListVariable.updateTrail.add(System.nanoTime() + " @" + this.name + ": " + this.value);
        try {
            Thread.sleep(10);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public UUID getID() {
        return this.id;
    }

    @Override
    public int getRange() {
        return this.domain.size();
    }

    @Override
    public Iterator<T> iterator() {
        return this.domain.iterator();
    }

    @Override
    public String toString() {
        return "" + this.name + ": " + (this.set ? this.value : "(unset)");
    }

}
