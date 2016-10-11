/**
 * File BinaryConstraint.java
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
package nl.coenvl.sam.constraints;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import nl.coenvl.sam.exceptions.VariableNotInvolvedException;
import nl.coenvl.sam.variables.Variable;

/**
 * BinaryConstraint
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 26 feb. 2016
 */
public abstract class BinaryConstraint<T extends Variable<V>, V> implements Constraint<T, V> {

    protected final T var1;

    protected final T var2;

    public BinaryConstraint(T var1, T var2) {
        this.var1 = var1;
        this.var2 = var2;
    }

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.constraints.Constraint#getVariables()
     */
    @Override
    public Set<UUID> getVariableIds() {
        Set<UUID> ret = new HashSet<>();
        ret.add(this.var1.getID());
        ret.add(this.var2.getID());
        return ret;
    }

    protected void assertVariableIsInvolved(T var) {
        if (!var.equals(this.var1) && !var.equals(this.var2)) {
            throw new VariableNotInvolvedException("Variable " + var.getName() + " is not involved in the constraint");
        }
    }

}
