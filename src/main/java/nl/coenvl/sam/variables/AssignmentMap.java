/**
 * File AssignmentMap.java
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
package nl.coenvl.sam.variables;

import java.util.UUID;

/**
 * AssignmentMap
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 11 mrt. 2016
 */
public class AssignmentMap<V> extends PublishableMap<UUID, V> {

    /**
     *
     */
    private static final long serialVersionUID = -6627005216007138557L;

    public V setAssignment(Variable<V> var, V value) {
        return super.put(var.getID(), value);
    }

    public boolean containsAssignment(Variable<V> var) {
        return super.containsKey(var.getID());
    }

    public V getAssignment(Variable<V> var) {
        return super.get(var.getID());
    }

    public V removeAssignment(Variable<V> var) {
        return super.remove(var.getID());
    }

    @Override
    public AssignmentMap<V> clone() {
        AssignmentMap<V> clone = new AssignmentMap<>();
        for (UUID key : this.keySet()) {
            clone.put(key, this.get(key));
        }
        return clone;
    }
}
