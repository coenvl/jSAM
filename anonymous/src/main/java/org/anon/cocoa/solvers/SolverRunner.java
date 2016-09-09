/**
 * File SolverRunner.java
 *
 * This file is part of the jCoCoA project 2014.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.anon.cocoa.solvers;

import java.util.concurrent.LinkedBlockingQueue;

import org.anon.cocoa.messages.Message;

/**
 * SolverRunner
 *
 * Wrapper for around a solver to make it run asynchronously
 *
 * @author Anomymous
 * @version 0.1
 * @since 4 apr. 2014
 *
 */
public class SolverRunner implements IterativeSolver {

	private class Runner implements Runnable {

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
					Message m = SolverRunner.this.queue.take();
					synchronized (this) {
						SolverRunner.this.mySolver.push(m);
					}
				}
			} catch (InterruptedException e) {
				// Do nothing... we are simply reset
				// throw new RuntimeException();
			}
		}

	}

	private static int solverRunnerCounter = 0;

	private Runner myRunner;
	private volatile Solver mySolver;
	private Thread myThread;
	private LinkedBlockingQueue<Message> queue;

	private final String threadName;

	/**
	 * @param greedySolver
	 */
	public SolverRunner(Solver solver) {
		this.queue = new LinkedBlockingQueue<>();
		this.mySolver = solver;
		this.threadName = "SolverRunnerThread-" + SolverRunner.solverRunnerCounter++;
	}

	public SolverRunner(Solver s, String threadName) {
		this.queue = new LinkedBlockingQueue<>();
		this.mySolver = s;
		this.threadName = threadName;
		SolverRunner.solverRunnerCounter++;
	}

	public void startThread() {
		this.myRunner = new Runner();
		this.myThread = new Thread(null, this.myRunner, this.threadName);
		// this.myThread = new Thread(this.myRunner);
		this.myThread.start();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.anon.cocoa.Solver#init()
	 */

	@Override
	public void init() {
		this.mySolver.init();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.anon.cocoa.Solver#push(org.anon.cocoa.Message)
	 */
	@Override
	public void push(Message m) {
		this.queue.add(m);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.anon.cocoa.solvers.Solver#tick()
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
	 * @see org.anon.cocoa.Solver#reset()
	 */
	@Override
	public void reset() {
		if (this.myRunner != null) {
			this.myRunner.running = false;
		}

		if (this.myThread != null) {
			this.myThread.interrupt();

			try {
				this.myThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			this.myThread = null;
		}
		this.mySolver.reset();
		this.queue.clear();
	}

	/**
	 * @return
	 */
	public boolean emptyQueue() {
		return this.queue.isEmpty();
	}
}
