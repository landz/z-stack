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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestClock
{
    private static Map<String, Result> results = new HashMap<String,Result>();

    private static IResultsConsumer resultsConsumer = new IResultsConsumer()
    {
        public void accept(Result result)
        {
            results.put(result.description.getMethodName(), result);
        }
    };

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule(resultsConsumer);


    @Test
    @BenchmarkOptions(clock = Clock.USER_TIME)
    public void testUserTime() throws Exception
    {
        Thread.sleep(100);
    }

    @Test
    @BenchmarkOptions(clock = Clock.CPU_TIME)
    public void testCpuTime() throws Exception
    {
        Thread.sleep(100);
    }


    @Test
    @BenchmarkOptions(clock = Clock.REAL_TIME)
    public void testRealTime() throws Exception
    {
        Thread.sleep(100);
    }


    @AfterClass
    public static void verify()
    {
        final double delta = 0.02;
        assertEquals(3, results.size());

        final double avg1 = results.get("testUserTime").roundAverage.avg;
        final double avg2 = results.get("testCpuTime").roundAverage.avg;
        final double avg3 = results.get("testRealTime").roundAverage.avg;

        assertTrue(avg1 > -delta && avg1 < delta);
        assertTrue(avg2 > -delta && avg2 < delta);
        assertTrue(avg3 > 0.1 - delta && avg3 < 0.1 + delta);
    }
}
