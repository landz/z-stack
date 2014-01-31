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
import org.HdrHistogram.Histogram;

import org.junit.Ignore;
import org.junit.Test;

import java.io.PrintStream;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static com.lmax.disruptor.RingBuffer.createSingleProducer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static z.util.Throwables.uncheck;

/**
 */
public class RoundTripLatencyTest {

  private static final Histogram HISTOGRAM = new Histogram(2000_000_000L, 0);

  private LinkedTransferQueue<Long> ltqC = new LinkedTransferQueue();
  private LinkedTransferQueue<Long> ltqP = new LinkedTransferQueue();

  private SynchronousQueue<Long> sqC = new SynchronousQueue();
  private SynchronousQueue<Long> sqP = new SynchronousQueue();

  private static final int BUFFER_SIZE = 2;//XXX: or 1/1024, not significant
  private RingBuffer<ValueEvent> ringBufferC =
      createSingleProducer(ValueEvent.EVENT_FACTORY, BUFFER_SIZE, new YieldingWaitStrategy());
  private RingBuffer<ValueEvent> ringBufferP =
      createSingleProducer(ValueEvent.EVENT_FACTORY, BUFFER_SIZE, new YieldingWaitStrategy());

  //XXX: ABQ/CLQ, not appropriate for one-shot latency measuring, just placeholder
  private ArrayBlockingQueue<Long> abq = new ArrayBlockingQueue(1024);
  private ConcurrentLinkedQueue<Long> clq = new ConcurrentLinkedQueue();

  private static final int RUNS = 10_000_000;

  private static final Long[] preallocatedLongs = new Long[RUNS];
  static {
    IntStream.range(0,RUNS).forEach((i) ->
        preallocatedLongs[i] = new Long(i)
    );
    System.out.println("pre-allocation done.");
  }

  @Test
  public void testLTQ() {
    runFinePrint("for LinkedTransferQueue", () -> runLTQ());
  }

  public void runLTQ() {
    int numRunnings0 = numOfRunningThreads();
    HISTOGRAM.reset();
    new Thread(() -> {
      IntStream.range(0, RUNS).forEach((i) -> {
        try {
          Long v = null;
          while (v == null) {
            v = ltqC.poll();
          }
          ltqP.transfer(v);
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
    }).start();

    //XXX: producer is in main thread
    IntStream.range(0, RUNS).forEach((i) ->
        uncheck(() -> {
          long t = System.nanoTime();
          ltqC.transfer(preallocatedLongs[i]);
          Long v = null;
          while (v == null) {
            v = ltqP.poll();
          }
          HISTOGRAM.recordValue(System.nanoTime() - t);
          assertThat(v.longValue(), is((long) i));
        })
    );

    assertThat(ltqC.size(), is(0));
    assertThat(ltqP.size(), is(0));
    assertThat(numOfRunningThreads(), is(numRunnings0));
    dumpHistogram(HISTOGRAM, System.out);
  }

  @Ignore
  @Test
  public void testSQ() {
    runFinePrint("for SynchronousQueue", () -> runSQ());
  }

  public void runSQ() {
    int numRunnings0 = numOfRunningThreads();
    HISTOGRAM.reset();
    new Thread(() -> {
      IntStream.range(0, RUNS).forEach((i) ->
          uncheck(() -> sqP.put(sqC.take()))
      );
    }).start();

    //XXX: producer is in main thread
    IntStream.range(0, RUNS).forEach((i) ->
        uncheck(() -> {
          long t = System.nanoTime();
          sqC.put(preallocatedLongs[i]);
          Long v = sqP.take();
          HISTOGRAM.recordValue(System.nanoTime() - t);
          assertThat(v.longValue(), is((long) i));
        })
    );

    assertThat(sqC.size(), is(0));
    assertThat(sqP.size(), is(0));
    assertThat(numOfRunningThreads(), is(numRunnings0));
    dumpHistogram(HISTOGRAM, System.out);
  }

  @Test
  public void testDisruptor() {
    runFinePrint("for Disruptor", () -> runDisruptor());
  }

  private void runDisruptor() {
    int numRunnings0 = numOfRunningThreads();
    HISTOGRAM.reset();

    BatchEventProcessor<ValueEvent> processorC = new BatchEventProcessor<>(//BUG: can not inference type if no diamond
        ringBufferC,
        ringBufferC.newBarrier(),
        (event, sequence, endOfBatch) -> {
          // Publishers claim events in sequence
//          long sequence = ringBufferP.next();
          ringBufferP.get(sequence).setValue(event.getValue());
          ringBufferP.publish(sequence);
        }
    );

    // Each processor runs on a separate thread
    new Thread(processorC).start();

    final SequenceBarrier sbP = ringBufferP.newBarrier();

    IntStream.range(0, RUNS).forEach((i) -> {
      long t = System.nanoTime();
      // Publishers claim events in sequence
      long seqC = ringBufferC.next();
      ValueEvent event = ringBufferC.get(seqC);
      event.setValue(preallocatedLongs[i]);
      // publish the event
      ringBufferC.publish(seqC);
      //
      try {
        long availableSequence = sbP.waitFor(i);
        ValueEvent e = ringBufferP.get(availableSequence);
        HISTOGRAM.recordValue(System.nanoTime() - t);
        assertThat(availableSequence, is((long) i));
        assertThat(e.getValue(), is((long) i));
      } catch (final Exception e) {
        throw new RuntimeException(e);//just rethrow
      }
    });

//    assertThat(numOfRunningThreads(), is(numRunnings0));
    dumpHistogram(HISTOGRAM, System.out);
  }

  private static final int numOfRunningThreads() {
    return (int)
        Thread.getAllStackTraces().keySet().stream()
            .filter(t -> t.getState() == Thread.State.RUNNABLE)
            .count();
  }

  private static void dumpHistogram(final Histogram histogram, final PrintStream out) {
    histogram.getHistogramData().outputPercentileDistribution(out, 1, 1.0);
  }

  private static void runFinePrint(String label, Runnable runnable) {
    System.out.println("================================");
    System.out.println(label + " start...");
    runnable.run();
    System.out.println(label + " done.");
    System.out.println("================================");
  }


  public static void main(String[] args) {
    new RoundTripLatencyTest().testLTQ();
    new RoundTripLatencyTest().testDisruptor();
  }

  static final class ValueEvent {
    private Long value;

    public Long getValue() {
      return value;
    }

    public void setValue(final Long value) {
      this.value = value;
    }

    public static final EventFactory<ValueEvent> EVENT_FACTORY = new EventFactory<ValueEvent>() {
      public ValueEvent newInstance() {
        return new ValueEvent();
      }
    };
  }

}