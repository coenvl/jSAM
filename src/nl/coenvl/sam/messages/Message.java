/**
 * File Message.java
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

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Message
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 4 feb. 2014
 * 
 */
public interface Message {

	public String getType();

	public void addString(String key, String value);
	
	public String getString(String key);
	
	public void addNumber(String key, Number value);
	
	public Number getNumber(String key);
	
	public void addValueMap(String key, Map<UUID, Number> value);
	
	public Map<UUID, Number> getValueMap(String key);

	public boolean hasContent(String key);

	public Set<String> getKeys();
	
	public long size();
	
	public Message clone();

}
