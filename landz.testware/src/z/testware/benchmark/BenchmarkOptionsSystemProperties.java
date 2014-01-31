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

import java.util.ArrayList;

/**
 * Global settings for benchmarks set through system properties. If
 * {@link #IGNORE_ANNOTATION_OPTIONS_PROPERTY} is specified, the system properties and
 * defaults will take precedence over the method- and class-level annotations.
 */
public final class BenchmarkOptionsSystemProperties
{
    /**
     * <code>{@value}</code>: the default number of warmup rounds.
     */
    public final static String WARMUP_ROUNDS_PROPERTY = "jub.rounds.warmup";

    /**
     * <code>{@value}</code>: the default number of benchmark rounds.
     */
    public final static String BENCHMARK_ROUNDS_PROPERTY = "jub.rounds.benchmark";

    /**
     * <code>{@value}</code>: the default number of threads.
     */
    public final static String CONCURRENCY_PROPERTY = "jub.concurrency";
    
    /**
     * <code>{@value}</code>: if <code>true</code>, the defaults (or property values) take precedence over
     * {@link z.testware.benchmark.BenchmarkOptions} annotations.
     */
    public final static String IGNORE_ANNOTATION_OPTIONS_PROPERTY = "jub.ignore.annotations";

    /**
     * <code>{@value}</code>: if <code>true</code>, do not call {@link System#gc()}
     * between rounds. Speeds up tests a lot, but renders GC statistics useless.
     */
    public final static String IGNORE_CALLGC_PROPERTY = "jub.ignore.callgc";

    /**
     * <code>{@value}</code>: if set, an {@link z.testware.benchmark.XMLConsumer} is added to the consumers list.
     */
    public final static String XML_FILE_PROPERTY = "jub.xml.file";

    /**
     * <code>{@value}</code>: custom key to attach to the run.
     */
    public final static String CUSTOMKEY_PROPERTY = "jub.customkey";

    /**
     * <code>{@value}</code>: specifies the consumers to instantiate and add to the 
     * benchmark results feed. This property takes a comma-separated list of values 
     * from {@link z.testware.benchmark.ConsumerName}.
     */
    public final static String CONSUMERS_PROPERTY = "jub.consumers";

    /**
     * The default consumer of benchmark results.
     */
    static IResultsConsumer [] consumers;

    /**
     * @return Return the default {@link z.testware.benchmark.IResultsConsumer}.
     */
    public synchronized static IResultsConsumer [] getDefaultConsumers()
    {
        if (consumers == null)
        {
            consumers = initializeDefault();
        }

        return consumers;
    }

    /**
     * Initialize the default consumers.
     */
    private static IResultsConsumer [] initializeDefault()
    {
        if (consumers != null)
        {
            throw new RuntimeException("Consumers list already initialized.");
        }

        /* Get the requested consumer list. */
        String [] consumers = 
            System.getProperty(CONSUMERS_PROPERTY, ConsumerName.CONSOLE.toString()).split("\\s*[,]\\s*");

        final ArrayList<IResultsConsumer> result = new ArrayList<IResultsConsumer>();

        for (String consumerName : consumers)
        {
            // For now only allow consumers from the consumer list.
            ConsumerName c = ConsumerName.valueOf(consumerName.toUpperCase());
            try
            {
                result.add(c.clazz.newInstance());
            }
            catch (Throwable e)
            {
                if (e instanceof Error)
                    throw (Error) e;
                if (e instanceof RuntimeException)
                    throw (RuntimeException) e;
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        return result.toArray(new IResultsConsumer [result.size()]);
    }
}
