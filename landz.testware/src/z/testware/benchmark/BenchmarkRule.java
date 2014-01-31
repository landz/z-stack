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

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A benchmark rule (causes tests to be repeated and measured). Benchmark rule should be
 * placed in the test class as a field, with annotation. Example:
 * 
 * <pre>
 * {@link org.junit.Rule}
 * public {@link org.junit.rules.TestRule} runBenchmarks = new BenchmarkRule();
 * </pre>
 */
public final class BenchmarkRule implements TestRule
{
    private final IResultsConsumer [] consumers;

    /**
     * Creates a benchmark rule with the default sink for benchmark results (the default
     * sink is taken from global properties).
     */
    public BenchmarkRule()
    {
        this(BenchmarkOptionsSystemProperties.getDefaultConsumers());
    }

    /**
     * Creates a benchmark rule with a given sink for benchmark results.
     */
    public BenchmarkRule(IResultsConsumer... consumers)
    {
        if (consumers == null || consumers.length == 0)
            throw new IllegalArgumentException("There needs to be at least one consumer.");

        this.consumers = consumers;
    }

    /**
     * Apply benchmarking to the given test description.
     */
    @Override
    public Statement apply(Statement base, Description description) {
        return new BenchmarkStatement(base, description, consumers);
    }
}