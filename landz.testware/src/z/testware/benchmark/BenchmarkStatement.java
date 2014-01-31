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

import static z.testware.benchmark.BenchmarkOptionsSystemProperties.*;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Benchmark evaluator statement.
 */
final class BenchmarkStatement extends Statement
{
    /**
     * Factored out as a nested class as it needs to keep some data during test
     * evaluation.
     */
    private abstract class BaseEvaluator
    {
        final protected ArrayList<SingleResult> results;

        final protected int warmupRounds;
        final protected int benchmarkRounds;
        final protected int totalRounds;

        final protected Clock clock;
        final protected ThreadMXBean threadMXBean;
        final protected Map<Long,Long> threadBlockedTimes;

        protected long warmupTime;
        protected long benchmarkTime;

        protected BaseEvaluator(int warmupRounds, int benchmarkRounds, int totalRounds, Clock clock)
        {
            super();
            this.warmupRounds = warmupRounds;
            this.benchmarkRounds = benchmarkRounds;
            this.totalRounds = totalRounds;
            this.clock = clock;
            this.results = new ArrayList<SingleResult>(totalRounds);

            this.threadMXBean = ManagementFactory.getThreadMXBean();
            this.threadMXBean.setThreadContentionMonitoringEnabled(true);
            this.threadBlockedTimes = new HashMap<Long, Long>();
        }

        protected GCSnapshot gcSnapshot = null;

        protected abstract Result evaluate() throws Throwable;

        protected final SingleResult evaluateInternally(int round) throws InvocationTargetException
        {
            // We assume no reordering will take place here.
            final long startTime = clock.time();
            cleanupMemory();
            final long afterGC = clock.time();

            if (round == warmupRounds)
            {
                gcSnapshot = new GCSnapshot();
                benchmarkTime = clock.time();
                warmupTime = benchmarkTime - warmupTime;
            }

            try
            {
                base.evaluate();
                final long endTime = clock.time();

                final long threadId = Thread.currentThread().getId();
                final ThreadInfo threadInfo = threadMXBean.getThreadInfo(threadId);
                final long threadBlockedTime = threadInfo.getBlockedTime();
                final long roundBlockedTime = threadBlockedTimes.containsKey(threadId)
                        ? threadBlockedTime - threadBlockedTimes.get(threadId)
                        : threadBlockedTime;
                threadBlockedTimes.put(threadId,threadBlockedTime);

                return new SingleResult(startTime, afterGC, endTime, roundBlockedTime);
            } catch (Throwable t)
            {
                throw new InvocationTargetException(t);
            }
        }

        protected Result computeResult()
        {
            final Statistics stats = Statistics.from(
                results.subList(warmupRounds, totalRounds));

            return new Result(description, benchmarkRounds, warmupRounds, warmupTime,
                benchmarkTime, stats.evaluation, stats.blocked, stats.gc, gcSnapshot, 1);
        }
    }

    /**
     * Performs test method evaluation sequentially.
     */
    private final class SequentialEvaluator extends BaseEvaluator
    {
        SequentialEvaluator(int warmupRounds, int benchmarkRounds, int totalRounds, Clock clock)
        {
            super(warmupRounds, benchmarkRounds, totalRounds, clock);
        }

        @Override
        public Result evaluate() throws Throwable
        {
            warmupTime = clock.time();
            benchmarkTime = 0;
            for (int i = 0; i < totalRounds; i++)
            {
                results.add(evaluateInternally(i));
            }
            benchmarkTime = clock.time() - benchmarkTime;

            return computeResult();
        }
    }

    /**
     * Performs test method evaluation concurrently. The basic idea is to obtain a
     * {@link java.util.concurrent.ThreadPoolExecutor} instance (either new one on each evaluation as it is
     * implemented now or a shared one to avoid excessive thread allocation), wrap it into
     * a <tt>CompletionService&lt;SingleResult&gt;</tt>, pause its execution until the
     * associated task queue is filled with <tt>totalRounds</tt> number of
     * <tt>EvaluatorCallable&lt;SingleResult&gt;</tt>.
     */
    private final class ConcurrentEvaluator extends BaseEvaluator
    {
        private final class EvaluatorCallable implements Callable<SingleResult>
        {
            // Sequence number in order to keep track of warmup / benchmark phase
            private final int i;

            public EvaluatorCallable(int i)
            {
                this.i = i;
            }

            @Override
            public SingleResult call() throws Exception
            {
                latch.await();
                return evaluateInternally(i);
            }
        }

        private final int concurrency;
        private final CountDownLatch latch;


        ConcurrentEvaluator(int warmupRounds, int benchmarkRounds, int totalRounds,
                            int concurrency, Clock clock)
        {
            super(warmupRounds, benchmarkRounds, totalRounds, clock);

            this.concurrency = concurrency;
            this.latch = new CountDownLatch(1);
        }

        /**
         * Perform ThreadPoolExecution initialization. Returns new preconfigured
         * threadPoolExecutor for particular concurrency level and totalRounds to be
         * executed Candidate for further development to mitigate the problem of excessive
         * thread pool creation/destruction.
         *
         * @param concurrency
         * @param totalRounds
         */
        private final ExecutorService getExecutor(int concurrency, int totalRounds)
        {
            return new ThreadPoolExecutor(concurrency, concurrency, 10000,
                    TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(totalRounds));
        }

        /**
         * Perform proper ThreadPool cleanup.
         */
        private final void cleanupExecutor(ExecutorService executor)
        {
            @SuppressWarnings("unused")
            List<Runnable> pending = executor.shutdownNow();
            // Can pending.size() be > 0?
        }

        @Override
        public Result evaluate() throws Throwable
        {
            // Obtain ThreadPoolExecutor (new instance on each test method for now)
            ExecutorService executor = getExecutor(concurrency, totalRounds);
            CompletionService<SingleResult> completed = new ExecutorCompletionService<SingleResult>(
                executor);

            for (int i = 0; i < totalRounds; i++)
            {
                completed.submit(new EvaluatorCallable(i));
            }

            // Allow all the evaluators to proceed to the warmup phase.
            try {
                Thread.sleep(1000);//XX: sleep to allow all workers are waiting on the starting point
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            latch.countDown();

            warmupTime = clock.time();
            try
            {
                benchmarkTime = 0;

                for (int i = 0; i < totalRounds; i++)
                {
                    results.add(completed.take().get());
                }

                benchmarkTime = clock.time() - benchmarkTime;
                return computeResult();
            }
            catch (ExecutionException e)
            {
                // Unwrap the Throwable thrown by the tested method.
                e.printStackTrace();
                throw e.getCause().getCause();
            }
            finally
            {
                // Assure proper executor cleanup either on test failure or an successful completion
                cleanupExecutor(executor);
            }
        }

        @Override
        protected Result computeResult()
        {
            Result r = super.computeResult();
            r.concurrency = this.concurrency;
            return r;
        }
    }

    /**
     * How many warmup runs should we execute for each test method?
     */
    final static int DEFAULT_WARMUP_ROUNDS = 5;

    /**
     * How many actual benchmark runs should we execute for each test method?
     */
    final static int DEFAULT_BENCHMARK_ROUNDS = 10;

    /**
     * If <code>true</code>, the local overrides using {@link z.testware.benchmark.BenchmarkOptions} are
     * ignored and defaults (or globals passed via system properties) are used.
     */
    private boolean ignoreAnnotationOptions = Boolean
        .getBoolean(IGNORE_ANNOTATION_OPTIONS_PROPERTY);

    /**
     * Disable all forced garbage collector calls.
     */
    private boolean ignoreCallGC = Boolean.getBoolean(IGNORE_CALLGC_PROPERTY);

    private final Description description;
    private final BenchmarkOptions options;
    private final IResultsConsumer [] consumers;

    private final Statement base;


    /* */
    public BenchmarkStatement(Statement base, Description description,
            IResultsConsumer[] consumers) {
        this.base = base;
        this.description = description;
        this.consumers = consumers;

        this.options = resolveOptions(description);
    }

    /* Provide the default options from the annotation. */
    @BenchmarkOptions
    @SuppressWarnings("unused")
    private void defaultOptions()
    {
    }

    /* */
    private BenchmarkOptions resolveOptions(Description description) {
        // Method-level or Class-level
        BenchmarkOptions options = description.getAnnotation(BenchmarkOptions.class);
        if (options != null) return options;

        // Defaults.
        try
        {
            return getClass().getDeclaredMethod("defaultOptions").getAnnotation(
                BenchmarkOptions.class);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /* */
    @Override
    public void evaluate() throws Throwable
    {
        final int warmupRounds = getIntOption(options.warmupRounds(),
            WARMUP_ROUNDS_PROPERTY, DEFAULT_WARMUP_ROUNDS);

        final int benchmarkRounds = getIntOption(options.benchmarkRounds(),
            BENCHMARK_ROUNDS_PROPERTY, DEFAULT_BENCHMARK_ROUNDS);

        final int concurrency = getIntOption(options.concurrency(), CONCURRENCY_PROPERTY,
            BenchmarkOptions.CONCURRENCY_SEQUENTIAL);

        final int totalRounds = warmupRounds + benchmarkRounds;

        final BaseEvaluator evaluator;
        if (concurrency == BenchmarkOptions.CONCURRENCY_SEQUENTIAL)
        {
            evaluator = new SequentialEvaluator(warmupRounds, benchmarkRounds, totalRounds, options.clock());
        }
        else
        {
            /*
             * Just don't allow call GC during concurrent execution.
             */
            if (options.callgc())
                throw new IllegalArgumentException("Concurrent benchmark execution must be"
                    + " combined ignoregc=\"true\".");

            int threads = (concurrency == BenchmarkOptions.CONCURRENCY_AVAILABLE_CORES
                    ? Runtime.getRuntime().availableProcessors()
                    : concurrency);

            evaluator = new ConcurrentEvaluator(
                warmupRounds, benchmarkRounds, totalRounds, threads, options.clock());
        }

        final Result result = evaluator.evaluate();

        for (IResultsConsumer consumer : consumers)
            consumer.accept(result);
    }

    /**
     * Best effort attempt to clean up the memory if {@link z.testware.benchmark.BenchmarkOptions#callgc()} is
     * enabled.
     */
    private void cleanupMemory()
    {
        if (ignoreCallGC) return;
        if (!options.callgc()) return;

        /*
         * Best-effort GC invocation. I really don't know of any other way to ensure a GC
         * pass.
         */
        System.gc();
        System.gc();
        Thread.yield();
    }

    /**
     * Get an integer override from system properties.
     */
    private int getIntOption(int localValue, String property, int defaultValue)
    {
        final String v = System.getProperty(property);
        if (v != null && v.trim().length() > 0)
        {
            defaultValue = Integer.parseInt(v);
        }

        if (ignoreAnnotationOptions || localValue < 0)
        {
            return defaultValue;
        }

        return localValue;
    }
}