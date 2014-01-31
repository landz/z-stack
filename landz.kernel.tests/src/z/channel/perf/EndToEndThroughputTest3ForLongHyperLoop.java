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

/**
 * same to EndToEndThroughputTestForLongHyperLoop except that we test
 * another {@link z.channel.LongHyperLoop.OutPort} recieve API
 */
public class EndToEndThroughputTest3ForLongHyperLoop {

  private static final int RUNS = 500_000_000;//100_000_000;

  private LongHyperLoop loop = new LongHyperLoop();
  private LongHyperLoop.OutPort out = loop.createOutPort();

  @Test
  public void testHyperLoop() {
    System.out.println("test another OUT recieve API");
      runHyperLoop();
  }

  private void runHyperLoop() {
    int numRunnings0 = numOfRunningThreads();
    CountDownLatch endLatch = new CountDownLatch(1);

    new Thread(() -> {
//      Affinity.bindTo(Topology.socket(0).physicalCore(3).virtualCore(0));
      long v;
      for (int i=0;i<RUNS;i++) {
        while(out.notReceivable()){
          Thread.yield();
        }
        v = out.receive();
      }
      endLatch.countDown();
    }).start();

    try {

    long s = System.nanoTime();
    //XXX: producer is in main thread
    for (long i=0;i<RUNS;i++) {
      while (!loop.send(i)){}
    }
    endLatch.await();
    long time = System.nanoTime()-s;

    long opsPerSecond = RUNS * 1000_000_000L / time;
    System.out.printf("%,d ops/sec\n", opsPerSecond);

    assertThat(numOfRunningThreads(), is(numRunnings0));

    }catch (Exception e){e.printStackTrace();}
  }

  private static final int numOfRunningThreads() {
    return (int)
        Thread.getAllStackTraces().keySet().stream()
            .filter(t -> t.getState() == Thread.State.RUNNABLE)
            .count();
  }

  public static void main(String[] args) {
//    Affinity.bindTo(Topology.socket(0).physicalCore(2).virtualCore(0));
    System.out.println("start...");
    new EndToEndThroughputTest3ForLongHyperLoop().testHyperLoop();
  }


}
