/**
 * File Solver.java
 *
 * This file is part of the jSAM project 2014.
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
package nl.coenvl.sam.solvers;

import nl.coenvl.sam.messages.Message;

/**
 * Solver
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 19 mrt. 2014
 *
 */
public interface Solver {

	/**
	 * If the solver sets the variable as part of it's initialization it should occur in this function. Depending on the
	 * implementation it may or may not do this. It is imperative that everything OTHER than this should not be part of
	 * this function, because it may not be called if this algorithm is not considered an "initialization" algorithm
	 */
	public void init();

	public void push(Message m);

	/**
	 * A call to the reset function should reset the solver to the state how it was exactly after it was constructed.
	 * All fields should be initialized, and calling it immediately after construction should yield no effect.
	 */
	public void reset();

}
