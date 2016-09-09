/**
 * File MaxSumVariableSolver.java
 *
 * This file is part of the jCoCoA project.
 *
 * Copyright 2016 Anonymous
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
package org.anon.cocoa.solvers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.anon.cocoa.MailMan;
import org.anon.cocoa.agents.VariableAgent;
import org.anon.cocoa.messages.HashMessage;
import org.anon.cocoa.messages.Message;
import org.anon.cocoa.variables.CostMap;
import org.anon.cocoa.variables.IntegerVariable;

/**
 * MaxSumVariableSolver
 *
 * @author Anomymous
 * @version 0.1
 * @since 22 jan. 2016
 */
public class MaxSumVariableSolver extends AbstractSolver<IntegerVariable, Integer>
        implements IterativeSolver, BiPartiteGraphSolver {

    private final Map<UUID, CostMap<Integer>> receivedCosts;
    protected final VariableAgent<IntegerVariable, Integer> variableAgent;

    public MaxSumVariableSolver(VariableAgent<IntegerVariable, Integer> agent) {
        super(agent);
        this.variableAgent = agent;
        this.receivedCosts = new HashMap<>();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.anon.cocoa.solvers.Solver#init()
     */
    @Override
    public void init() {
        // Do nothing?
    }

    /*
     * (non-Javadoc)
     *
     * @see org.anon.cocoa.solvers.Solver#push(org.anon.cocoa.messages.Message)
     */
    @Override
    public synchronized void push(Message m) {

        if (m.getType().equals("FUN2VAR")) {
            UUID neighbor = m.getSource();
            @SuppressWarnings("unchecked")
            CostMap<Integer> costMap = (CostMap<Integer>) m.getMap("costMap");
            this.receivedCosts.put(neighbor, costMap);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.anon.cocoa.solvers.Solver#reset()
     */
    @Override
    public void reset() {
        this.receivedCosts.clear();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.anon.cocoa.solvers.IterativeSolver#tick()
     */
    @Override
    public synchronized void tick() {
        // Target represents function node f
        for (UUID target : this.variableAgent.getFunctionAdresses()) {
            Message v2f = this.var2funMessage(target);

            MailMan.sendMessage(target, v2f);
        }

        this.setMinimizingValue();
        this.receivedCosts.clear();
    }

    protected Message var2funMessage(UUID target) {
        // For all values of variable
        CostMap<Integer> costMap = new CostMap<>();
        double totalCost = 0;

        for (Integer value : this.myVariable) {
            double valueCost = 0;

            // The sum of costs for this value it received from all function neighbors apart from f in iteration i −
            // 1.
            for (UUID neighbor : this.variableAgent.getFunctionAdresses()) {
                if (neighbor != target) {
                    if (this.receivedCosts.containsKey(neighbor)
                            && this.receivedCosts.get(neighbor).containsKey(value)) {
                        valueCost += this.receivedCosts.get(neighbor).get(value);
                    }
                }
            }

            totalCost += valueCost;
            costMap.put(value, valueCost);
        }

        // Normalize to avoid increasingly large values
        double avg = totalCost / costMap.size();
        for (Integer value : this.myVariable) {
            costMap.put(value, costMap.get(value) - avg);
        }

        Message msg = new HashMessage(this.myVariable.getID(), "VAR2FUN");
        msg.put("costMap", costMap);
        return msg;
    }

    protected void setMinimizingValue() {
        double minCost = Double.MAX_VALUE;
        Integer bestAssignment = null;

        for (Integer value : this.myVariable) {
            double valueCost = 0;
            for (UUID neighbor : this.variableAgent.getFunctionAdresses()) {
                if (this.receivedCosts.containsKey(neighbor) && this.receivedCosts.get(neighbor).containsKey(value)) {
                    valueCost += this.receivedCosts.get(neighbor).get(value);
                }
            }

            if (valueCost < minCost) {
                minCost = valueCost;
                bestAssignment = value;
            }
        }
        if (bestAssignment != null) {
            this.myVariable.setValue(bestAssignment);
        } else {
            // Do nothing
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.anon.cocoa.solvers.BiPartiteGraphSolver#getCounterPart()
     */
    @Override
    public Class<? extends BiPartiteGraphSolver> getCounterPart() {
        return MaxSumFunctionSolver.class;
    }
}
