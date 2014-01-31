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

package z.channel.contrast;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ContendedEndToEndThroughputTest {

  private static final int RUNS = 200_000_000;

  private ContendedHyperLoop loop = new ContendedHyperLoop(64*1024);
  private ContendedHyperLoop.Out out = loop.new Out();

  @Test
  public void testContendedHyperLoop() {
    System.out.println("ContendedEndToEndThroughputTest start...");
    for (int i = 0; i < 15; i++) {
      runContendedHyperLoop();
    }
  }

  private void runContendedHyperLoop() {
    int numRunnings0 = numOfRunningThreads();
    CountDownLatch endLatch = new CountDownLatch(1);

    new Thread(() -> {
//      Affinity.bindTo(Topology.socket(0).physicalCore(3).virtualCore(0));
      for (long i=0;i<RUNS;i++) {
        out.receive();
      }
      endLatch.countDown();
    }).start();

    try {

    long s = System.nanoTime();
    //XXX: producer is in main thread
    for (long i=0;i<RUNS;i++) {
      loop.sendTo(i);
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
    new ContendedEndToEndThroughputTest().testContendedHyperLoop();
  }


}
