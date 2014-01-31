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

import java.io.*;

import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

/**
 * 
 */
public class TestConcurrencyOverride
{
    static StringWriter sw = new StringWriter();
    static IResultsConsumer stringConsumer = new WriterConsumer(sw);

    public static class Nested
    {
        @Rule
        public TestRule benchmarkRun = new BenchmarkRule(stringConsumer);

        @Test
        @BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 10)
        public void testMethodA() throws Exception
        {
            Thread.sleep(20);
        }
    }

    @Test
    public void testConcurrencyOverride() throws IOException
    {
        System.setProperty(BenchmarkOptionsSystemProperties.CONCURRENCY_PROPERTY, "2");

        sw.getBuffer().setLength(0);
        Result runClasses = JUnitCore.runClasses(Nested.class);
        Assert.assertEquals(1, runClasses.getRunCount());
        Assert.assertTrue(sw.getBuffer().toString().contains("threads: 2"));
    }

    @AfterClass
    public static void cleanup()
    {
        System.clearProperty(BenchmarkOptionsSystemProperties.CONCURRENCY_PROPERTY);
    }
}
