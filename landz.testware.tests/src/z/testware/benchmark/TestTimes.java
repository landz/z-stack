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

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test the average times.
 */
public class TestTimes
{
    private static ArrayList<Result> results = new ArrayList<Result>();

    private static IResultsConsumer resultsConsumer = new IResultsConsumer() {
        public void accept(Result result)
        {
            results.add(result);
        }
    };
    
    @Rule
    public TestRule benchmarkRun = new BenchmarkRule(resultsConsumer);

    @Test
    public void test100msDelay() throws Exception
    {
        Thread.sleep(100);
    }

    @AfterClass
    public static void verify()
    {
        assertEquals(1, results.size());

        final double avg = results.get(0).roundAverage.avg;
        final double delta = 0.02;
        assertTrue(avg > 0.1 - delta && avg < 0.1 + delta);
    }
}
