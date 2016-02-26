/**
 * File SolverRunner.java
 *
 * This file is part of the jSAM project 2014.
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
package nl.coenvl.sam.solvers;

import java.util.concurrent.LinkedBlockingQueue;

import nl.coenvl.sam.messages.HashMessage;
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
@SuppressWarnings("synthetic-access")
public class SolverRunner implements Solver {

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

			mySolver.init();

			try {
				while (this.running) {
					Message m = queue.take();
					mySolver.push(m);
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
		this.queue = new LinkedBlockingQueue<Message>();
		this.mySolver = solver;
		this.threadName = "SolverRunnerThread-" + solverRunnerCounter++;
	}

	public SolverRunner(Solver s, String threadName) {
		this.queue = new LinkedBlockingQueue<Message>();
		this.mySolver = s;
		this.threadName = threadName;
		solverRunnerCounter++;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.Solver#init()
	 */
	
	@Override
	public void init() {
		this.myRunner = new Runner();
		this.myThread = new Thread(null, myRunner, threadName);
		// this.myThread = new Thread(this.myRunner);
		this.myThread.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.Solver#push(nl.coenvl.sam.Message)
	 */
	@Override
	public void push(Message m) {
		this.queue.add(m);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.Solver#reset()
	 */
	@Override
	public void reset() {
		if (this.myRunner != null)
			this.myRunner.running = false;
		
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
}
