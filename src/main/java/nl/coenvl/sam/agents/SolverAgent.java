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
     * Just to make it more obvious what the "singleThreaded" boolean value means.
     */
    public static final boolean SINGLE_THREADED = true;
    public static final boolean MULTI_THREADED = false;

    /**
     * Defines whether different solvers are run in different threads. This means that if synchronous is set to true,
     * all solvers will execute in series, whereas if it is false, all solvers will run in parallel.
     */
    protected final boolean singleThreaded;

    /**
     * Just to make it more obvious what the "rootedActivation" boolean value means.
     */
    public static final boolean ROOTING_ACTIVATION = true;
    public static final boolean SIMULTANEOUS_ACTIVATION = false;

    protected final boolean rootedActivation;

    /**
     * @param name
     * @param var
     */
    protected SolverAgent(final T var, final String name, final boolean synchronous, final boolean activation) {
        super(var, name);
        this.singleThreaded = synchronous;
        this.rootedActivation = activation;
    }

    /**
     * @param name
     * @param var
     */
    protected SolverAgent(final T var, final String name) {
        this(var, name, SolverAgent.MULTI_THREADED, SolverAgent.SIMULTANEOUS_ACTIVATION);
    }

    public abstract void setSolver(final Solver solver);

}
