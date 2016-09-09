/**
 * File Message.java
 *
 * This file is part of the jCoCoA project 2014.
 *
 * Copyright 2014 Anomymous
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
package org.anon.cocoa.messages;

import java.util.Map;
import java.util.UUID;

import org.anon.cocoa.variables.PublishableMap;

/**
 * Message
 *
 * @author Anomymous
 * @version 0.1
 * @since 4 feb. 2014
 *
 */
public interface Message extends Map<String, String> {

	public String getType();

	public UUID getSource();

	public void put(String key, Object value);

	// public UUID getUUID(String key);

	public Integer getInteger(String key);

	public Double getDouble(String key);

	public PublishableMap<?, ?> getMap(String key);

	public Message clone();

	public long messageSize();

}
