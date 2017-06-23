/**
 * File TestSerializer.java
 *
 * Copyright 2017 TNO
 */
package nl.coenvl.sam.variables;

import java.io.NotSerializableException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import nl.coenvl.sam.messages.HashMessage;
import nl.coenvl.sam.messages.Message;

/**
 * TestSerializer
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 2 jun. 2017
 */
public class TestSerializer {

    private final IntegerVariable var = new IntegerVariable(0, 10);

    @Test
    public void test() {
        this.var.setValue(4);
        final Message m = new HashMessage(UUID.randomUUID(), "stuff");
        try {
            m.put("KEY", this.var);
            Assert.fail("It is not allowed to serialize variables");
        } catch (final Exception e) {
            Assert.assertEquals(NotSerializableException.class, e.getCause().getClass());
        }
    }

}
