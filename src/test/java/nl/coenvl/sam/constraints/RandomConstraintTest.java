/**
 * File TestCostMatrixConstraintTest.java
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
package nl.coenvl.sam.constraints;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nl.coenvl.sam.exceptions.VariableNotInvolvedException;
import nl.coenvl.sam.exceptions.VariableNotSetException;
import nl.coenvl.sam.variables.AssignmentMap;
import nl.coenvl.sam.variables.IntegerVariable;

/**
 * TestCostMatrixConstraintTest
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 4 mrt. 2016
 */
public class RandomConstraintTest {

	private IntegerVariable var1;
	private IntegerVariable var2;
	private RandomConstraint<Integer> arc;

	public RandomConstraintTest() {
		this.var1 = new IntegerVariable(3, 10);
		this.var2 = new IntegerVariable(5, 20);
		this.arc = new RandomConstraint<>(this.var1, this.var2);
	}

	@Test
	public void testMatrix() {
		double[][] randomMatrix = RandomConstraint.randomMatrix(this.var1.getRange(), this.var2.getRange());
		Assertions.assertEquals(this.var1.getRange(), randomMatrix.length);
		Assertions.assertEquals(this.var2.getRange(), randomMatrix[0].length);

		// This is kind of redundant
		Assertions.assertNotEquals(this.var1.getRange(), randomMatrix[0].length);
		Assertions.assertNotEquals(this.var2.getRange(), randomMatrix.length);
	}

	@Test
	public void testGetIds() {
		Set<UUID> ids = this.arc.getVariableIds();
		Assertions.assertEquals(2, ids.size());
		Assertions.assertTrue(ids.contains(this.var1.getID()));
		Assertions.assertTrue(ids.contains(this.var2.getID()));
	}

	@Test
	public void testGetCost() {
		// Test regular getCost
		for (Integer v1 : this.var1) {
			for (Integer v2 : this.var2) {
				this.var1.setValue(v1);
				this.var2.setValue(v2);

				Assertions.assertTrue(this.arc.getCost(this.var1) > 0);
				Assertions.assertTrue(this.arc.getCost(this.var1) < RandomConstraint.MAX_COST);
				Assertions.assertTrue(this.arc.getCost(this.var2) > 0);
				Assertions.assertTrue(this.arc.getCost(this.var2) < RandomConstraint.MAX_COST);
			}
		}

		this.var1.clear();
		try {
			this.arc.getCost(this.var1);
			Assertions.fail("Expected VariableNotSetException");
		} catch (Exception e) {
			Assertions.assertEquals(VariableNotSetException.class, e.getClass());
		}

		try {
			this.arc.getCost(this.var2);
			Assertions.fail("Expected VariableNotSetException");
		} catch (Exception e) {
			Assertions.assertEquals(VariableNotSetException.class, e.getClass());
		}
	}

	@Test
	public void testGetCostIf() {
		// Test getCostIf
		AssignmentMap<Integer> valueMap = new AssignmentMap<>();
		for (Integer v1 : this.var1) {
			for (Integer v2 : this.var2) {
				valueMap.setAssignment(this.var1, v1);
				valueMap.setAssignment(this.var2, v2);

				Assertions.assertTrue(this.arc.getCostIf(this.var1, valueMap) > 0);
				Assertions.assertTrue(this.arc.getCostIf(this.var1, valueMap) < RandomConstraint.MAX_COST);
				Assertions.assertTrue(this.arc.getCostIf(this.var2, valueMap) > 0);
				Assertions.assertTrue(this.arc.getCostIf(this.var2, valueMap) < RandomConstraint.MAX_COST);
			}
		}

		Assertions.assertNotNull(valueMap.removeAssignment(this.var1));
		try {
			this.arc.getCostIf(this.var1, valueMap);
			Assertions.fail("Expected VariableNotInvolvedException");
		} catch (Exception e) {
			Assertions.assertEquals(VariableNotInvolvedException.class, e.getClass());
		}

		this.arc.getCostIf(this.var2, valueMap);
		Assertions.assertTrue(this.arc.getCostIf(this.var2, valueMap) > 0);
		Assertions.assertTrue(this.arc.getCostIf(this.var2, valueMap) < RandomConstraint.MAX_COST);
	}

	@Test
	public void testGetExternalCost() {
		// Test regular getCost
		for (Integer v1 : this.var1) {
			for (Integer v2 : this.var2) {
				this.var1.setValue(v1);
				this.var2.setValue(v2);

				Assertions.assertTrue(this.arc.getExternalCost() > 0);
				Assertions.assertTrue(this.arc.getExternalCost() < (2 * RandomConstraint.MAX_COST));
				Assertions.assertTrue(this.arc.getExternalCost() > 0);
				Assertions.assertTrue(this.arc.getExternalCost() < (2 * RandomConstraint.MAX_COST));
			}
		}
	}

}
