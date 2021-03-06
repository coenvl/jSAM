/**
 * File CompareCounter.java
 *
 * This file is part of the jSAM project.
 *
 * Copyright 2015 TNO
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

/**
 * AbstractCostFunction
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 3 jul. 2015
 *
 */
public abstract class CompareCounter {

    private static int nComparisons = 0;

    // public static Set<String> loggedComparisons = new LinkedHashSet<>();
    // public static int numComparisons = 0;

    // public static void log(final Variable<?> var1, final Object value1, final Variable<?> var2, final Object value2)
    // {
    // // CompareCounter.loggedComparisons.add(String.format("%s%s%d%d", var1.getName(), var2.getName(), value1,
    // // value2));
    // CompareCounter.loggedComparisons.add(MailMan.stateString(var1, value1, var2, value2));
    // CompareCounter.numComparisons++;
    // }

    public static void compare() {
        CompareCounter.nComparisons++;
    }

    public static int getComparisons() {
        return CompareCounter.nComparisons;
    }

    public static void reset() {
        CompareCounter.nComparisons = 0;
        // CompareCounter.loggedComparisons.clear();
        // CompareCounter.numComparisons = 0;
    }

}