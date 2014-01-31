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

package z.channel.stress;

import org.junit.Test;
import z.channel.LongHyperLoop;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static z.util.Throwables.uncheck;

/**
 * in this stress, add 7 threads to consume one HL without setting cpu affinity
 *
 * assumed to be run on 4/8 modern intel64 platform
 *
 */
public class ThroughputStressTest2NoAffinityForLongHL {

  private static final int RUNS = 200_000_000;//100_000_000;

  private LongHyperLoop loop = new LongHyperLoop(1024);
  private LongHyperLoop.OutPort[] outs = new LongHyperLoop.OutPort[7];

  public ThroughputStressTest2NoAffinityForLongHL() {
    IntStream.range(0,7).forEach(i->outs[i] = loop.createOutPort());
  }

  @Test
  public void testHyperLoop() {
    for (int i = 0; i < 5; i++) {
      System.gc();
      runHyperLoopStress("#" + i);
    }
  }

  private void runHyperLoopStress(String label) {
    System.out.println("=====in Round "+label);
    int numRunnings0 = numOfRunningThreads();

    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch endLatch = new CountDownLatch(7);

    long[] values = new long[RUNS];
    ThreadLocalRandom rnd = ThreadLocalRandom.current();
    IntStream.range(0, RUNS).forEach( i->
        values[i]=rnd.nextLong()
    );

    System.out.println("start stress...");

    //
//    IntStream.range(0,7).forEach(i->{
//    });
    spawnConsumer(outs[0],values,startLatch,endLatch).start();
    spawnConsumer(outs[1],values,startLatch,endLatch).start();
    spawnConsumer(outs[2],values,startLatch,endLatch).start();
    spawnConsumer(outs[3],values,startLatch,endLatch).start();
    spawnConsumer(outs[4],values,startLatch,endLatch).start();
    spawnConsumer(outs[5],values,startLatch,endLatch).start();
    spawnConsumer(outs[6],values,startLatch,endLatch).start();

    startLatch.countDown();

    try {
    long s = System.nanoTime();
    //XXX: producer is in main thread
    for (int i=0;i<RUNS;i++) {
      while (!loop.send(values[i])){}
    }

    endLatch.await();
    long time = System.nanoTime()-s;
    System.out.printf("ThroughputStressTest2NoAffinityForLongHL " +
        "done with costed time: %,d\n", time);

      //for safety, we wait 1s for the termination of all consumers .
      Thread.sleep(1000L);
      assertThat(numOfRunningThreads(), is(numRunnings0));
    }catch (Exception e){e.printStackTrace();}
  }

  private static final int numOfRunningThreads() {
    return (int)
        Thread.getAllStackTraces().keySet().stream()
            .filter(t -> t.getState() == Thread.State.RUNNABLE)
            .count();
  }

  private Thread spawnConsumer(
      LongHyperLoop.OutPort out,
      long[] expectedValues,
      CountDownLatch startLatch,
      CountDownLatch endLatch) {
    return
        new Thread(() -> {
          uncheck(() -> startLatch.await());

          long v;
          for (int i = 0; i < RUNS; i++) {
            while (out.notReceivable()) {
            }
            v = out.receive();
            assertThat(v, is(expectedValues[i]));
          }
          endLatch.countDown();
        });
  }

}
