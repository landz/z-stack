/**
 * Copyright 2013, Landz and its contributors. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package z.testware.benchmark;

import org.junit.AfterClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Creates a benchmark with two measured methods. One of the test methods has an
 * overridden number of warmup and benchmark rounds.
 */
public class TestDefaultRuleFired extends AbstractBenchmark
{
    private static int regularMethodInvocationCount;
    private static int customAnnotationMethodInvocationCount;

    @Test
    public void testNormalMethod()
    {
        regularMethodInvocationCount++;
    }

    @Test
    @BenchmarkOptions(warmupRounds = 3, benchmarkRounds = 5)
    public void testCustomAnnotation()
    {
        customAnnotationMethodInvocationCount++;
    }

    @AfterClass
    public static void verifyCounts()
    {
        assertEquals("Custom method invocation count.", 8,
            customAnnotationMethodInvocationCount);

        assertEquals("Regular method invocation count.",
            BenchmarkStatement.DEFAULT_BENCHMARK_ROUNDS
                + BenchmarkStatement.DEFAULT_WARMUP_ROUNDS, regularMethodInvocationCount);
    }
}
