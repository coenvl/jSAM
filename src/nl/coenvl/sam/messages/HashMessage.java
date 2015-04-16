/**
 * File HashMessage.java
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
package nl.coenvl.sam.messages;

import java.util.HashMap;

/**
 * Message
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 4 feb. 2014
 * 
 */
public final class HashMessage implements Message {

	private HashMap<String, Object> content;

	private String type;

	public HashMessage(String type) {
		this.type = type;
		content = new HashMap<String, Object>();
	}

	@Override
	public Object addContent(String key, Object value) {
		return content.put(key, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public HashMessage clone() {
		HashMessage clone = new HashMessage(this.type);
		clone.content = (HashMap<String, Object>) this.content.clone();
		return clone;
	}

	@Override
	public Object getContent(String key) {
		return content.get(key);
	}

	@Override
	public String getType() {
		return this.type;
	}

	@Override
	public boolean hasContent(String key) {
		return content.containsKey(key);
	}

	@Override
	public long size() {
		return 0L;
	}

	@Override
	public String toString() {
		return "Message of Type " + this.type + "(" + this.content + ")";
	}
}
