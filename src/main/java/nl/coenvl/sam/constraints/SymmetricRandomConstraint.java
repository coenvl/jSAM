/**
 * File SymmetricRandomConstraint.java
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

import nl.coenvl.sam.variables.DiscreteVariable;

/**
 * SymmetricRandomConstraint
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 4 mrt. 2016
 */
public class SymmetricRandomConstraint<V> extends CostMatrixConstraint<V> {

    /**
     * @param var1
     * @param var2
     */
    public SymmetricRandomConstraint(DiscreteVariable<V> var1, DiscreteVariable<V> var2) {
        super(var1, var2, RandomConstraint.randomMatrix(var1.getRange(), var2.getRange()));
    }

}
