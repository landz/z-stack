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

import org.junit.Rule;
import org.junit.rules.TestRule;

/**
 * A superclass for tests that should be executed as benchmarks (several rounds, GC and
 * time accounting). Provides a JUnit rule in {@link #benchmarkRun} that runs the tests
 * repeatedly, logging the intermediate results (memory usage, times).
 * <p>
 * Subclasses may add {@link z.testware.benchmark.BenchmarkOptions} at the class-level or to individual methods
 * to override the defaults.
 * </p>
 */
public abstract class AbstractBenchmark
{
    /**
     * Enables the benchmark rule.
     */
    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();
}
