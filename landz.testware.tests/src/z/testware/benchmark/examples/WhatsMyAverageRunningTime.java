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

import org.junit.Test;
import z.testware.benchmark.AbstractBenchmark;
import z.testware.benchmark.BenchmarkOptions;

import java.util.Random;

public class WhatsMyAverageRunningTime extends AbstractBenchmark
{
    private final static Random rnd = new Random();

    @BenchmarkOptions(benchmarkRounds = 50, warmupRounds = 0)
    @Test
    public void question() throws Exception
    {
        Thread.sleep(rnd.nextInt(100));
    }
}