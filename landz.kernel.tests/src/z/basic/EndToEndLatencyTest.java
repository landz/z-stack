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

import org.HdrHistogram.Histogram;
import org.junit.Ignore;
import org.junit.Test;

import java.io.PrintStream;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import static z.util.Throwables.uncheck;

/**
 * Note: the time of instantizate new Long value for passing
 *
 */
public class EndToEndLatencyTest {

  private static final Histogram HISTOGRAM = new Histogram(2000_000_000L, 0);

  private LinkedTransferQueue<Long> ltq = new LinkedTransferQueue();
  private SynchronousQueue<Long> sq = new SynchronousQueue();
  //XXX: ABQ/CLQ, not appropriate for one-shot latency, just placeholder
  private ArrayBlockingQueue<Long> abq = new ArrayBlockingQueue(1024);
  private ConcurrentLinkedQueue<Long> clq = new ConcurrentLinkedQueue();

  private static final int RUNS = 10_000_000;

  public void runLTQ() {
    int numRunnings0 = numOfRunningThreads();
    HISTOGRAM.reset();
    new Thread(() -> {
      IntStream.range(0, RUNS).forEach((i) ->
          uncheck(() -> {
            Long t = ltq.take();
            long now = System.nanoTime();
            HISTOGRAM.recordValue(now - t);
          })
      );
    }).start();

    //XXX: producer is in main thread
    IntStream.range(0, RUNS).forEach((i) ->
        uncheck(() ->
            ltq.transfer(new Long(System.nanoTime())))
    );

    assertThat(ltq.size(), is(0));
    assertThat(numOfRunningThreads(), is(numRunnings0));
    dumpHistogram(HISTOGRAM, System.out);
  }

  public void runSQ() {
    int numRunnings0 = numOfRunningThreads();
    HISTOGRAM.reset();
    new Thread(() -> {
      IntStream.range(0, RUNS).forEach((i) ->
          uncheck(() -> {
            Long t = sq.take();
            long now = System.nanoTime();
            HISTOGRAM.recordValue(now - t);
          })
      );
    }).start();

    //XXX: producer is in main thread
    IntStream.range(0, RUNS).forEach((i) ->
        uncheck(() ->
            sq.put(new Long(System.nanoTime())))
    );

    assertThat(sq.size(), is(0));
    assertThat(numOfRunningThreads(), is(numRunnings0));
    dumpHistogram(HISTOGRAM, System.out);
  }


  public void runABQ() {
    int numRunnings0 = numOfRunningThreads();
    HISTOGRAM.reset();
    CountDownLatch latch = new CountDownLatch(2);
    new Thread(() -> {
      IntStream.range(0, RUNS).forEach((i) ->
          uncheck(() -> {
            Long t = abq.take();
            long now = System.nanoTime();
            HISTOGRAM.recordValue(now - t);
          })
      );
      latch.countDown();
    }).start();

    //XXX: producer is in main thread
    IntStream.range(0, RUNS).forEach((i) ->
        uncheck(() ->
            abq.put(new Long(System.nanoTime())))
    );
    latch.countDown();

    uncheck(() -> latch.await());

    assertThat(abq.size(), is(0));
    assertThat(numOfRunningThreads(), is(numRunnings0));
    dumpHistogram(HISTOGRAM, System.out);
  }

  public void runCLQ() {
    int numRunnings0 = numOfRunningThreads();
    HISTOGRAM.reset();
    CountDownLatch latch = new CountDownLatch(2);

    new Thread(() -> {
      IntStream.range(0, RUNS).forEach((i) ->
          uncheck(() -> {
            Long t = null;
            while (t==null)
              t = clq.poll();
            long now = System.nanoTime();
            HISTOGRAM.recordValue(now - t);
          })
      );
      latch.countDown();
    }).start();

    //XXX: producer is in main thread
    IntStream.range(0, RUNS).forEach((i) ->
        uncheck(() ->
            clq.offer(new Long(System.nanoTime())))
    );
    latch.countDown();

    uncheck(() -> latch.await());

    assertThat(clq.size(), is(0));
    assertThat(numOfRunningThreads(), is(numRunnings0));
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
    new EndToEndLatencyTest().testLTQ();
    new EndToEndLatencyTest().testSQ();
//    new EndToEndLatencyTest().testABQ();
//    new EndToEndLatencyTest().testCLQ();
  }

  @Test
  public void testLTQ() {
    for (int i=0;i<3;i++)
      runFinePrint("for LinkedTransferQueue", () -> runLTQ());
  }

  @Ignore
  @Test
  public void testSQ() {
    runFinePrint("for SynchronousQueue", () -> runSQ());
  }

  @Ignore
  @Test
  public void testABQ() {
    runFinePrint("for ArrayBlockingQueue", () -> runABQ());
  }

  @Ignore
  @Test
  public void testCLQ() {
    runFinePrint("for ConcurrentLinkedQueue", () -> runCLQ());
  }

}