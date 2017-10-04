/**
 * File SolverRunner.java
 *
 * This file is part of the jSAM project 2014.
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
package nl.coenvl.sam.solvers;

import java.util.concurrent.LinkedBlockingQueue;

import nl.coenvl.sam.messages.Message;

/**
 * SolverRunner
 *
 * Wrapper for around a solver to make it run asynchronously
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 4 apr. 2014
 *
 */
public class SolverRunner implements IterativeSolver {

    protected final class Runner implements Runnable {

        private volatile boolean running;

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            this.running = true;

            try {
                while (this.running) {
                    final Message m = SolverRunner.this.queue.take();
                    synchronized (this) {
                        SolverRunner.this.mySolver.push(m);
                    }
                }
            } catch (final InterruptedException e) {
                // Do nothing... we are simply reset
                // throw new RuntimeException();
            }
        }

        /**
         *
         */
        public void stop() {
            this.running = false;
        }

    }

    private static int solverRunnerCounter = 0;

    private Runner myRunner;
    protected volatile Solver mySolver;
    private Thread myThread;
    protected final LinkedBlockingQueue<Message> queue;

    private final String threadName;

    /**
     * Creates a SolverRunner for the provided Solver, and uses a default generated Thread name.
     *
     * @param solver The solver to run
     */
    public SolverRunner(final Solver solver) {
        this(solver, "SolverRunnerThread-" + SolverRunner.solverRunnerCounter++);
    }

    /**
     * Create a SolverRunner for the provided Solver, which will have an internal Thread with the given thread name.
     *
     * @param s The solver to run
     * @param threadName The name of the internal thread which will be started by {@link #startThread()}
     */
    public SolverRunner(final Solver s, final String threadName) {
        this.queue = new LinkedBlockingQueue<>();
        this.mySolver = s;
        this.threadName = threadName;
        SolverRunner.solverRunnerCounter++;
        this.startThread();
    }

    /**
     * Starts the internal thread
     */
    private void startThread() {
        this.myRunner = new Runner();
        this.myThread = new Thread(null, this.myRunner, this.threadName);
        // this.myThread = new Thread(this.myRunner);
        this.myThread.start();
    }

    /**
     * Gracefully stops the internal running thread
     */
    public void stopThread() {
        if (this.myRunner != null) {
            this.myRunner.stop();
        }

        if (this.myThread != null) {
            this.myThread.interrupt();

            try {
                this.myThread.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            this.myThread = null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.Solver#init()
     */

    @Override
    public void init() {
        this.mySolver.init();
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.Solver#push(nl.coenvl.sam.Message)
     */
    @Override
    public void push(final Message m) {
        this.queue.add(m);
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.solvers.Solver#tick()
     */
    @Override
    public void tick() {
        if (this.mySolver instanceof IterativeSolver) {
            ((IterativeSolver) this.mySolver).tick();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.Solver#reset()
     */
    @Override
    public void reset() {
        this.stopThread();
        this.mySolver.reset();
        this.queue.clear();
    }

    /**
     * Returns wether the internal queue of the solver runner is empty.
     *
     * @return true is the Queue is empty
     */
    public boolean emptyQueue() {
        return this.queue.isEmpty();
    }

    /**
     * Returns whether the thread is non-null. Assumes the thread is only set in {@link #startThread()} and immediately
     * started after.
     *
     * @return true if the thread has started
     */
    public boolean started() {
        return this.myThread != null;
    }
}
