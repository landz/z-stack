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

package z.testware.benchmark.examples;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import z.testware.benchmark.BenchmarkOptions;
import z.testware.benchmark.BenchmarkRule;
import z.testware.benchmark.annotation.*;

// [[[start:lists-annot]]]
@BenchmarkOptions(callgc = false, benchmarkRounds = 20, warmupRounds = 3)
// [[[end:lists-annot]]]

// [[[start:lists-chart-methods]]]
@AxisRange(min = 0, max = 1)
@BenchmarkMethodChart(filePrefix = "benchmark-lists")
// [[[end:lists-chart-methods]]]

// [[[start:lists-chart-history]]]
@BenchmarkHistoryChart(labelWith = LabelType.CUSTOM_KEY, maxRuns = 20)
// [[[end:lists-chart-history]]]
public class Lists2
{
    private static Object singleton = new Object();
    private static int COUNT = 10000;
    private static int [] rnd;

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    /** Prepare random numbers for tests. */
    @BeforeClass
    public static void prepare()
    {
        rnd = new int [COUNT];

        final Random random = new Random();
        for (int i = 0; i < COUNT; i++)
        {
            rnd[i] = Math.abs(random.nextInt());
        }
    }

    @Test
    public void arrayList() throws Exception
    {
        runTest(new ArrayList<Object>());
    }

    @Test
    public void linkedList() throws Exception
    {
        runTest(new LinkedList<Object>());
    }

    @Test
    public void vector() throws Exception
    {
        runTest(new Vector<Object>());
    }

    private void runTest(List<Object> list)
    {
        assert list.isEmpty();
        
        // First, add a number of objects to the list.
        for (int i = 0; i < COUNT; i++)
            list.add(singleton);
        
        // Randomly delete objects from the list.
        for (int i = 0; i < rnd.length; i++)
            list.remove(rnd[i] % list.size());
    }
}