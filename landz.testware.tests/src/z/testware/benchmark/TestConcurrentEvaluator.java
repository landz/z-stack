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

import org.junit.*;
import org.junit.rules.TestRule;

import java.util.concurrent.atomic.AtomicInteger;

public class TestConcurrentEvaluator
{
    @SuppressWarnings("serial")
    private static class NestedException extends Exception
    {
    }

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    private AtomicInteger roundNo = new AtomicInteger();

    @Test
    @BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 10)
    public void twentyMillisSequentially() throws Exception
    {
        Thread.sleep(20);
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 10, concurrency = 1)
    public void twentyMillisSingleThread() throws Exception
    {
        Thread.sleep(20);
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 10, concurrency = 0)
    public void twentyMillisDefaultConcurrency() throws Exception
    {
        Thread.sleep(20);
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 10, concurrency = 4)
    public void twentyMillisConcurrently() throws Exception
    {
        Thread.sleep(20);
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 10, concurrency = 4)
    public void statefullTwentyMillisConcurrently() throws Exception
    {
        Thread.sleep(20);
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 10, callgc = true)
    public void twentyMillisSequentiallyWithGC() throws Exception
    {
        Thread.sleep(20);
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 10, concurrency = 4)
    public void twentyMillisConcurrentlyInError() throws Exception
    {
        Thread.sleep(20);
    }

    /**
     * JUnit expects every run to thrown an exception if expected is set in
     * {@link Test#expected()}. We can't guarantee this with concurrent execution, so it's
     * impossible to make this test succeed.
     */
    @Ignore
    @Test
    @BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 10, concurrency = 4)
    public void twentyMillisConcurrentlyInFailure() throws Exception
    {
        if (roundNo.incrementAndGet() == 30)
        {
            throw new NestedException();
        }
        Thread.sleep(20);
    }

    @Test(expected = NestedException.class)
    @BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 10, concurrency = 4)
    public void twentyMillisConcurrentlyWithExpectedException() throws Exception
    {
        Thread.sleep(20);
        throw new NestedException();
    }

    /**
     * JUnit expects every run to thrown an exception if expected is set in
     * {@link Test#expected()}. We can't guarantee this with concurrent execution, so it's
     * impossible to make this test succeed.
     */
    @Ignore
    @Test
    @BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 10, concurrency = 1)
    public void twentyMillisSequentiallyInFailure() throws Exception
    {
        Thread.sleep(20);
        if (roundNo.incrementAndGet() == 30)
        {
            Assert.fail("Assertion failure at 30th iteration");
        }
    }

    @Test(expected = Exception.class)
    @BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 10)
    public void twentyMillisSequentiallyWithExpectedException() throws Exception
    {
        Thread.sleep(20);
        throw new Exception("Expected exception");
    }

    @Before
    public void reset()
    {
        roundNo.set(0);
    }
}