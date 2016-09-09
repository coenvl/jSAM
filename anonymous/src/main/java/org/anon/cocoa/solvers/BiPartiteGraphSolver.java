/**
 * File BiPartiteGraphSolver.java
 *
 * This file is part of the jCoCoA project.
 *
 * Copyright 2016 Anonymous
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
package org.anon.cocoa.solvers;

/**
 * BiPartiteGraphSolver
 *
 * @author Anomymous
 * @version 0.1
 * @since 23 jan. 2016
 */
public interface BiPartiteGraphSolver {

	public Class<? extends BiPartiteGraphSolver> getCounterPart();

}
