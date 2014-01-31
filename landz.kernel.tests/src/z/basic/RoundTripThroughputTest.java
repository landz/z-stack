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
import z.testware.common.Stopwatch;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static com.lmax.disruptor.RingBuffer.createSingleProducer;
import static z.util.Contracts.contract;
import static z.util.Throwables.uncheck;

/**
 */
public class RoundTripThroughputTest {

  private LinkedTransferQueue<Integer> ltqC = new LinkedTransferQueue();
  private LinkedTransferQueue<Integer> ltqP = new LinkedTransferQueue();

  private static final int BUFFER_SIZE = 1024;
  private RingBuffer<ValueEvent> ringBufferC =
      createSingleProducer(ValueEvent.EVENT_FACTORY, BUFFER_SIZE, new YieldingWaitStrategy());
  private RingBuffer<ValueEvent> ringBufferP =
      createSingleProducer(ValueEvent.EVENT_FACTORY, BUFFER_SIZE, new YieldingWaitStrategy());

  private ConcurrentLinkedQueue<Integer> clqC = new ConcurrentLinkedQueue();
  private ConcurrentLinkedQueue<Integer> clqP = new ConcurrentLinkedQueue();

  private static final int RUNS = 10_000_000;
  private static final Integer[] preallocatedInts = new Integer[RUNS+1];
  static {
    IntStream.range(0, RUNS+1).forEach((i) ->
        preallocatedInts[i] = new Integer(i)
    );
    System.out.println("pre-allocation done.");
  }

  @Ignore
  @Test
  public void testLTQ() {
    runFinePrint("for LinkedTransferQueue", () -> runLTQ());
  }

  public void runLTQ() {
    int numRunnings0 = numOfRunningThreads();
    List<Integer> bufferC = new ArrayList<>(2024 * 2048);
    List<Integer> bufferP = new ArrayList<>(2024 * 2048);

    //consumer thread
    new Thread(() -> {
      IntStream.range(0, RUNS).forEach((i) -> {
        try {
          int num = 0;
          while (num == 0) {
            num = ltqC.drainTo(bufferC);
            Thread.yield();
          }
          for (int k = 0; k < num; k++) {//XXX: num guarantee the last available index?
            ltqP.put(preallocatedInts[bufferC.get(k).intValue() + 1]);
          }
          bufferC.clear();
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
    }).start();

    //producer is in main thread?
    Stopwatch watch = Stopwatch.create();
    watch.start();
//    LongStream.range(0, RUNS).forEach((i) ->
    for (int p = 0; p < RUNS; p++) {
      try {
        ltqC.put(preallocatedInts[p]);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
//    watch.print();
//    );

    //XXX: fetch after put?
    int count = 0;
    while (count != RUNS) {
      int num = 0;
      while (num == 0) {
        num = ltqP.drainTo(bufferP);
      }
//      for (int k = 0; k < num; k++) {
//        final long checked = bufferP.get(k).longValue();
//      }
      bufferP.clear();
      count += num;
    }
    watch.stop();
    long opsPerSecond = RUNS * 1000_000_000L / watch.elapsed();//XXX: *2 for EndToEnd?
    System.out.printf("%,d ops/sec\n", opsPerSecond);

    contract(() -> ltqC.size() == 0);
    contract(() -> ltqP.size() == 0);
    contract(() -> numOfRunningThreads() == numRunnings0);
  }

  @Test
  public void testCLQ() {
    runFinePrint("for ConcurrentLinkedQueue", () -> runCLQ());
  }

  public void runCLQ() {
    int numRunnings0 = numOfRunningThreads();

    //consumer thread
    new Thread(() -> {
      LongStream.range(0, RUNS).forEach((i) -> {
        try {
          Integer v = null;
          while (v == null) {
            v = clqC.poll();
          }
          clqP.offer(preallocatedInts[v.intValue() + 1]);
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
    }).start();

    //producer is in main thread?
    Stopwatch watch = Stopwatch.create();
    watch.start();
//    LongStream.range(0, RUNS).forEach((i) ->
    for (int i = 0; i < RUNS; i++) {
      try {
        clqC.offer(preallocatedInts[i]);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
//    watch.print();
//    );

    //XXX: fetch after put?
    int count = 0;
    for (int i = 0; i < RUNS; i++) {
      Integer v = clqP.poll();
//      final int checked = i;
//      contract(()-> v==(checked+1));
    }
    watch.stop();
//    watch.print();
    long opsPerSecond = RUNS * 1000_000_000L / watch.elapsed();//XXX: *2 for EndToEnd?
    System.out.printf("%,d ops/sec\n", opsPerSecond);

    contract(() -> clqC.size() == 0);
    contract(() -> clqP.size() == 0);
    contract(() -> numOfRunningThreads() == numRunnings0);
  }

  @Test
  public void testDisruptor() {
    runFinePrint("for Disruptor", () -> runDisruptor());
  }

  private void runDisruptor() {
    BatchEventProcessor<ValueEvent> processorC = new BatchEventProcessor<>(//BUG?: can not infer type if no diamond
        ringBufferC,
        ringBufferC.newBarrier(),
        (event, sequence, endOfBatch) -> {
          // Publishers claim events in sequence
//          long sequence = ringBufferP.next();
          ringBufferP.get(sequence).setValue(preallocatedInts[event.getValue().intValue() + 1]);
          ringBufferP.publish(sequence);
        }
    );

    // Each processor runs on a separate thread
    new Thread(processorC).start();

    final SequenceBarrier sbP = ringBufferP.newBarrier();

    Stopwatch watch = Stopwatch.create();
    watch.start();
    IntStream.range(0, RUNS).forEach((i) -> {
      // Publishers claim events in sequence
      long seqC = ringBufferC.next();
      ValueEvent event = ringBufferC.get(seqC);
      event.setValue(preallocatedInts[i]);
      // publish the event
      ringBufferC.publish(seqC);
    });

    //
    long availableSequence = -1;
//    while (availableSequence != RUNS - 1) {
//      availableSequence = sbP.getCursor();
//    }
    for (int i = 0; i < RUNS; i++) {
      try {
      availableSequence = sbP.waitFor(i);
      }catch (Exception e){
        e.printStackTrace();
      }
    }

    watch.stop();
    long opsPerSecond = RUNS * 1000_000_000L / watch.elapsed();//XXX: *2 for EndToEnd?
    System.out.printf("%,d ops/sec\n", opsPerSecond);

  }

  /**
   * NOTE: the state of RUNNABLE or not is not important here
   *
   * @return
   */
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
//    new EndToEndThroughputTestForLongHyperLoop().testLTQ();
//    new EndToEndThroughputTestForLongHyperLoop().testCLQ();
    new RoundTripThroughputTest().testDisruptor();
  }

  static final class ValueEvent {
    private Integer value;

    public Integer getValue() {
      return value;
    }

    public void setValue(final Integer value) {
      this.value = value;
    }

    public static final EventFactory<ValueEvent> EVENT_FACTORY = new EventFactory<ValueEvent>() {
      public ValueEvent newInstance() {
        return new ValueEvent();
      }
    };
  }

}