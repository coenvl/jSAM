/**
 * File AbstractPropertyOwner.java
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
package org.anon.cocoa.agents;

import java.util.HashMap;
import java.util.Map;

import org.anon.cocoa.exceptions.InvalidPropertyException;
import org.anon.cocoa.exceptions.PropertyNotSetException;

/**
 * AbstractPropertyOwner
 *
 * @author Anomymous
 * @version 0.1
 * @since 18 mrt. 2016
 */
public abstract class AbstractPropertyOwner implements PropertyOwner {

	private final Map<String, Object> properties;

	protected AbstractPropertyOwner() {
		this.properties = new HashMap<>();
	}

	@Override
	public final boolean has(String key) {
		return this.properties.containsKey(key);
	}

	@Override
	public Object get(String key) throws PropertyNotSetException {
		if (!this.properties.containsKey(key)) {
			throw new PropertyNotSetException(key);
		}

		return this.properties.get(key);
	}

	@Override
	public final void set(String key, Object val) throws InvalidPropertyException {
		if (key == null || key.isEmpty()) {
			throw new InvalidPropertyException("Property name cannot be empty");
		}

		this.properties.put(key, val);
	}

}
