/**
 * File ExperimentControl.java
 *
 * This file is part of the jSAM project 2014.
 * 
 * Copyright 2014 Coen van Leeuwen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package nl.coenvl.sam;

import nl.coenvl.sam.agents.AbstractAgent;
import nl.coenvl.sam.agents.AbstractSolverAgent;
import nl.coenvl.sam.agents.OrderedSolverAgent;
import nl.coenvl.sam.costfunctions.CompareCounter;

/**
 * ExperimentControl
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 4 apr. 2014
 * 
 */
public class ExperimentControl {

	public static void ResetExperiment() {
		AbstractAgent.destroyAgents();
//		HashMessage.resetCount();
		AbstractSolverAgent.resetMessageCount();
		CompareCounter.reset();
		OrderedSolverAgent.maxSequenceID = 0;
	}
	
	public static int getNumberEvals() {
		return CompareCounter.getComparisons();
	}

}
