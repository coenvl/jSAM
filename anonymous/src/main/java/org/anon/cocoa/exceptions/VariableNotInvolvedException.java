/**
 * File VariableNotInvolvedException.java
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
package org.anon.cocoa.exceptions;

/**
 * VariableNotInvolvedException
 *
 * @author Anomymous
 * @version 0.1
 * @since 26 feb. 2016
 */
public class VariableNotInvolvedException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = 5657616732601999531L;

	/**
	 * @param string
	 */
	public VariableNotInvolvedException(String msg) {
		super(msg);
	}

}
