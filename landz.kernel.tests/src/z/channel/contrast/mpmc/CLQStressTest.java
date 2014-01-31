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

package z.channel.contrast.mpmc;

import org.junit.Test;
import z.znr.Affinity;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static z.util.Throwables.uncheck;

/**
 * 60000000
 * MPMCQueue done with costed time: 7,657,119,575
 * CLQStressTest done with costed time: 46,391,598,873
 *
 */
public class CLQStressTest {

  private static final int RUNS = 60_000_000;//100_000_000;

  private ConcurrentLinkedQueue<Long> queue = new ConcurrentLinkedQueue();

  private final AtomicInteger counter = new AtomicInteger();

  @Test
  public void testCLQ() {
    Affinity.bindTo(
        Affinity.Topology.socket(0).physicalCore(0).virtualCore(0));
//    for (int i = 0; i < 5; i++)
    runStress("#" + 1);
  }

  private void runStress(String label) {
    System.out.println("=====in Round "+label);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch endLatch = new CountDownLatch(7);

    Long[] values = new Long[RUNS];
    ThreadLocalRandom rnd = ThreadLocalRandom.current();
    IntStream.range(0, RUNS).forEach( i->
        values[i]=rnd.nextLong(0,Long.MAX_VALUE)
    );

    System.out.println("start stress...");

    //
//    IntStream.range(0,7).forEach(i->{
//    });
    spawnConsumer(0,1,queue,startLatch,endLatch).start();
    spawnConsumer(1,0,queue,startLatch,endLatch).start();
    spawnConsumer(1,1,queue,startLatch,endLatch).start();
    spawnConsumer(2,0,queue,startLatch,endLatch).start();
    spawnConsumer(2,1,queue,startLatch,endLatch).start();
    spawnConsumer(3,0,queue,startLatch,endLatch).start();
    spawnConsumer(3,1,queue,startLatch,endLatch).start();

    startLatch.countDown();


    try {
      long s = System.nanoTime();
      //XXX: producer is in main thread
      for (int i=0;i<RUNS;i++) {
//        System.out.println(i);
        while (!queue.offer(values[i])){
//          Unsafes.UNSAFE.park(false, 200);
        }
      }
//      System.out.printf("sent out %d values\n", RUNS);
      endLatch.await();
      long time = System.nanoTime()-s;

      System.out.println(counter.get());
      System.out.printf("CLQStressTest " +
          "done with costed time: %,d\n",time);

    }catch (Exception e){e.printStackTrace();}
  }

  private Thread spawnConsumer(
      int physicalCoreId,
      int virtualCoreId,
      ConcurrentLinkedQueue<Long> queue,
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
          Long v=null;
          while ((counter.get()<RUNS)) {
            while ( null==(v=queue.poll()) &&
              (counter.get()<RUNS) ) {
//              Unsafes.UNSAFE.park(false, 50);//Thread.yield();//
            }
            if (v!=null) {
              counter.incrementAndGet();
            }
          }
//          System.out.println(counter.get());
          endLatch.countDown();
        });
  }

}
