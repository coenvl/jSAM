/**
 * File SolverAgent.java
 *
 * Copyright 2017 TNO
 */
package nl.coenvl.sam.agents;

import nl.coenvl.sam.solvers.IterativeSolver;
import nl.coenvl.sam.solvers.Solver;
import nl.coenvl.sam.variables.Variable;

/**
 * SolverAgent
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 14 apr. 2017
 */
public abstract class SolverAgent<T extends Variable<V>, V> extends AbstractAgent<T, V> implements IterativeSolver {

    /**
     * Just to make it more obvious what the "synchronicity" boolean value means.
     */
    public static final boolean SINGLE_THREADED = true;
    public static final boolean MULTI_THREADED = false;

    /**
     * Defines whether different solvers are run in different threads. This means that if synchronous is set to true,
     * all solvers will execute in series, whereas if it is false, all solvers will run in parallel.
     */
    protected final boolean synchronous;

    /**
     * @param name
     * @param var
     */
    protected SolverAgent(final T var, final String name, final boolean synchronous) {
        super(var, name);
        this.synchronous = synchronous;
    }

    /**
     * @param name
     * @param var
     */
    protected SolverAgent(final T var, final String name) {
        this(var, name, SolverAgent.MULTI_THREADED);
    }

    public abstract void setSolver(final Solver solver);

}
