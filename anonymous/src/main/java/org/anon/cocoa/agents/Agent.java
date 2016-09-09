/**
 * File Agent.java
 *
 * This file is part of the jCoCoA project 2014.
 *
 * Copyright 2014 Anomymous
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
package org.anon.cocoa.agents;

import java.util.Set;
import java.util.UUID;

import org.anon.cocoa.constraints.Constraint;
import org.anon.cocoa.solvers.Solver;
import org.anon.cocoa.variables.AssignmentMap;
import org.anon.cocoa.variables.Variable;

/**
 * Agent
 *
 * @author Anomymous
 * @version 0.1
 * @since 4 feb. 2014
 *
 */
public interface Agent<T extends Variable<V>, V> extends PropertyOwner, Solver {

    public String getName();

    public T getVariable();

    public void addConstraint(Constraint<T, V> c);

    public void removeConstraint(Constraint<T, V> c);

    public double getLocalCost();

    public double getLocalCostIf(AssignmentMap<V> valueMap);

    public Set<UUID> getConstrainedVariableIds();

    public boolean isFinished();

    public Constraint<T, V> getConstraintForAgent(UUID target);

}
