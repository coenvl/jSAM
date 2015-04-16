/**
 * File GreedyCooperativeSolver.java
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.coenvl.sam.agents.Agent;
import nl.coenvl.sam.agents.LocalCommunicatingAgent;
import nl.coenvl.sam.costfunctions.CostFunction;
import nl.coenvl.sam.exceptions.InvalidValueException;
import nl.coenvl.sam.messages.HashMessage;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.problemcontexts.LocalProblemContext;
import nl.coenvl.sam.variables.IntegerVariable;
import nl.coenvl.sam.variables.IntegerVariable.IntegerVariableIterator;

/**
 * GreedyCooperativeSolver
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 11 apr. 2014
 * 
 */
@Deprecated
@SuppressWarnings("unchecked")
public class GreedyCooperativeSolver implements Solver {

	private static enum State {
		ACTIVE, DONE, IDLE
	};

	private static final String ASSIGN_VAR = "GreedyCooperativeSolver:PickAVar";

	private static final String COST_MSG = "GreedyCooperativeSolver:CostOfAssignments";
	private static final String INQUIRE_MSG = "GreedyCooperativeSolver:InquireAssignment";
	private static final String VAR_PICKED = "GreedyCooperativeSolver:VarPicked";
	private final Set<Agent> activatedNeighbors;

	private Agent activator;

	private volatile LocalProblemContext<Integer> context;

	private final CostFunction costfun;

	private volatile State currentState;

	private final IntegerVariable myVariable;

	private final LocalCommunicatingAgent parent;

	private volatile List<HashMap<Integer, Double>> receivedMaps;

	public GreedyCooperativeSolver(LocalCommunicatingAgent parent,
			CostFunction costfun) {
		this.parent = parent;
		this.costfun = costfun;
		// Assume we DO always have an integer variable
		this.myVariable = (IntegerVariable) parent.getVariable();
		this.activatedNeighbors = new HashSet<Agent>();
	}

	private void activateNextNeighbor() {
		HashMessage nextMessage = new HashMessage(ASSIGN_VAR);
		nextMessage.addContent("cpa", context.getAssignment().clone());
		nextMessage.addContent("source", this.parent);

		// Iterate over the set until we found a non-activated neighbor
		for (Agent neighbor : this.parent.getNeighborhood())
			if (!this.activatedNeighbors.contains(neighbor)) {
				this.activatedNeighbors.add(neighbor);
				neighbor.push(nextMessage);
				return;
			}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.Solver#init()
	 */
	@Override
	public void init() {
		this.currentState = State.IDLE;
		context = new LocalProblemContext<Integer>(this.parent);
		this.activatedNeighbors.clear();
		// this.sendInquireMsgs();
	}

	/**
	 * @param m
	 */
	private synchronized void processCostMessage(Message m) {
		{
			HashMap<Integer, Double> costMap = (HashMap<Integer, Double>) m
					.getContent("costMap");
			this.receivedMaps.add(costMap);
		}

		if (this.receivedMaps.size() < this.parent.getNeighborhood().size()) {
			return;
		} else {
			// Gather all of the results and get the best assignment for me
			IntegerVariableIterator iter = this.myVariable.iterator();

			double bestCost = Double.MAX_VALUE;
			Integer bestAssignment = this.myVariable.getUpperBound();

			LocalProblemContext<Integer> pa = new LocalProblemContext<Integer>(
					this.parent);
			pa.setAssignment(this.context.getAssignment());

			while (iter.hasNext()) {
				Integer iterAssignment = iter.next();
				double totalCost = 0;

				for (HashMap<Integer, Double> neighborMap : this.receivedMaps) {
					Double neighborCost = neighborMap.get(iterAssignment);
					totalCost += neighborCost;
				}

				// Add my OWN cost
				pa.setValue(iterAssignment);
				double localCost = costfun.evaluate(pa);

				totalCost += localCost;

				if (totalCost < bestCost) {
					bestCost = totalCost;
					bestAssignment = iterAssignment;
				}
			}

			try {
				this.myVariable.setValue(bestAssignment);
				context.setValue(bestAssignment);
			} catch (InvalidValueException e) {
				throw new RuntimeException(e);
			}

			// At this moment we are done
			this.currentState = State.DONE;

			// Tell it to our activator
			if (this.activator != null) {
				Message pickedMsg = new HashMessage(VAR_PICKED);
				pickedMsg.addContent("cpa", this.context.getAssignment()
						.clone());
				this.activator.push(pickedMsg);
			}

			activateNextNeighbor();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.anon.cocoa.solvers.Solver#push(org.anon.cocoa.messages.Message)
	 */
	@Override
	public synchronized void push(Message m) {

		if (m.hasContent("cpa")) {
			if (context == null)
				context = new LocalProblemContext<Integer>(this.parent);

			context.setAssignment((HashMap<Agent, Integer>) m.getContent("cpa"));
		}

		if (m.getType().equals(GreedyCooperativeSolver.ASSIGN_VAR)) {
			Agent source = (Agent) m.getContent("source");
			if (this.currentState != State.IDLE) {
				source.push(new HashMessage(VAR_PICKED));
			} else {
				this.activator = source;
				this.currentState = State.ACTIVE;
				this.sendInquireMsgs();
			}
		} else if (m.getType().equals(GreedyCooperativeSolver.INQUIRE_MSG)) {
			this.respond(m);
		} else if (m.getType().equals(GreedyCooperativeSolver.COST_MSG)) {
			if (this.currentState != State.ACTIVE)
				return;

			this.processCostMessage(m);
		} else if (m.getType().equals(GreedyCooperativeSolver.VAR_PICKED)) {
			activateNextNeighbor();
		} else {
			System.err.println("Unexpected message of type " + m.getType());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.coenvl.sam.solvers.Solver#reset()
	 */
	@Override
	public void reset() {
		this.myVariable.clear();
		this.activatedNeighbors.clear();
		this.receivedMaps = null;
		this.context = null;
		this.currentState = State.IDLE;
	}

	/**
	 * Iterate over all possible assignments for the source
	 * 
	 * @param m
	 */
	private synchronized void respond(Message m) {
		HashMap<Integer, Double> costMap = new HashMap<Integer, Double>();

		Agent source = (Agent) m.getContent("source");
		LocalProblemContext<Integer> pa = new LocalProblemContext<Integer>(
				source);
		pa.setAssignment(context.getAssignment());

		// Build the cost map
		// Making the strong assumption that I have the same type of variable as
		// the source
		IntegerVariableIterator iter = this.myVariable.iterator();
		while (iter.hasNext()) {
			Integer iterAssignment = iter.next();
			pa.setValue(iterAssignment);

			Double iterCost = Double.MAX_VALUE;
			if (myVariable.isSet()) {
				double localCost = this.costfun.evaluate(pa);

				if (localCost < iterCost)
					iterCost = localCost;
			} else {
				// Now the internal loop to optimize MY value
				LocalProblemContext<Integer> spa = new LocalProblemContext<Integer>(
						this.parent);
				spa.setAssignment(pa.getAssignment());
				IntegerVariableIterator siter = this.myVariable.iterator();

				while (siter.hasNext()) {
					Integer siterAssignment = siter.next();
					spa.setValue(siterAssignment);
					double siterCost = this.costfun.evaluate(spa);

					if (siterCost < iterCost)
						iterCost = siterCost;
				}
			}

			costMap.put(iterAssignment, iterCost);
		}

		// Respond to source
		Message response = new HashMessage(GreedyCooperativeSolver.COST_MSG);
		response.addContent("costMap", costMap);
		source.push(response);
	}

	private synchronized void sendInquireMsgs() {
		// Create a map for storing incoming costmap messages
		this.receivedMaps = new ArrayList<HashMap<Integer, Double>>();

		Message m = new HashMessage(INQUIRE_MSG);
		m.addContent("cpa", this.context.getAssignment().clone());
		m.addContent("source", this.parent);

		for (Agent neighbor : this.parent.getNeighborhood())
			neighbor.push(m);
	}

}
