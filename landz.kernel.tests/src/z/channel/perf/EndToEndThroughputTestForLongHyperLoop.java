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

import org.junit.Test;
import z.channel.LongHyperLoop;

import java.util.concurrent.CountDownLatch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class EndToEndThroughputTestForLongHyperLoop {

  private static class MyRunnable implements Runnable
  {
    private CountDownLatch latch;
    private LongHyperLoop.OutPort out;

    public MyRunnable(LongHyperLoop.OutPort out,CountDownLatch latch)
    {
      this.out = out;
      this.latch = latch;
    }

    @Override
    public void run()
    {
      long v;
      for (long i=0;i<RUNS;i++) {
        v = out.received();
      }
      latch.countDown();
    }
  }

  private static final int RUNS = 500_000_000;

  private LongHyperLoop loop = new LongHyperLoop(64*1024);
  private LongHyperLoop.OutPort out = loop.createOutPort();

  @Test
  public void testHyperLoop() throws Exception {
    runHyperLoop();
  }

  private void runHyperLoop() throws Exception {
//    int numRunnings0 = numberOfRunningThreads();
    CountDownLatch endLatch = new CountDownLatch(1);

    new Thread(new MyRunnable(out,endLatch)).start();


    long s = System.nanoTime();
    //XXX: producer is in main thread
    for (long i=0;i<RUNS;i++) {
      while (!loop.send(i)){}
    }
    endLatch.await();

    long opsPerSecond = RUNS * 1000_000_000L / (System.nanoTime()-s);
    System.out.printf("%,d ops/sec\n", opsPerSecond);

//    assertThat(numberOfRunningThreads(), is(numRunnings0));

  }

  private static final int numberOfRunningThreads() {
    return (int)
        Thread.getAllStackTraces().keySet().stream()
            .filter(t -> t.getState() == Thread.State.RUNNABLE)
            .count();
  }

  public static void main(String[] args) throws Exception {
//    Affinity.bindTo(Topology.socket(0).physicalCore(2).virtualCore(0));
//    System.out.println("start...");
    new EndToEndThroughputTestForLongHyperLoop().testHyperLoop();
  }


}
