/**
 * File MaxSumFunctionSolver.java
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
package nl.coenvl.sam.solvers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import nl.coenvl.sam.MailMan;
import nl.coenvl.sam.agents.ConstraintAgent;
import nl.coenvl.sam.messages.HashMessage;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.variables.AssignmentMap;
import nl.coenvl.sam.variables.CostMap;
import nl.coenvl.sam.variables.DiscreteVariable;

/**
 * MaxSumFunctionSolver
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 22 jan. 2016
 */
public class MaxSumFunctionSolver<T extends DiscreteVariable<V>, V> extends AbstractSolver<T, V>
        implements IterativeSolver, BiPartiteGraphSolver {

    protected final ConstraintAgent<T, V> constraintAgent;
    protected Map<UUID, CostMap<V>> receivedCosts;

    public MaxSumFunctionSolver(ConstraintAgent<T, V> agent) {
        super(agent);
        this.constraintAgent = agent;
        this.receivedCosts = new HashMap<>();
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.solvers.Solver#init()
     */
    @Override
    public void init() {
        // Do nothing?
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.solvers.Solver#push(nl.coenvl.sam.messages.Message)
     */
    @Override
    public synchronized void push(Message m) {
        if (m.getType().equals("VAR2FUN")) {
            UUID neighbor = m.getSource();
            @SuppressWarnings("unchecked")
            CostMap<V> costMap = (CostMap<V>) m.getMap("costMap");
            this.receivedCosts.put(neighbor, costMap);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.solvers.Solver#reset()
     */
    @Override
    public void reset() {
        this.receivedCosts.clear();
    }

    /*
     * A message sent from a function-node f to a variable-node x in iteration i includes for each possible value d \in
     * Dx the minimal cost of any combination of assignments to the variables involved in f apart from x and the
     * assignment of value d to variable x.
     *
     * @see nl.coenvl.sam.solvers.IterativeSolver#tick()
     */
    @Override
    public synchronized void tick() {
        // Only works for binary constraints
        assert (super.numNeighbors() == 2);

        for (UUID target : this.parent.getConstrainedVariableIds()) {
            Message f2v = this.fun2varmessage(target);
            MailMan.sendMessage(target, f2v);
        }

        this.receivedCosts.clear();
    }

    protected Message fun2varmessage(UUID target) {
        AssignmentMap<V> temp = new AssignmentMap<>();

        // For all values of variable
        CostMap<V> costMap = new CostMap<>();
        for (V value : this.constraintAgent.getVariableWithID(target)) {
            temp.put(target, value);

            double minCost = Double.MAX_VALUE;
            // Now we know there is only one other neighbor, so iterate for him
            for (UUID other : this.parent.getConstrainedVariableIds()) {
                if (other == target) {
                    continue;
                }

                // if (minCost < Double.MAX_VALUE) {
                // throw new InvalidValueException(
                // "The min cost could not be lowered already, more than one agent in constraint?");
                // }

                for (V val2 : this.constraintAgent.getVariableWithID(other)) {
                    temp.put(other, val2);
                    double cost = this.parent.getLocalCostIf(temp);

                    if (this.receivedCosts.containsKey(other) && this.receivedCosts.get(other).containsKey(val2)) {
                        cost += this.receivedCosts.get(other).get(val2);
                    }

                    if (cost < minCost) {
                        minCost = cost;
                    }
                }
            }

            // The following can hold if there are no other variables, i.e. I a am a unary constraint
            if (minCost == Double.MAX_VALUE) {
                minCost = this.parent.getLocalCostIf(temp);
            }

            costMap.put(value, minCost);
        }

        Message msg = new HashMessage(this.constraintAgent.getID(), "FUN2VAR");
        msg.put("costMap", costMap);

        return msg;
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.solvers.BiPartiteGraphSolver#getCounterPart()
     */
    @Override
    public Class<? extends BiPartiteGraphSolver> getCounterPart() {
        return MaxSumVariableSolver.class;
    }

}
