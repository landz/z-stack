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

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class TestClassForNameVSEnumVauleOf
{
    static IResultsConsumer stringConsumer = new WriterConsumer();
    static Map<String, Class> map = new HashMap();
     private static final int LOOP_COUNT = 100000000;
    enum ConsumerType {
        CONSOLE(WriterConsumer.class);

        Class c;
        ConsumerType(Class c) {
            this.c = c;
        }
    }

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule(stringConsumer);

//    @Test
    @BenchmarkOptions(benchmarkRounds = 2, warmupRounds = 2)
    public void testClassForName() throws Exception
    {
        for (int i=0;i<1_000_000;i++) {
            Class.forName("z.testware.benchmark.WriterConsumer");
        }
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 2, warmupRounds = 2)
    public void testEnumVauleOf() throws Exception
    {
        for (int i=0;i<LOOP_COUNT;i++) {
            ConsumerType.valueOf("CONSOLE");
        }
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 2, warmupRounds = 2)
    public void testDirectMap() throws Exception
    {
        for (int i=0;i<LOOP_COUNT;i++) {
            ConsumerName.valueOf("CONSOLE");
        }
    }

    @Before
    public void initialize() {
        map.put("z.testware.benchmark.WriterConsumer",WriterConsumer.class);
    }

    @After
    public void clean() {
        map.clear();
    }

}
