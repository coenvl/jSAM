/**
 * File HashMessage.java
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

import java.util.HashMap;
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
public final class HashMessage extends HashMap<String, String> implements Message {

	/**
	 *
	 */
	private static final long serialVersionUID = 6306118222585166399L;

	private final String type;

	private final UUID source;

	public HashMessage(UUID source, String type) {
		super();
		this.source = source;
		this.type = type;
	}

	@Override
	public void put(String key, Object value) {
		super.put(key, value.toString());
	}

	// @Override
	// public UUID getUUID(String key) {
	// return UUID.fromString(super.get(key));
	// }

	@Override
	public UUID getSource() {
		return this.source;
	}

	@Override
	public Integer getInteger(String key) {
		return Integer.valueOf(super.get(key));
	}

	@Override
	public Double getDouble(String key) {
		return Double.valueOf(super.get(key));
	}

	@Override
	public PublishableMap<?, ?> getMap(String key) {
		return PublishableMap.fromString(super.get(key));
	}

	@Override
	public HashMessage clone() {
		HashMessage clone = new HashMessage(this.source, this.type);
		clone.putAll(this);
		return clone;
	}

	@Override
	public String getType() {
		return this.type;
	}

	@Override
	public long messageSize() {
		return 0L;
	}

	@Override
	public String toString() {
		return "Message of Type " + this.type + "(" + super.toString() + ")";
	}

}
