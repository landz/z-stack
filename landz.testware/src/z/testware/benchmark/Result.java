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

import java.lang.reflect.Method;

import org.junit.runner.Description;

/**
 * A result of a single benchmark test.
 */
public final class Result
{
    final Description description;

    public final int benchmarkRounds, warmupRounds;
    public final long warmupTime, benchmarkTime;
    public final Average roundAverage;
    public final Average blockedAverage;
    public final Average gcAverage;
    public final GCSnapshot gcInfo;

    /**
     * Concurrency level (number of used threads).
     */
    int concurrency;

    /**
     * @param description Target object and method of the test.
     * @param benchmarkRounds Number of executed benchmark rounds.
     * @param warmupRounds Number of warmup rounds.
     * @param warmupTime Total warmup time, includes benchmarking and GC overhead.
     * @param benchmarkTime Total benchmark time, includes benchmarking and GC overhead.
     * @param roundAverage Average and standard deviation from benchmark rounds.
     * @param gcAverage Average and standard deviation from GC cleanups.
     * @param blockedAverage Average and standard deviation from thread blocks.
     * @param gcInfo Extra information about GC activity.
     * @param concurrency {@link BenchmarkOptions#concurrency()} setting (or global override).
     */
    public Result(
        Description description,
        int benchmarkRounds,
        int warmupRounds, 
        long warmupTime, 
        long benchmarkTime,
        Average roundAverage,
        Average blockedAverage,
        Average gcAverage,
        GCSnapshot gcInfo,
        int concurrency)
    {
        this.description = description;
        this.benchmarkRounds = benchmarkRounds;
        this.warmupRounds = warmupRounds;
        this.warmupTime = warmupTime;
        this.benchmarkTime = benchmarkTime;
        this.roundAverage = roundAverage;
        this.blockedAverage = blockedAverage;
        this.gcAverage = gcAverage;
        this.gcInfo = gcInfo;
        this.concurrency = concurrency;
    }

    /**
     * Returns the short version of the test's class.
     */
    public String getShortTestClassName()
    {
        return getTestClass().getSimpleName();
    }

    /**
     * Returns the long version of the test's class.
     */
    public String getTestClassName()
    {
        return this.description.getClassName();
    }

    /**
     * @return Return the test method's name.
     */
    public String getTestMethodName()
    {
        return this.description.getMethodName();
    }

    /**
     * Returns the class under test.
     */
    public Class<?> getTestClass()
    {
        return this.description.getTestClass();
    }

    /**
     * Returns the method under test. 
     */
    public Method getTestMethod() {
        try {
            return getTestClass().getMethod(getTestMethodName());
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    getTestMethodName()
                            + " is declared with required signature[public void no-arguments]",
                    e);
        }
    }

    public int getThreadCount()
    {
        return concurrency;
    }
}
