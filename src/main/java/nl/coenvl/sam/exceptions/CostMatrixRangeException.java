/**
 * File CostMatrixRangeException.java
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
package nl.coenvl.sam.exceptions;

/**
 * CostMatrixRangeException
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 26 feb. 2016
 */
public class CostMatrixRangeException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 7312424837820215430L;

    public CostMatrixRangeException(String msg) {
        super(msg);
    }

}
