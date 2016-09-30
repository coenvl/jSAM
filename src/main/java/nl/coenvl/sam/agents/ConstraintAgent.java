/**
 * File ConstraintAgent.java
 *
 * Copyright 2016 TNO
 */
package nl.coenvl.sam.agents;

import java.util.UUID;

import nl.coenvl.sam.variables.Variable;

/**
 * ConstraintAgent
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 23 sep. 2016
 */
public interface ConstraintAgent<T extends Variable<V>, V> extends Agent<T, V> {

    /**
     * @return
     */
    public UUID getID();

    /**
     * @param id
     * @return
     */
    public T getVariableWithID(UUID id);

}
