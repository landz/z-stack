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
import z.channel.GenericMPMCQueue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static z.util.Throwables.uncheck;

/**
 * spawn 8 threads to test but without affinity pinning
 *
 */
public class GenericMPMCQueueStressTest2NoAffinity {

  private static final int RUNS = 20_000_000;//100_000_000;

  private GenericMPMCQueue<String> loop = new GenericMPMCQueue(1024);

  private final AtomicInteger counter = new AtomicInteger();

  @Test
  public void testMPMCHyperLoop() {
//    for (int i = 0; i < 5; i++)
    runStress("#" + 1);
  }

  private void runStress(String label) {
    System.out.println("=====in Round "+label);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch endLatch = new CountDownLatch(7);

    long s = System.currentTimeMillis();
    String[] values = new String[RUNS];
    ThreadLocalRandom rnd = ThreadLocalRandom.current();
    IntStream.range(0, RUNS).forEach( i->
        values[i]=String.valueOf(rnd.nextLong(0, Long.MAX_VALUE))
    );
    long time = System.currentTimeMillis()-s;
    System.out.printf(
        "after %d seconds initialization,start stress...",time/1000L);

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
      s = System.nanoTime();
      //XXX: producer is in main thread
      for (int i=0;i<RUNS;i++) {
//        System.out.println(i);
        while (!loop.offer(values[i])){
          Thread.yield();
        }
      }
//      System.out.printf("sent out %d values\n", RUNS);
      endLatch.await();
      time = System.nanoTime()-s;

      System.out.println(counter.get());
      System.out.printf("GenericMPMCQueueStressTest2NoAffinity " +
          "done with costed time: %,d\n",time);

    }catch (Exception e){e.printStackTrace();}
  }

  private Thread spawnConsumer(
      int physicalCoreId,
      int virtualCoreId,
      GenericMPMCQueue<String> loop,
      String[] expectedValues,
      CountDownLatch startLatch,
      CountDownLatch endLatch) {
    return
        new Thread(() -> {

          uncheck(() -> startLatch.await());
          String v;
          while ((counter.get()<RUNS)) {
            while ( null ==(v=loop.poll()) &&
              (counter.get()<RUNS) ) {
              Thread.yield();
            }
            if (v!= null) {
              counter.incrementAndGet();
            }
          }
//          System.out.println(counter.get());
          endLatch.countDown();
        });
  }

}
