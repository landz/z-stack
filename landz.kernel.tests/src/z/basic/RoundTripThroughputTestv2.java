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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedTransferQueue;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static com.lmax.disruptor.RingBuffer.createSingleProducer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static z.util.Contracts.contract;

/**
 */
public class RoundTripThroughputTestv2 {

  private static final int BUFFER_SIZE = 1024;
  private RingBuffer<ValueEvent> ringBufferC =
      createSingleProducer(ValueEvent.EVENT_FACTORY, BUFFER_SIZE, new YieldingWaitStrategy());
  private RingBuffer<ValueEvent> ringBufferP =
      createSingleProducer(ValueEvent.EVENT_FACTORY, BUFFER_SIZE, new YieldingWaitStrategy());

  private static final int RUNS = 500_000_000;
  public static final Object OBJECT = new Object();

  @Test
  public void testDisruptor() {
    runFinePrint("for Disruptor", () -> runDisruptor());
  }

  private void runDisruptor() {
    CountDownLatch endLatch = new CountDownLatch(1);

    BatchEventProcessor<ValueEvent> processorC = new BatchEventProcessor<>(//BUG?: can not infer type if no diamond
        ringBufferC,
        ringBufferC.newBarrier(),
        (event, seq, endOfBatch) -> {
          // Publishers claim events in sequence
          long sequence = ringBufferP.next();
          ringBufferP.get(sequence).setValue(event.getValue());
          ringBufferP.publish(sequence);
          assertThat(event.getValue(),is(OBJECT));
        }
    );

    // Each processor runs on a separate thread
    Thread t = new Thread(processorC);
    t.start();

    long s = System.nanoTime();
    IntStream.range(0, RUNS).forEach((i) -> {
      // Publishers claim events in sequence
      long seqC = ringBufferC.next();
      ValueEvent event = ringBufferC.get(seqC);
      event.setValue(OBJECT);
      // publish the event
      ringBufferC.publish(seqC);
    });

    long time = System.nanoTime()-s ;
    long opsPerSecond = RUNS * 1000_000_000L / time;
    System.out.printf("%,d ops/sec\n", opsPerSecond);
  }


  private static void runFinePrint(String label, Runnable runnable) {
    System.out.println("================================");
    System.out.println(label + " start...");
    runnable.run();
    System.out.println(label + " done.");
    System.out.println("================================");
  }


  public static void main(String[] args) {
    new RoundTripThroughputTestv2().testDisruptor();
  }

  static final class ValueEvent {
    private Object value;

    public Object getValue() {
      return value;
    }

    public void setValue(final Object value) {
      this.value = value;
    }

    public static final EventFactory<ValueEvent> EVENT_FACTORY = new EventFactory<ValueEvent>() {
      public ValueEvent newInstance() {
        return new ValueEvent();
      }
    };
  }

}