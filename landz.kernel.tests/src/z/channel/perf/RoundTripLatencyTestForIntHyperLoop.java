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

package z.channel.perf;

import org.HdrHistogram.Histogram;
import org.junit.Test;
import z.channel.IntHyperLoop;

import java.io.PrintStream;

//import z.znr.Clock;

public class RoundTripLatencyTestForIntHyperLoop {
  private static class MyRunnable implements Runnable
  {
    private IntHyperLoop.OutPort outC;
    private IntHyperLoop loopP;

    public MyRunnable(IntHyperLoop.OutPort outC, IntHyperLoop loopP)
    {
      this.outC = outC;
      this.loopP = loopP;
    }

    @Override
    public void run()
    {
//      Affinity.bindTo(Topology.socket(0).physicalCore(3).virtualCore(0));
      for (int i=0;i<RUNS;i++) {
        try {
          while(outC.notReceivable()){}
          int v = outC.receive();
          while (!loopP.trySend(v)){}
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  private final Histogram HISTOGRAM = new Histogram(10000000000L, 1);

  private static final int RUNS = 30_000_000;

  private IntHyperLoop loopC = new IntHyperLoop(1024);
  private IntHyperLoop.OutPort outC = loopC.createOutPort();

  private IntHyperLoop loopP = new IntHyperLoop(1024);
  private IntHyperLoop.OutPort outP = loopP.createOutPort();

//  @Before
//  public void warmup() {
//    //warmup
//    for (int i = 0; i < 10_000_000; i++) {
//      int s = Clock.rdtsc();
//      Clock.tscToNano(Clock.rdtsc()-s);
//    }
//  }

  @Test
  public void testHyperLoop() throws Exception {
//    Affinity.bindTo(Topology.socket(0).physicalCore(3).virtualCore(1));
    for (int i = 0; i < 3; i++) {
      runHyperLoop();
    }
  }

  private void runHyperLoop() throws Exception {
//    int numRunnings0 = numOfRunningThreads();
    HISTOGRAM.reset();

    new Thread(new MyRunnable(outC, loopP)).start();

    Thread.sleep(1000);
    //XXX: producer is in main thread
    for (int i=0;i<RUNS;i++) {
        int v = 0;
        long s = System.nanoTime();//Clock.rdtsc();
        while (!loopC.trySend(i)){}

        while(outP.notReceivable()){}
        v = outP.receive();
        HISTOGRAM.recordValue(System.nanoTime()-s);//Clock.tscToNano(Clock.rdtsc()-s)
//        assertThat(v, is(i));
    }

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
    System.out.println("================================");
    System.out.println("min: " + histogram.getHistogramData().getMinValue());
    System.out.println("mean: " + histogram.getHistogramData().getMean());
  }

  private static void runFinePrint(String label, Runnable runnable) {
    System.out.println("================================");
    System.out.println(label + " start...");
    runnable.run();
    System.out.println(label + " done.");
    System.out.println("================================");
  }


  public static void main(String[] args) throws Exception {
//    //warmup
//    for (int i = 0; i < 10_000_000; i++) {
//      long s = Clock.rdtsc();
//      Clock.tscToNano(Clock.rdtsc()-s);
//    }

    new RoundTripLatencyTestForIntHyperLoop().testHyperLoop();
  }


}
