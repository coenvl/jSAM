/**
 * File PropertyOwner.java
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

import org.anon.cocoa.exceptions.InvalidPropertyException;
import org.anon.cocoa.exceptions.PropertyNotSetException;

/**
 * PropertyOwner
 *
 * @author Anomymous
 * @version 0.1
 * @since 22 jan. 2016
 */
public interface PropertyOwner {

	public boolean has(String key);

	public Object get(String key) throws PropertyNotSetException;

	public void set(String key, Object val) throws InvalidPropertyException;

}
