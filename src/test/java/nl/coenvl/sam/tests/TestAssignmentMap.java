/**
 * File TestAssignmentMap.java
 *
 * Copyright 2016 TNO
 */
package nl.coenvl.sam.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import nl.coenvl.sam.variables.AssignmentMap;
import nl.coenvl.sam.variables.IntegerVariable;
import nl.coenvl.sam.variables.Variable;

/**
 * TestAssignmentMap
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 30 sep. 2016
 */
public class TestAssignmentMap {

    private final Variable<Integer> testVariable;
    private AssignmentMap<Integer> map;

    public TestAssignmentMap() {
        this.testVariable = new IntegerVariable(0, 2);
    }

    @Before
    public void init() {
        this.map = new AssignmentMap<>();
    }

    @Test
    public void testAdd() {
        // Adding it should return old value (null)
        Integer testValue = 24;
        Assert.assertNull(this.map.setAssignment(this.testVariable, testValue));

        // Setting another value should return the old value
        Integer newValue = 0;
        Assert.assertEquals(testValue, this.map.setAssignment(this.testVariable, newValue));

        // Setting to null also returns the old value
        Assert.assertEquals(newValue, this.map.setAssignment(this.testVariable, null));

        Assert.assertNull(this.map.setAssignment(this.testVariable, newValue));
    }

    @Test
    public void testRemove() {
        // First it should be empty
        Assert.assertNull(this.map.removeAssignment(this.testVariable));

        // Adding it should return old value (null)
        Integer testValue = 24;
        Assert.assertNull(this.map.setAssignment(this.testVariable, testValue));

        // Removing it now should return the previously set value
        Assert.assertEquals(testValue, this.map.removeAssignment(this.testVariable));

        // Removing it again should return null again
        Assert.assertNull(this.map.removeAssignment(this.testVariable));
    }

    @Test
    public void testContains() {
        // First it does not have the value
        Assert.assertFalse(this.map.containsAssignment(this.testVariable));

        // Setting it to null returns null if it didn't contain anything
        Assert.assertEquals(null, this.map.setAssignment(this.testVariable, null));

        // Now it does contain something
        Assert.assertTrue(this.map.containsAssignment(this.testVariable));

        // Setting it to a value returns the old value (null)
        Integer testValue = 1;
        Assert.assertNull(this.map.setAssignment(this.testVariable, testValue));

        // Still contains something
        Assert.assertTrue(this.map.containsAssignment(this.testVariable));

        // Remove it returns the set value
        Assert.assertEquals(testValue, this.map.removeAssignment(this.testVariable));

        // Now it it does not have the value
        Assert.assertFalse(this.map.containsAssignment(this.testVariable));

    }

}
