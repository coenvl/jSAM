/**
 * File MaxSumADVPFunctionSolver.java
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

import java.util.ArrayList;
import java.util.UUID;

import nl.coenvl.sam.MailMan;
import nl.coenvl.sam.agents.ConstraintAgent;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.variables.AssignmentMap;
import nl.coenvl.sam.variables.DiscreteVariable;

/**
 * MaxSumADVPFunctionSolver
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 22 jan. 2016
 */
public class MaxSumADVPFunctionSolver<T extends DiscreteVariable<V>, V> extends MaxSumADFunctionSolver<T, V> {

    // To make this one generic, we have to forward not values, but valueMaps since there are publishables, and discrete
    // values are not

    private final AssignmentMap<V> knownValues;

    public MaxSumADVPFunctionSolver(final ConstraintAgent<T, V> agent) {
        super(agent);
        this.knownValues = new AssignmentMap<>();
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.solvers.Solver#push(nl.coenvl.sam.messages.Message)
     */
    @Override
    public synchronized void push(final Message m) {
        super.push(m);

        if (m.containsKey("value")) {
            @SuppressWarnings("unchecked")
            final V value = (V) m.get("value");
            this.knownValues.put(m.getSource(), value);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.solvers.Solver#reset()
     */
    @Override
    public void reset() {
        super.reset();
        this.knownValues.clear();
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
        this.iterCount++;
        if ((this.iterCount % MaxSumADFunctionSolver.REVERSE_AFTER_ITERS) == 0) {
            this.direction = !this.direction;
        }

        // Only works for binary constraints
        // assert (super.numNeighbors() == 2);

        for (final UUID target : this.parent.getConstrainedVariableIds()) {
            if ((target.hashCode() > this.parent.hashCode()) == this.direction) {
                continue;
            }

            final Message f2vadvp = this.fun2varmessage(target);
            MailMan.sendMessage(target, f2vadvp);
        }

        // this.receivedCosts.clear();
    }

    @Override
    protected double findMin(final AssignmentMap<V> temp, final ArrayList<UUID> neighbors, final int i) {
        if (neighbors.size() == i) {
            return this.parent.getLocalCostIf(temp);
        } else {
            final UUID neighbor = neighbors.get(i);
            double bestCost = Double.MAX_VALUE;

            if (this.knownValues.containsKey(neighbor)) {
                final V val = this.knownValues.get(neighbor);

                temp.put(neighbor, val);
                double cost = this.findMin(temp, neighbors, i + 1);

                if (this.receivedCosts.containsKey(neighbor) && this.receivedCosts.get(neighbor).containsKey(val)) {
                    cost += this.receivedCosts.get(neighbor).get(val);
                }

                return cost;
            } else {
                for (final V val : this.constraintAgent.getVariableWithID(neighbor)) {
                    temp.put(neighbor, val);
                    double cost = this.findMin(temp, neighbors, i + 1);

                    if (this.receivedCosts.containsKey(neighbor) && this.receivedCosts.get(neighbor).containsKey(val)) {
                        cost += this.receivedCosts.get(neighbor).get(val);
                    }

                    if (cost < bestCost) {
                        bestCost = cost;
                    }
                }
                return bestCost;
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.solvers.BiPartiteGraphSolver#getCounterPart()
     */
    @Override
    public Class<? extends BiPartiteGraphSolver> getCounterPart() {
        return MaxSumADVPVariableSolver.class;
    }

}
