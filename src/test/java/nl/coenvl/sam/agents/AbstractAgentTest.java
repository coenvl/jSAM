/**
 * File AbstractAgentTest.java
 *
 * Copyright 2016 TNO
 */
package nl.coenvl.sam.agents;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.coenvl.sam.constraints.Constraint;
import nl.coenvl.sam.constraints.InequalityConstraint;
import nl.coenvl.sam.constraints.LessThanConstraint;
import nl.coenvl.sam.constraints.RandomConstraint;
import nl.coenvl.sam.exceptions.VariableNotInvolvedException;
import nl.coenvl.sam.messages.Message;
import nl.coenvl.sam.variables.AssignmentMap;
import nl.coenvl.sam.variables.DiscreteVariable;
import nl.coenvl.sam.variables.FixedPrecisionVariable;
import nl.coenvl.sam.variables.Variable;

/**
 * AbstractAgentTest
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 10 okt. 2016
 */
public class AbstractAgentTest {

    private static final String TEST_NAME = "testAgent";

    private final DiscreteVariable<Double> ownedVariable;
    private final DiscreteVariable<Double> otherVariable;
    private final Constraint<DiscreteVariable<Double>, Double> constraint;

    private PublicConstraintsAgent<DiscreteVariable<Double>, Double> agent;

    public AbstractAgentTest() {
        this.ownedVariable = new FixedPrecisionVariable(0, 10, 1.0 / 3);
        this.otherVariable = new FixedPrecisionVariable(0, 20, 2.0 / 3);
        this.constraint = new RandomConstraint<>(this.ownedVariable, this.otherVariable);
    }

    @BeforeEach
    public void init() {
        this.agent = new PublicConstraintsAgent<>(this.ownedVariable, AbstractAgentTest.TEST_NAME);
        this.ownedVariable.clear();
        this.otherVariable.clear();
    }

    @Test
    public void testGetName() {
        Assertions.assertEquals(AbstractAgentTest.TEST_NAME, this.agent.getName());

        // Not setting a name is allowed
        final Agent<DiscreteVariable<Double>, Double> a = new VariableAgent<>(this.ownedVariable, null);
        Assertions.assertNull(a.getName());

        Assertions.assertNotNull(a.toString());
    }

    @Test
    public void getVariable() {
        Assertions.assertEquals(this.ownedVariable, this.agent.getVariable());

        // Not setting a ownedVariable is not allowed
        try {
            @SuppressWarnings("unused")
            final VariableAgent<DiscreteVariable<Double>, Double> variableAgent = new VariableAgent<>(null,
                    AbstractAgentTest.TEST_NAME);
            Assertions.fail("Expected NullPointerException");
        } catch (final Exception e) {
            Assertions.assertEquals(NullPointerException.class, e.getClass());
        }
    }

    @Test
    public void reset() throws IllegalAccessException {
        this.agent.reset();

        this.agent.getVariable().setValue(3.33);
        Assertions.assertEquals(3.33, this.agent.getVariable().getValue(), 0.1);

        this.agent.addConstraint(this.constraint);
        Assertions.assertTrue(this.agent.getConstraints().contains(this.constraint));

        // Now reset it
        this.agent.reset();
        Assertions.assertFalse(this.agent.getVariable().isSet());
        this.agent.reset();

        // Constraints should not be influenced by the reset
        Assertions.assertTrue(this.agent.getConstraints().contains(this.constraint));
    }

    @Test
    public void addAndRemoveConstraint() throws IllegalAccessException {
        Assertions.assertTrue(this.agent.getConstraints().isEmpty());
        this.agent.addConstraint(this.constraint);
        Assertions.assertEquals(1, this.agent.getConstraints().size());
        Assertions.assertTrue(this.agent.getConstraints().contains(this.constraint));

        // Should do nothing
        this.agent.addConstraint(this.constraint);
        Assertions.assertEquals(1, this.agent.getConstraints().size());

        // Removing should set back to 0
        this.agent.removeConstraint(this.constraint);
        Assertions.assertTrue(this.agent.getConstraints().isEmpty());

        // Should do nothing
        this.agent.removeConstraint(this.constraint);
        Assertions.assertTrue(this.agent.getConstraints().isEmpty());

        // Cannot add the following constraint:
        final Constraint<DiscreteVariable<Double>, Double> incorrect = new RandomConstraint<>(this.otherVariable,
                this.otherVariable);
        try {
            this.agent.addConstraint(incorrect);
            Assertions.fail("Expected VariableNotSetException");
        } catch (final Exception e) {
            Assertions.assertEquals(VariableNotInvolvedException.class, e.getClass());
        }
        Assertions.assertFalse(this.agent.getConstraints().contains(incorrect));

        // The following should do nothing
        this.agent.removeConstraint(incorrect);
        Assertions.assertTrue(this.agent.getConstraints().isEmpty());
    }

    @Test
    public void getLocalCost() {
        // Without any constraints should be zero
        Assertions.assertEquals(0.0, this.agent.getLocalCost(), 0);

        // Add a less-than constraint
        final double lessThanCost = 2.5;
        final Constraint<DiscreteVariable<Double>, Double> lessThanContraint = new LessThanConstraint<>(
                this.ownedVariable,
                this.otherVariable,
                lessThanCost);
        this.agent.addConstraint(lessThanContraint);

        // Just set some values, it is not about the behavior of the constraint, but about the agent
        this.ownedVariable.setValue(8.0 / 3);
        this.otherVariable.setValue(4.0 / 3);
        Assertions.assertEquals(lessThanCost, this.agent.getLocalCost(), 0);

        // Add an inequality constraint
        final double inequalityCost = 1.337;
        final Constraint<DiscreteVariable<Double>, Double> inequalityContraint = new InequalityConstraint<>(
                this.ownedVariable,
                this.otherVariable,
                inequalityCost);
        this.agent.addConstraint(inequalityContraint);
        Assertions.assertEquals(lessThanCost, this.agent.getLocalCost(), 0);

        // Set the values to be equal, should make both constraints fail
        this.otherVariable.setValue(this.ownedVariable.getValue());
        Assertions.assertEquals(lessThanCost + inequalityCost, this.agent.getLocalCost(), 0);

        // Adding the random constraint should increase it, but not sure by how much
        this.agent.addConstraint(this.constraint);
        Assertions.assertTrue((lessThanCost + inequalityCost) < this.agent.getLocalCost());

        // Removing one constraint leaves the other
        this.agent.removeConstraint(this.constraint);
        this.agent.removeConstraint(lessThanContraint);
        Assertions.assertEquals(inequalityCost, this.agent.getLocalCost(), 0);
    }

    @Test
    public void getLocalCostIf() {
        // Create the assignmentMap
        final AssignmentMap<Double> map = new AssignmentMap<>();

        // Without any constraints should be zero
        Assertions.assertEquals(0.0, this.agent.getLocalCostIf(map), 0);

        // Add a less-than constraint
        final double lessThanCost = 2.5;
        final Constraint<DiscreteVariable<Double>, Double> lessThanContraint = new LessThanConstraint<>(
                this.ownedVariable,
                this.otherVariable,
                lessThanCost);
        this.agent.addConstraint(lessThanContraint);

        // Just set some values, it is not about the behavior of the constraint, but about the agent
        map.setAssignment(this.ownedVariable, 8.0 / 3);
        map.setAssignment(this.otherVariable, 4.0 / 3);
        Assertions.assertEquals(lessThanCost, this.agent.getLocalCostIf(map), 0);

        // Add an inequality constraint
        final double inequalityCost = 1.337;
        final Constraint<DiscreteVariable<Double>, Double> inequalityContraint = new InequalityConstraint<>(
                this.ownedVariable,
                this.otherVariable,
                inequalityCost);
        this.agent.addConstraint(inequalityContraint);
        Assertions.assertEquals(lessThanCost, this.agent.getLocalCostIf(map), 0);

        // Set the values to be equal, should make both constraints fail
        map.setAssignment(this.otherVariable, map.getAssignment(this.ownedVariable));
        Assertions.assertEquals(lessThanCost + inequalityCost, this.agent.getLocalCostIf(map), 0);

        // Adding the random constraint should increase it, but not sure by how much
        this.agent.addConstraint(this.constraint);
        Assertions.assertTrue((lessThanCost + inequalityCost) < this.agent.getLocalCostIf(map));

        // Removing one constraint leaves the other
        this.agent.removeConstraint(this.constraint);
        this.agent.removeConstraint(lessThanContraint);
        Assertions.assertEquals(inequalityCost, this.agent.getLocalCostIf(map), 0);
    }

    @Test
    public void getConstrainedVariableIds() {
        Assertions.assertTrue(this.agent.getConstrainedVariableIds().isEmpty());

        this.agent.addConstraint(this.constraint);
        Assertions.assertTrue(this.agent.getConstrainedVariableIds().contains(this.otherVariable.getID()));
        Assertions.assertFalse(this.agent.getConstrainedVariableIds().contains(this.ownedVariable.getID()));
        Assertions.assertEquals(1, this.agent.getConstrainedVariableIds().size());

        // Adding it again should not be of influence
        this.agent.addConstraint(this.constraint);
        Assertions.assertTrue(this.agent.getConstrainedVariableIds().contains(this.otherVariable.getID()));
        Assertions.assertFalse(this.agent.getConstrainedVariableIds().contains(this.ownedVariable.getID()));
        Assertions.assertEquals(1, this.agent.getConstrainedVariableIds().size());

        this.agent.removeConstraint(this.constraint);
        Assertions.assertTrue(this.agent.getConstrainedVariableIds().isEmpty());
    }

    @Test
    public void getConstraintForAgent() throws IllegalAccessException {
        Assertions.assertNull(this.agent.getConstraintForAgent(UUID.randomUUID()));
        Assertions.assertNull(this.agent.getConstraintForAgent(this.ownedVariable.getID()));

        this.agent.addConstraint(this.constraint);
        Assertions.assertTrue(this.agent.getConstraints().contains(this.constraint));
        Assertions.assertEquals(this.constraint, this.agent.getConstraintForAgent(this.ownedVariable.getID()));
        Assertions.assertNull(this.agent.getConstraintForAgent(UUID.randomUUID()));

        this.agent.removeConstraint(this.constraint);
        Assertions.assertNull(this.agent.getConstraintForAgent(this.ownedVariable.getID()));
    }

    /**
     * PublicConstraintsAgent
     *
     * @author leeuwencjv
     * @version 0.1
     * @since 11 okt. 2016
     */
    public class PublicConstraintsAgent<T extends Variable<V>, V> extends AbstractAgent<T, V> {

        /**
         * @param var
         * @param name
         */
        protected PublicConstraintsAgent(final T var, final String name) {
            super(var, name);
        }

        @Override
        public void push(final Message m) {
            // Do nothing
        }

        @Override
        public void init() {
            // Do nothing
        }

        // @Override
        // public boolean isFinished() {
        // return false;
        // }

        // This is a dirty hack, but for testing purposes I will allow it.
        @SuppressWarnings("unchecked")
        public Set<Constraint<T, V>> getConstraints() throws IllegalAccessException {
            try {
                final Field f = AbstractAgent.class.getDeclaredField("constraints");
                f.setAccessible(true);
                return (Set<Constraint<T, V>>) f.get(this);
            } catch (NoSuchFieldException | SecurityException e) {
                throw new IllegalAccessException(e.getMessage());
            }
        }

        /*
         * (non-Javadoc)
         *
         * @see nl.coenvl.sam.agents.Agent#isFinished()
         */
        @Override
        public boolean isFinished() {
            return true;
        }
    }

}
