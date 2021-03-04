/**
 * File AssignmentMapTest.java
 *
 * Copyright 2016 TNO
 */
package nl.coenvl.sam.variables;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * AssignmentMapTest
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 30 sep. 2016
 */
public class AssignmentMapTest {

    private final Variable<Integer> testVariable;
    private AssignmentMap<Integer> map;

    public AssignmentMapTest() {
        this.testVariable = new IntegerVariable(0, 2);
    }

    @BeforeEach
    public void init() {
        this.map = new AssignmentMap<>();
    }

    @Test
    public void testAdd() {
        // Adding it should return old value (null)
        Integer testValue = 24;
        Assertions.assertNull(this.map.setAssignment(this.testVariable, testValue));

        // Setting another value should return the old value
        Integer newValue = 0;
        Assertions.assertEquals(testValue, this.map.setAssignment(this.testVariable, newValue));

        // Setting to null also returns the old value
        Assertions.assertEquals(newValue, this.map.setAssignment(this.testVariable, null));

        Assertions.assertNull(this.map.setAssignment(this.testVariable, newValue));
    }

    @Test
    public void testRemove() {
        // First it should be empty
        Assertions.assertNull(this.map.removeAssignment(this.testVariable));

        // Adding it should return old value (null)
        Integer testValue = 24;
        Assertions.assertNull(this.map.setAssignment(this.testVariable, testValue));

        // Removing it now should return the previously set value
        Assertions.assertEquals(testValue, this.map.removeAssignment(this.testVariable));

        // Removing it again should return null again
        Assertions.assertNull(this.map.removeAssignment(this.testVariable));
    }

    @Test
    public void testContains() {
        // First it does not have the value
        Assertions.assertFalse(this.map.containsAssignment(this.testVariable));

        // Setting it to null returns null if it didn't contain anything
        Assertions.assertEquals(null, this.map.setAssignment(this.testVariable, null));

        // Now it does contain something
        Assertions.assertTrue(this.map.containsAssignment(this.testVariable));

        // Setting it to a value returns the old value (null)
        Integer testValue = 1;
        Assertions.assertNull(this.map.setAssignment(this.testVariable, testValue));

        // Still contains something
        Assertions.assertTrue(this.map.containsAssignment(this.testVariable));

        // Remove it returns the set value
        Assertions.assertEquals(testValue, this.map.removeAssignment(this.testVariable));

        // Now it it does not have the value
        Assertions.assertFalse(this.map.containsAssignment(this.testVariable));
    }

}
