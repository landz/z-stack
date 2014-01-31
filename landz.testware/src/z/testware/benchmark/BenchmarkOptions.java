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

import java.lang.annotation.*;

/**
 * Benchmark options applicable to methods annotated as tests.
 *
 * Note: the concurrency of BenchmarkOptions now is designed as a thread number pool
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(
{
    ElementType.TYPE, ElementType.METHOD
})
public @interface BenchmarkOptions
{
    /**
     * Sequential runs (no threads).
     */
    public final static int CONCURRENCY_SEQUENTIAL = -1;

    /**
     * Runs the benchmark with the number of threads reported
     * by {@link Runtime#availableProcessors()}.
     */
    public final static int CONCURRENCY_AVAILABLE_CORES = 0;

    /**
     * @return Call {@link System#gc()} before each test. This may slow down the tests in
     *         a significant way, so disabling it is sensible in most cases.
     */
    boolean callgc() default false;

    /**
     * Sets the number of warmup rounds for the test. If negative, the default is taken
     * from global options.
     */
    int warmupRounds() default -1;

    /**
     * Sets the number of benchmark rounds for the test. If negative, the default is taken
     * from global options.
     */
    int benchmarkRounds() default -1;

    /**
     * Specifies the number of threads that should execute the benchmarked method 
     * in parallel. This is a tricky thing to do and you should know what you're doing 
     * (because concurrent execution will affect GC and other measurements).
     * 
     * <p>Allowed values:
     * <ul>
     * <li>{@link #CONCURRENCY_SEQUENTIAL} - executed sequentially</li>
     * <li>{@link #CONCURRENCY_AVAILABLE_CORES} - executed concurrently with as many threads as reported by
     * {@link Runtime#availableProcessors()}.</li>
     * <li>any other integer &gt; 0 - executed concurrently with the given number of threads</li>
     * </ul>
     */
    int concurrency() default CONCURRENCY_SEQUENTIAL;

    /**
     * Sets the type of clock to be used for time measuring.
     * See {@link z.testware.benchmark.Clock} for available values.
     *
     */
    Clock clock() default Clock.REAL_TIME;
}