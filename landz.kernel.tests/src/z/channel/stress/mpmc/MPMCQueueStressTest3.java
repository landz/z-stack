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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static z.util.Throwables.uncheck;

/**
 * spawn 7 consumer threads/7 producer threads
 */
public class MPMCQueueStressTest3 {

  private static final int RUNS = 20_000_000;
  private static final int TOTAL_RUNS = RUNS*7;

//  private MPMCQueue loop = new MPMCQueue(8);
  private MPMCQueue loop = new MPMCQueue(1024*128);

  private AtomicInteger counter;

  @Test
  public void testMPMCHyperLoop() {
    Affinity.bindTo(
        Affinity.Topology.socket(0).physicalCore(0).virtualCore(0));
    for (int i = 0; i < 5; i++) {
      System.gc();//?
      counter =  new AtomicInteger();
      runStress("#" + i);
    }
  }

  private void runStress(String label) {
    System.out.println("=====in Round "+label);
    int numRunnings0 = numOfRunningThreads();

    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch endLatch = new CountDownLatch(7*2);

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
    spawnProducer(0,1,loop,values,startLatch,endLatch).start();
    
    spawnConsumer(1,0,loop,values,startLatch,endLatch).start();
    spawnProducer(1,0,loop,values,startLatch,endLatch).start();

    spawnConsumer(1,1,loop,values,startLatch,endLatch).start();
    spawnProducer(1,1,loop,values,startLatch,endLatch).start();
    
    spawnConsumer(2,0,loop,values,startLatch,endLatch).start();
    spawnProducer(2, 0, loop, values, startLatch, endLatch).start();

    spawnConsumer(2,1,loop,values,startLatch,endLatch).start();
    spawnProducer(2, 1, loop, values, startLatch, endLatch).start();

    spawnConsumer(3,0,loop,values,startLatch,endLatch).start();
    spawnProducer(3, 0, loop, values, startLatch, endLatch).start();

    spawnConsumer(3,1,loop,values,startLatch,endLatch).start();
    spawnProducer(3, 1, loop, values, startLatch, endLatch).start();


    uncheck(() ->
        Thread.sleep(1000));

    startLatch.countDown();
    long s = System.nanoTime();

    uncheck(() ->
        endLatch.await());
    long time = System.nanoTime()-s;

    System.out.println(counter.get());
    System.out.printf("MPMCQueueStressTest2 " +
        "done with costed time: %,d\n",time);

    //for safety, we wait 1s for the termination of all consumers .
    uncheck(()->Thread.sleep(1000L));
    assertThat(numOfRunningThreads(), is(numRunnings0));
  }

  private static final int numOfRunningThreads() {
    return (int)
        Thread.getAllStackTraces().keySet().stream()
            .filter(t -> t.getState() == Thread.State.RUNNABLE)
            .count();
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
          while ((counter.get()<TOTAL_RUNS)) {
            while ( MPMCQueue.NULL ==(v=loop.poll()) &&
              (counter.get()<TOTAL_RUNS) ) {
//              Thread.yield();
            }
            if (v!= MPMCQueue.NULL) {
              counter.incrementAndGet();
            }
          }
//          System.out.println(counter.get());
          endLatch.countDown();
        });
  }

  private Thread spawnProducer(
      int physicalCoreId,
      int virtualCoreId,
      MPMCQueue loop,
      long[] values,
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
          for (int i=0;i<RUNS;i++) {
//        System.out.println(i);
            while (!loop.offer(values[i])){
//              Thread.yield();
            }
          }
//          System.out.println(counter.get());
          endLatch.countDown();
        });
  }

}
