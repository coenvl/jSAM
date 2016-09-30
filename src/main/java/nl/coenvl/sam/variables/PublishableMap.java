/**
 * File PublishableMap.java
 *
 * This file is part of the jSAM project.
 *
 * Copyright 2016 TNO
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
 */
package nl.coenvl.sam.variables;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.HashMap;

/**
 * PublishableMap
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 11 mrt. 2016
 */
public class PublishableMap<K, V> extends HashMap<K, V> {

    /**
     *
     */
    private static final long serialVersionUID = -995047423361340426L;

    public PublishableMap() {
        super();
    }

    public String serialize() {
        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(this);
            return new String(Base64.getEncoder().encode(baos.toByteArray()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static PublishableMap<?, ?> deserialize(String s) {
        try (
                ObjectInputStream ois = new ObjectInputStream(
                        new ByteArrayInputStream(Base64.getDecoder().decode(s.getBytes())))) {
            return (PublishableMap<?, ?>) ois.readObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
