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
 * http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Map;
import java.util.UUID;

/**
 * Message
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 4 feb. 2014
 *
 */
public final class HashMessage implements Message {

    private static final StringSerializer serializer = new StringSerializer();
    private final String type;
    private final UUID source;

    private final Map<String, String> valueMap;

    public HashMessage(final UUID source, final String type) {
        super();
        this.source = source;
        this.type = type;
        this.valueMap = new HashMap<>();
    }

    @Override
    public void put(final String key, final Object value) {
        if (value != null) {
            this.valueMap.put(key, HashMessage.serializer.serialize(value));
        }
    }

    @Override
    public UUID getSource() {
        return this.source;
    }

    @Override
    public Object get(final String key) {
        if (!this.valueMap.containsKey(key)) {
            return null;
        }
        return HashMessage.serializer.deserialize(this.valueMap.get(key));
    }

    @Override
    public HashMessage clone() {
        final HashMessage clone = new HashMessage(this.source, this.type);
        clone.valueMap.putAll(this.valueMap);
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

    /*
     * (non-Javadoc)
     *
     * @see nl.coenvl.sam.messages.Message#containsKey()
     */
    @Override
    public boolean containsKey(final String key) {
        return this.valueMap.containsKey(key);
    }

}
