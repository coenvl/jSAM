/**
 * File UniqueFirstCooperativeSolver.java
 *
 * This file is part of the jSAM project 2014.
 *
 * Copyright 2014 Coen van Leeuwen
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

import java.util.Map.Entry;
import java.util.UUID;

import nl.coenvl.sam.agents.Agent;
import nl.coenvl.sam.variables.DiscreteVariable;

/**
 * UniqueFirstCooperativeSolver
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 11 apr. 2014
 *
 */
public class CoCoAWPTSolver<V> extends CoCoASolver<V> {

    public CoCoAWPTSolver(final Agent<DiscreteVariable<V>, V> agent) {
        super(agent);
    }

    /**
     * Instead of "normal" picking a value, make sure only 1 active neighbor selects that value at a time.
     */
    @Override
    protected void pickValue() {
        if (this.getLargerActiveNeighbors() > 0) {
            super.sendInquireMsgs();
        } else {
            super.pickValue();
        }
    }

    /**
     * @return
     */
    private int getLargerActiveNeighbors() {
        int activeNeighbors = 0;

        for (final Entry<UUID, State> neighborState : this.neighborStates.entrySet()) {
            if ((neighborState.getKey().compareTo(this.myVariable.getID()) > 0)
                    && (neighborState.getValue() == State.ACTIVE)) {
                activeNeighbors++;
            }
        }

        return activeNeighbors;
    }
}
