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
package z.basic;

import com.lmax.disruptor.*;
import com.lmax.disruptor.util.PaddedLong;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedTransferQueue;

import static com.lmax.disruptor.RingBuffer.createSingleProducer;

/**
 * <pre>
 * UniCast a series of items between 1 publisher and 1 event processor.
 *
 * +----+    +-----+
 * | P1 |--->| EP1 |
 * +----+    +-----+
 *
 *
 * Queue Based:
 * ============
 *
 *        put      take
 * +----+    +====+    +-----+
 * | P1 |--->| Q1 |<---| EP1 |
 * +----+    +====+    +-----+
 *
 * P1  - Publisher 1
 * Q1  - Queue 1
 * EP1 - EventProcessor 1
 *
 *
 * Disruptor:
 * ==========
 *              track to prevent wrap
 *              +------------------+
 *              |                  |
 *              |                  v
 * +----+    +====+    +====+   +-----+
 * | P1 |--->| RB |<---| SB |   | EP1 |
 * +----+    +====+    +====+   +-----+
 *      claim      get    ^        |
 *                        |        |
 *                        +--------+
 *                          waitFor
 *
 * P1  - Publisher 1
 * RB  - Hyperloop
 * SB  - SequenceBarrier
 * EP1 - EventProcessor 1
 *
 * </pre>
 */
public final class OnePublisherToOneProcessorUniCastThroughputTest
{

    //==================AbstractPerfTestQueueVsDisruptor=============================
    public static final int RUNS = 5;

    protected void testImplementations()
            throws Exception
    {
        final int availableProcessors = Runtime.getRuntime().availableProcessors();
        if (getRequiredProcessorCount() > availableProcessors)
        {
            System.out.print("*** Warning ***: your system has insufficient processors to execute the test efficiently. ");
            System.out.println("Processors required = " + getRequiredProcessorCount() + " available = " + availableProcessors);
        }

        long[] queueOps = new long[RUNS];
        long[] disruptorOps = new long[RUNS];

        if ("true".equalsIgnoreCase(System.getProperty("com.lmax.runQueueTests", "false")))
        {
            System.out.println("Starting Queue tests...");
            for (int i = 0; i < RUNS; i++)
            {
                System.gc();
                queueOps[i] = runQueuePass();
                System.out.format("Run %d, TransferQueue=%,d ops/sec%n", i, Long.valueOf(queueOps[i]));
            }
        }
        else
        {
            System.out.println("Skipping Queue tests");
        }

        System.out.println("Starting Disruptor tests...");
        for (int i = 0; i < RUNS; i++)
        {
            System.gc();
            disruptorOps[i] = runDisruptorPass();
            System.out.format("Run %d, Disruptor=%,d ops/sec%n", i, Long.valueOf(disruptorOps[i]));
        }

//        printResults(getClass().getSimpleName(), disruptorOps, queueOps);

        for (int i = 0; i < RUNS; i++)
        {
            Assert.assertTrue("Performance degraded", disruptorOps[i] > queueOps[i]);
        }
    }

    public static void printResults(final String className, final long[] disruptorOps, final long[] queueOps)
    {
        for (int i = 0; i < RUNS; i++)
        {
            System.out.format("%s run %d: BlockingQueue=%,d Disruptor=%,d ops/sec\n",
                    className, Integer.valueOf(i), Long.valueOf(queueOps[i]), Long.valueOf(disruptorOps[i]));
        }
    }


    public static long accumulatedAddition(final long iterations)
    {
        long temp = 0L;
        for (long i = 0L; i < iterations; i++)
        {
            temp += i;
        }

        return temp;
    }


    private static final int BUFFER_SIZE = 1024*2; //XXX: 4K:)
    private static final long ITERATIONS = 1000L * 1000L * 100L;//XXX: decrease for testing
    private final long expectedResult = accumulatedAddition(ITERATIONS);

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private final LinkedTransferQueue<Long> transferQueue = new LinkedTransferQueue<>();//new LinkedBlockingQueue<Long>(BUFFER_SIZE);
    private final ValueAdditionProcessorOfQueue queueProcessor = new ValueAdditionProcessorOfQueue(transferQueue, ITERATIONS - 1);

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private final RingBuffer<ValueEvent> ringBuffer =
        createSingleProducer(ValueEvent.EVENT_FACTORY, BUFFER_SIZE, new YieldingWaitStrategy());
    private final SequenceBarrier sequenceBarrier = ringBuffer.newBarrier();
    private final ValueAdditionEventHandlerForDisruptor handler = new ValueAdditionEventHandlerForDisruptor();
    private final BatchEventProcessor<ValueEvent> batchEventProcessor = new BatchEventProcessor<ValueEvent>(ringBuffer, sequenceBarrier, handler);
    {
        ringBuffer.addGatingSequences(batchEventProcessor.getSequence());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected int getRequiredProcessorCount()
    {
        return 2;
    }

    @Test
    public void shouldCompareDisruptorVsQueues() throws Exception
    {
        testImplementations();
    }

    protected long runQueuePass() throws InterruptedException
    {
        final CountDownLatch latch = new CountDownLatch(1);
        queueProcessor.reset(latch);
        new Thread(queueProcessor).start();
        long start = System.currentTimeMillis();

        for (long i = 0; i < ITERATIONS; i++)
        {
            transferQueue.put(new Long(i));//XXX: transfer -> put will improve throughput
        }

        latch.await();
        long opsPerSecond = (ITERATIONS * 1000L) / (System.currentTimeMillis() - start);
        queueProcessor.halt();

        Assert.assertEquals(expectedResult, queueProcessor.getValue());

        return opsPerSecond;
    }

    protected long runDisruptorPass() throws InterruptedException
    {
        final CountDownLatch latch = new CountDownLatch(1);
        long expectedCount = batchEventProcessor.getSequence().get() + ITERATIONS;
        handler.reset(latch, expectedCount);
        new Thread(batchEventProcessor).start();
        long start = System.currentTimeMillis();

        final RingBuffer<ValueEvent> rb = ringBuffer;

        for (long i = 0; i < ITERATIONS; i++)
        {
            long next = rb.next();
            rb.get(next).setValue(i);
            rb.publish(next);
        }

        latch.await();
        long opsPerSecond = (ITERATIONS * 1000L) / (System.currentTimeMillis() - start);
        waitForEventProcessorSequence(expectedCount);
        batchEventProcessor.halt();

        Assert.assertEquals(expectedResult, handler.getValue());

        return opsPerSecond;
    }

    private void waitForEventProcessorSequence(long expectedCount) throws InterruptedException
    {
        while (batchEventProcessor.getSequence().get() != expectedCount)
        {
            Thread.sleep(1);
        }
    }

    public static void main(String[] args) throws Exception
    {
        OnePublisherToOneProcessorUniCastThroughputTest test = new OnePublisherToOneProcessorUniCastThroughputTest();
        test.shouldCompareDisruptorVsQueues();
    }
}

final class ValueAdditionProcessorOfQueue implements Runnable
{
    private volatile boolean running;
    private long value;
    private long sequence;
    private CountDownLatch latch;

    private final LinkedTransferQueue<Long> transferQueue;
    private final long count;

    public ValueAdditionProcessorOfQueue(final LinkedTransferQueue<Long> transferQueue, final long count)
    {
        this.transferQueue = transferQueue;
        this.count = count;
    }

    public long getValue()
    {
        return value;
    }

    public void reset(final CountDownLatch latch)
    {
        value = 0L;
        sequence = 0L;
        this.latch = latch;
    }

    public void halt()
    {
        running = false;
    }

    @Override
    public void run()
    {
        running = true;
        while (running)
        {
                Long v = null;
                while (v==null) {
                    v = transferQueue.poll();
                }
                this.value += v.longValue();

                if (sequence++ == count)
                {
                    latch.countDown();
                }
        }
    }
}


final class ValueAdditionEventHandlerForDisruptor implements EventHandler<ValueEvent>
{
    private final PaddedLong value = new PaddedLong();
    private long count;
    private CountDownLatch latch;
    private long localSequence = -1;

    public long getValue()
    {
        return value.get();
    }

    public void reset(final CountDownLatch latch, final long expectedCount)
    {
        value.set(0L);
        this.latch = latch;
        count = expectedCount;
    }

    @Override
    public void onEvent(final ValueEvent event, final long sequence, final boolean endOfBatch) throws Exception
    {
        value.set(value.get() + event.getValue());

        if (localSequence + 1 == sequence)
        {
            localSequence = sequence;
        }
        else
        {
            System.err.println("Expected: " + (localSequence + 1) + "found: " + sequence);
        }

        if (count == sequence)
        {
            latch.countDown();
        }
    }
}


