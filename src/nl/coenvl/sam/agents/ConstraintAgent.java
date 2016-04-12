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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.coenvl.sam.agents;

import java.util.Set;
import java.util.UUID;

import nl.coenvl.sam.MailMan;
import nl.coenvl.sam.constraints.BiPartiteConstraint;
import nl.coenvl.sam.constraints.Constraint;
import nl.coenvl.sam.exceptions.VariableNotInvolvedException;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.solvers.Solver;
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
public class ConstraintAgent<T extends Variable<V>, V> extends AbstractPropertyOwner implements Agent<T, V> {

	private final UUID address;
	private final String name;
	private final BiPartiteConstraint<T, V> myConstraint;

	Solver mySolver;

	/**
	 * @param name
	 * @param var
	 */
	public ConstraintAgent(String name, BiPartiteConstraint<T, V> constraint) {
		super();
		this.name = name;
		this.myConstraint = constraint;
		this.address = UUID.randomUUID();
		MailMan.register(this.address, this);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nl.coenvl.sam.Agent#init()
	 */
	@Override
	public final synchronized void init() {
		this.mySolver.init();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nl.coenvl.sam.Agent#push(nl.coenvl.sam.Message)
	 */
	@Override
	public final synchronized void push(Message m) {
		this.mySolver.push(m);
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

	public final void setSolver(Solver solver) {
		this.mySolver = new SolverRunner(solver);
	}

	public final void setSolver(Solver solver, boolean asynchronous) {
		if (asynchronous) {
			this.mySolver = new SolverRunner(solver);
		} else {
			// System.err.println("Warning: You are using a synchronous solver!");
			this.mySolver = solver;
		}
	}

	public T getVariable(UUID id) {
		if (this.myConstraint.getFrom().getID().equals(id)) {
			return this.myConstraint.getFrom();
		} else if (this.myConstraint.getTo().getID().equals(id)) {
			return this.myConstraint.getTo();
		} else {
			throw new VariableNotInvolvedException("Variable not part of Constraint!");
		}
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
	public UUID getID() {
		return this.address;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nl.coenvl.sam.agents.Agent#getConstraintIds()
	 */
	@Override
	public Set<UUID> getConstraintIds() {
		return this.myConstraint.getVariableIds();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nl.coenvl.sam.agents.Agent#getLocalCost()
	 */
	@Override
	public double getLocalCost() {
		return this.myConstraint.getCost(this.myConstraint.getFrom())
				+ this.myConstraint.getCost(this.myConstraint.getTo());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nl.coenvl.sam.agents.Agent#getLocalCostIf(nl.coenvl.sam.variables.AssignmentMap)
	 */
	@Override
	public double getLocalCostIf(AssignmentMap<V> valueMap) {
		return this.myConstraint.getCostIf(this.myConstraint.getFrom(), valueMap)
				+ this.myConstraint.getCostIf(this.myConstraint.getTo(), valueMap);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nl.coenvl.sam.agents.Agent#getVariable()
	 */
	@Override
	public T getVariable() {
		return null;
		// throw new UnsupportedOperationException("Cannot get the variable of ConstraintAgent");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nl.coenvl.sam.agents.Agent#addConstraint(nl.coenvl.sam.constraints.Constraint)
	 */
	@Override
	public void addConstraint(Constraint<T, V> c) {
		throw new UnsupportedOperationException("Cannot add Constraints to ConstraintAgent");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nl.coenvl.sam.agents.Agent#removeConstraint(nl.coenvl.sam.constraints.Constraint)
	 */
	@Override
	public void removeConstraint(Constraint<T, V> c) {
		throw new UnsupportedOperationException("Cannot remove Constraints to ConstraintAgent");
	}

}
