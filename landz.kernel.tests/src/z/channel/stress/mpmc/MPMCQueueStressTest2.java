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
import z.channel.MPMCQueue;
import z.znr.Affinity;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static z.util.Throwables.uncheck;

/**
 * spawn 8 threads to test
 *
 * NOTE:
 * basically, the algorithm scales well.
 *
 */
public class MPMCQueueStressTest2 {

  private static final int RUNS = 100_000_000;//100_000_000;

//  private MPMCQueue loop = new MPMCQueue(8);
  private MPMCQueue loop = new MPMCQueue(1024*128);

  private final AtomicInteger counter = new AtomicInteger();

  @Test
  public void testMPMCHyperLoop() {
    Affinity.bindTo(
        Affinity.Topology.socket(0).physicalCore(0).virtualCore(0));
//    for (int i = 0; i < 5; i++)
    runStress("#" + 1);
  }

  private void runStress(String label) {
    System.out.println("=====in Round "+label);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch endLatch = new CountDownLatch(7);

    long[] values = new long[RUNS];
    ThreadLocalRandom rnd = ThreadLocalRandom.current();
    IntStream.range(0, RUNS).forEach( i->
        values[i]=rnd.nextLong(0,Long.MAX_VALUE)
    );

    System.out.println("start stress...");

    //
//    IntStream.range(0,7).forEach(i->{
//    });
    spawnConsumer(0,1,loop,values,startLatch,endLatch).start();
    spawnConsumer(1,0,loop,values,startLatch,endLatch).start();
    spawnConsumer(1,1,loop,values,startLatch,endLatch).start();
    spawnConsumer(2,0,loop,values,startLatch,endLatch).start();
    spawnConsumer(2,1,loop,values,startLatch,endLatch).start();
    spawnConsumer(3,0,loop,values,startLatch,endLatch).start();
    spawnConsumer(3,1,loop,values,startLatch,endLatch).start();

    startLatch.countDown();


    try {
      long s = System.nanoTime();
      //XXX: producer is in main thread
      for (int i=0;i<RUNS;i++) {
//        System.out.println(i);
        while (!loop.offer(values[i])){
          Thread.yield();
        }
      }
//      System.out.printf("sent out %d values\n", RUNS);
      endLatch.await();
      long time = System.nanoTime()-s;

      System.out.println(counter.get());
      System.out.printf("MPMCQueueStressTest2 " +
          "done with costed time: %,d\n",time);

    }catch (Exception e){e.printStackTrace();}
  }

  private Thread spawnConsumer(
      int physicalCoreId,
      int virtualCoreId,
      MPMCQueue loop,
      long[] expectedValues,
      CountDownLatch startLatch,
      CountDownLatch endLatch) {
    return
        new Thread(() -> {
          Affinity.bindTo(
              Affinity.Topology.
                  socket(0).
                  physicalCore(physicalCoreId).
                  virtualCore(virtualCoreId));

          uncheck(() -> startLatch.await());
          long v= MPMCQueue.NULL;
          while ((counter.get()<RUNS)) {
            while ( MPMCQueue.NULL ==(v=loop.poll()) &&
              (counter.get()<RUNS) ) {
              Thread.yield();
            }
            if (v!= MPMCQueue.NULL) {
              counter.incrementAndGet();
            }
          }
//          System.out.println(counter.get());
          endLatch.countDown();
        });
  }

}
