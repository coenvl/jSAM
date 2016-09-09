/**
 * File TestCostMatrixConstraintTest.java
 *
 * This file is part of the jCoCoA project.
 *
 * Copyright 2016 Anonymous
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
package org.anon.cocoa.tests;

import org.junit.Assert;
import org.junit.Test;

import org.anon.cocoa.constraints.CostMatrixConstraint;
import org.anon.cocoa.constraints.InequalityConstraint;
import org.anon.cocoa.constraints.PreferentialEqualityConstraint;
import org.anon.cocoa.constraints.RandomConstraint;
import org.anon.cocoa.constraints.SemiRandomConstraint;
import org.anon.cocoa.constraints.SymmetricRandomConstraint;
import org.anon.cocoa.exceptions.CostMatrixRangeException;
import org.anon.cocoa.exceptions.InvalidDomainException;
import org.anon.cocoa.exceptions.VariableNotSetException;
import org.anon.cocoa.variables.AssignmentMap;
import org.anon.cocoa.variables.DiscreteVariable;
import org.anon.cocoa.variables.IntegerVariable;

/**
 * TestCostMatrixConstraintTest
 *
 * @author Anomymous
 * @version 0.1
 * @since 4 mrt. 2016
 */
@SuppressWarnings("static-method")
public class TestCostMatrixConstraint {

    @Test
    public void testNonValueBehavior() throws InvalidDomainException {
        IntegerVariable var1 = new IntegerVariable(0, 2);
        IntegerVariable var2 = new IntegerVariable(0, 2);

        double[][] costMat1 = new double[3][3];

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                costMat1[x][y] = Math.random();
            }
        }

        CostMatrixConstraint<Integer> scmc = new CostMatrixConstraint<>(var1, var2, costMat1);

        // Test no value provided behavior
        AssignmentMap<Integer> valueMap = new AssignmentMap<>();
        Assert.assertEquals(0, scmc.getCostIf(var1, null), 0);
        Assert.assertEquals(0, scmc.getCostIf(var1, valueMap), 0);
        valueMap.setAssignment(var1, 1);
        Assert.assertEquals((costMat1[1][0] + costMat1[1][1] + costMat1[1][2]) / 3, scmc.getCostIf(var1, valueMap), 0);
        valueMap.setAssignment(var2, 1);
        Assert.assertEquals(costMat1[1][1], scmc.getCostIf(var1, valueMap), 0);

        valueMap.setAssignment(var2, 3);
        try {
            scmc.getCostIf(var1, valueMap);
            Assert.fail("Expected CostMatrixRangeException");
        } catch (Exception e) {
            Assert.assertEquals(CostMatrixRangeException.class, e.getClass());
        }

        try {
            scmc.getCost(var1);
            Assert.fail("Expected VariableNotSetException");
        } catch (Exception e) {
            Assert.assertEquals(VariableNotSetException.class, e.getClass());
        }
    }

    @Test
    public void testRandomCostMatrices() throws InvalidDomainException {
        IntegerVariable var1 = new IntegerVariable(3, 10);
        IntegerVariable var2 = new IntegerVariable(5, 20);

        CostMatrixConstraint<Integer> src = new SymmetricRandomConstraint<>(var1, var2);
        CostMatrixConstraint<Integer> arc = new RandomConstraint<>(var1, var2);
        CostMatrixConstraint<Integer> hrc = new SemiRandomConstraint<>(var1, var2);

        int eq = 0;

        // Test regular getCost
        for (Integer v1 : var1) {
            for (Integer v2 : var2) {
                var1.setValue(v1);
                var2.setValue(v2);

                Assert.assertEquals(src.getCost(var1), src.getCost(var2), 0);
                Assert.assertNotEquals(arc.getCost(var1), arc.getCost(var2));
                eq += (hrc.getCost(var1) == hrc.getCost(var2) ? 1 : 0);
            }
        }

        Assert.assertNotEquals(var1.getRange() * var2.getRange(), eq);
        eq = 0;

        // Test getCostIf
        AssignmentMap<Integer> valueMap = new AssignmentMap<>();
        for (Integer v1 : var1) {
            for (Integer v2 : var2) {
                valueMap.setAssignment(var1, v1);
                valueMap.setAssignment(var2, v2);

                Assert.assertEquals(src.getCostIf(var1, valueMap), src.getCostIf(var2, valueMap), 0);
                Assert.assertNotEquals(arc.getCostIf(var1, valueMap), arc.getCostIf(var2, valueMap));
                eq += (hrc.getCostIf(var1, valueMap) == hrc.getCostIf(var2, valueMap) ? 1 : 0);
            }
        }

        Assert.assertNotEquals(var1.getRange() * var2.getRange(), eq);
    }

    @Test
    public void testCostMatrices() throws InvalidDomainException {
        IntegerVariable var1 = new IntegerVariable(0, 2);
        IntegerVariable var2 = new IntegerVariable(0, 2);

        double[][] costMat1 = new double[3][3];
        double[][] costMat2 = new double[3][3];

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                costMat1[x][y] = Math.random();
                costMat2[x][y] = Math.random();
            }
        }

        CostMatrixConstraint<Integer> scmc = new CostMatrixConstraint<>(var1, var2, costMat1);
        CostMatrixConstraint<Integer> acmc = new CostMatrixConstraint<>(var1, var2, costMat1, costMat2);

        // Test regular getCost
        for (Integer v1 : var1) {
            for (Integer v2 : var2) {
                var1.setValue(v1);
                var2.setValue(v2);

                Assert.assertEquals(scmc.getCost(var1), scmc.getCost(var2), 0);
                Assert.assertNotEquals(acmc.getCost(var1), acmc.getCost(var2));

                Assert.assertEquals(costMat1[v1][v2], scmc.getCost(var1), 0);
                Assert.assertEquals(costMat1[v1][v2], scmc.getCost(var2), 0);

                Assert.assertEquals(costMat1[v1][v2], acmc.getCost(var1), 0);
                Assert.assertEquals(costMat2[v2][v1], acmc.getCost(var2), 0);
            }
        }

        // Test getCostIf
        AssignmentMap<Integer> valueMap = new AssignmentMap<>();
        for (Integer v1 : var1) {
            for (Integer v2 : var2) {
                valueMap.setAssignment(var1, v1);
                valueMap.setAssignment(var2, v2);

                Assert.assertEquals(scmc.getCostIf(var1, valueMap), scmc.getCostIf(var2, valueMap), 0);
                Assert.assertNotEquals(acmc.getCostIf(var1, valueMap), acmc.getCostIf(var2, valueMap));

                Assert.assertEquals(costMat1[v1][v2], scmc.getCostIf(var1, valueMap), 0);
                Assert.assertEquals(costMat1[v1][v2], scmc.getCostIf(var2, valueMap), 0);

                Assert.assertEquals(costMat1[v1][v2], acmc.getCostIf(var1, valueMap), 0);
                Assert.assertEquals(costMat2[v2][v1], acmc.getCostIf(var2, valueMap), 0);
            }
        }
    }

    @Test
    public void testInequalityConstraints() throws InvalidDomainException {
        DiscreteVariable<Integer> var1 = new IntegerVariable(0, 5);
        DiscreteVariable<Integer> var2 = new IntegerVariable(6, 10);

        double cost = Math.random();
        InequalityConstraint<DiscreteVariable<Integer>, Integer> ic = new InequalityConstraint<>(var1, var2, cost);

        for (Integer v1 : var1) {
            for (Integer v2 : var2) {
                var1.setValue(v1);
                var2.setValue(v2);

                // Non-overlapping domains, so never should be equal...
                Assert.assertEquals(0, ic.getCost(var1), 0);
                Assert.assertEquals(0, ic.getCost(var2), 0);
            }
        }

        // Test getCostIf
        AssignmentMap<Integer> valueMap = new AssignmentMap<>();
        for (Integer v1 : var1) {
            valueMap.setAssignment(var1, v1);
            valueMap.setAssignment(var2, v1); // These are invalid values, but is allowed by inequalityConstraint

            Assert.assertEquals(cost, ic.getCostIf(var1, valueMap), 0);
            Assert.assertEquals(cost, ic.getCostIf(var2, valueMap), 0);
        }
    }

    @Test
    public void testPreferentialEqualityConstraint() throws InvalidDomainException {
        DiscreteVariable<Integer> var1 = new IntegerVariable(0, 5);
        DiscreteVariable<Integer> var2 = new IntegerVariable(0, 5);

        double ineqCost = 1000;
        double[] pref1 = new double[var1.getRange()];
        double[] pref2 = new double[var2.getRange()];

        for (int i = 0; i < pref1.length; i++) {
            pref1[i] = Math.random();
            pref2[i] = Math.random();
        }

        PreferentialEqualityConstraint<Integer> ic = new PreferentialEqualityConstraint<>(var1, var2, pref1, pref2,
                ineqCost);

        for (Integer v1 : var1) {
            for (Integer v2 : var2) {
                var1.setValue(v1);
                var2.setValue(v2);

                if (v1 == v2) {
                    Assert.assertEquals(pref1[v1], ic.getCost(var1), 0.0);
                    Assert.assertEquals(pref2[v1], ic.getCost(var2), 0.0);
                    Assert.assertEquals(pref1[v1] + pref2[v2], ic.getExternalCost(), 0.0);
                } else {
                    Assert.assertEquals(ineqCost, ic.getCost(var1), 0.0);
                    Assert.assertEquals(ineqCost, ic.getCost(var2), 0.0);
                    Assert.assertEquals(2 * ineqCost, ic.getExternalCost(), 0.0);
                }
            }
        }

        // Test getCostIf
        AssignmentMap<Integer> valueMap = new AssignmentMap<>();
        for (Integer v1 : var1) {
            valueMap.setAssignment(var1, v1);
            for (Integer v2 : var2) {
                valueMap.setAssignment(var2, v2); // These are invalid values, but is allowed by inequalityConstraint

                if (v1 == v2) {
                    Assert.assertEquals(pref1[v1], ic.getCostIf(var1, valueMap), 0.0);
                    Assert.assertEquals(pref2[v1], ic.getCostIf(var2, valueMap), 0.0);
                } else {
                    Assert.assertEquals(ineqCost, ic.getCostIf(var1, valueMap), 0.0);
                    Assert.assertEquals(ineqCost, ic.getCostIf(var2, valueMap), 0.0);
                }
            }
        }
    }

}
