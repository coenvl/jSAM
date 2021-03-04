/**
 * File IntegrationTest.java
 *
 * Copyright 2016 TNO
 */
package nl.coenvl.sam;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import nl.coenvl.sam.agents.Agent;
import nl.coenvl.sam.agents.VariableAgent;
import nl.coenvl.sam.constraints.Constraint;
import nl.coenvl.sam.constraints.InequalityConstraint;
import nl.coenvl.sam.exceptions.InvalidPropertyException;
import nl.coenvl.sam.solvers.CoCoASolver;
import nl.coenvl.sam.solvers.CoCoSolver;
import nl.coenvl.sam.variables.IntegerVariable;
import nl.coenvl.sam.variables.Variable;

/**
 * IntegrationTest
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 3 okt. 2016
 */
public class IntegrationTest {

    @Test
    public void runTest() throws InterruptedException, InvalidPropertyException {
        final List<Variable> variables = new ArrayList<>();
        final List<Agent> agents = new ArrayList<>();

        for (int v = 0; v < 10; v++) {
            final IntegerVariable var = new IntegerVariable(1, 3);
            final VariableAgent agent = new VariableAgent<>(var, String.format("Agent %d", v));
            agent.setSolver(new CoCoASolver<>(agent));

            variables.add(var);
            agents.add(agent);
        }

        final int[][] edges = {{0, 4}, {1, 4}, {2, 7}, {3, 7}, {4, 6}, {4, 8}, {4, 9}, {5, 7}, {6, 7}};
        for (final int[] e : edges) {
            final Constraint c = new InequalityConstraint(variables.get(e[0]), variables.get(e[1]));
            agents.get(e[0]).addConstraint(c);
            agents.get(e[1]).addConstraint(c);
        }

        agents.get(0).set(CoCoSolver.ROOTNAME_PROPERTY, true);
        agents.get(0).init();
        
        while(!agents.stream().allMatch(Agent::isFinished)) {
        	Thread.sleep(10);
        }

        for (final Variable v : variables) {
            System.out.println(v.getValue());
        }
    }

}
