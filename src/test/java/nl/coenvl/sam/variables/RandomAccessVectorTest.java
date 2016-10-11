/**
 * File RandomAccessVectorTest.java
 *
 * Copyright 2016 TNO
 */
package nl.coenvl.sam.variables;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * RandomAccessVectorTest
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 3 okt. 2016
 */
public class RandomAccessVectorTest {

    private final RandomAccessVector<Integer> candidates;

    /**
     *
     */
    public RandomAccessVectorTest() {
        this.candidates = new RandomAccessVector<>();
    }

    @Before
    public void init() {
        this.candidates.clear();
    }

    @Test
    public void testEmptyGet() {
        try {
            this.candidates.randomElement();
            Assert.fail("An exception was expected");
        } catch (Exception e) {
            Assert.assertEquals(NoSuchElementException.class, e.getClass());
        }
    }

    @Test
    public void testGetOneCandidate() {
        Integer candidate = 4;
        this.candidates.add(candidate);
        for (int i = 1; i < 100; i++) {
            Assert.assertEquals(candidate, this.candidates.randomElement());
        }
    }

    @Test
    public void getRandomCandidate() {
        int MAX_DRAWS = 1000;
        for (int i = 0; i < 10; i++) {
            this.candidates.add(i);
        }

        Set<Integer> observed = new HashSet<>();

        int draw = 0;
        for (int i = 0; (i < MAX_DRAWS) && (observed.size() < 10); draw++) {
            observed.add(this.candidates.randomElement());
        }

        System.out.println("Observed " + observed.size() + " integers in " + draw + " draws.");
        Assert.assertTrue(draw >= 10);
        Assert.assertEquals(10, observed.size());
        Assert.assertNotEquals(MAX_DRAWS, draw);
    }
}
