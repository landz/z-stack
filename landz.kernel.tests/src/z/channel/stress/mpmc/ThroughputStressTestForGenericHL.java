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

package z.channel.stress.mpmc;

import org.junit.Test;
import z.channel.GenericHyperLoop;
import z.channel.ReceivePort;
import z.znr.Affinity;
import z.znr.Affinity.Topology;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ThroughputStressTestForGenericHL {

  private static final int RUNS = 20_000_000;//100_000_000;

  private GenericHyperLoop<String> loop = new GenericHyperLoop();
  private ReceivePort<String> out = loop.createReceivePort();

  @Test
  public void testHyperLoop() {
    for (int i = 0; i < 5; i++) {
      runHyperLoopStress("#"+i);
    }
  }

  private void runHyperLoopStress(String label) {
    System.out.println("=====in Round "+label);
    int numRunnings0 = numOfRunningThreads();

    CountDownLatch endLatch = new CountDownLatch(1);
    String[] values = new String[RUNS];
    ThreadLocalRandom rnd = ThreadLocalRandom.current();
    IntStream.range(0, RUNS).forEach( i->
        values[i]=Long.toHexString(rnd.nextLong())
    );

    System.out.println("start stress...");
    new Thread(() -> {
      Affinity.bindTo(Topology.socket(0).physicalCore(3).virtualCore(0));
      String v;
      for (int i=0;i<RUNS;i++) {
        while(out.notReceivable()){}
        v = out.tryReceive();
        assertThat(v,is(values[i]));
      }
      endLatch.countDown();
    }).start();

    try {
    long s = System.nanoTime();
    //XXX: producer is in main thread
    for (int i=0;i<RUNS;i++) {
      while (!loop.trySend(values[i])){}
    }
    endLatch.await();
      long time = System.nanoTime()-s;
      System.out.printf("done with costed time: %,d\n",time);

    assertThat(numOfRunningThreads(), is(numRunnings0));

    }catch (Exception e){e.printStackTrace();}
  }

  private static final int numOfRunningThreads() {
    return (int)
        Thread.getAllStackTraces().keySet().stream()
            .filter(t -> t.getState() == Thread.State.RUNNABLE)
            .count();
  }

}
