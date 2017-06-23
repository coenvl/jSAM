/**
 * File StringSerializer.java
 *
 * Copyright 2017 TNO
 */
package nl.coenvl.sam.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

/**
 * StringSerializer
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 2 jun. 2017
 */
public class StringSerializer {

    public String serialize(final Object obj) {
        try (
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            final byte[] barr = Base64.getEncoder().encode(baos.toByteArray());
            return new String(barr);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Object deserialize(final String str) {
        final byte[] barr = Base64.getDecoder().decode(str.getBytes());
        try (final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(barr))) {
            return ois.readObject();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
