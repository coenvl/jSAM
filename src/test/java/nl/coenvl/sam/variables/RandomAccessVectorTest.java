/**
 * File RandomAccessVectorTest.java
 *
 * Copyright 2016 TNO
 */
package nl.coenvl.sam.variables;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @BeforeEach
    public void init() {
        this.candidates.clear();
    }

    @Test
    public void testEmptyGet() {
        try {
            this.candidates.randomElement();
            Assertions.fail("An exception was expected");
        } catch (Exception e) {
        	Assertions.assertEquals(NoSuchElementException.class, e.getClass());
        }
    }

    @Test
    public void testGetOneCandidate() {
        Integer candidate = 4;
        this.candidates.add(candidate);
        for (int i = 1; i < 100; i++) {
        	Assertions.assertEquals(candidate, this.candidates.randomElement());
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
        Assertions.assertTrue(draw >= 10);
        Assertions.assertEquals(10, observed.size());
        Assertions.assertNotEquals(MAX_DRAWS, draw);
    }
}
