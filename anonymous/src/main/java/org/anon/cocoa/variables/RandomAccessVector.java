/**
 * File RandomAccessVector.java
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
package org.anon.cocoa.variables;

import java.util.Random;
import java.util.Vector;

/**
 * RandomAccessVector
 *
 * @author Anomymous
 * @version 0.1
 * @since 11 mrt. 2016
 */
public class RandomAccessVector<V> extends Vector<V> {

	/**
	 *
	 */
	private static final long serialVersionUID = 67303850286701313L;

	public V randomElement() {
		if (this.size() <= 1) {
			return this.firstElement();
		}

		int i = (new Random()).nextInt(this.size());
		return this.elementAt(i);
	}

}
