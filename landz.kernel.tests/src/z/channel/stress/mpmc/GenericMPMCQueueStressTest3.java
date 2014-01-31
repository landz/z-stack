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
public class GenericMPMCQueueStressTest3 {

  private static final int RUNS = 5_000_000;
  private static final int TOTAL_RUNS = RUNS*7;

//  private GenericMPMCQueue<String> loop = new GenericMPMCQueue(8);
  private GenericMPMCQueue<String> loop = new GenericMPMCQueue(1024*16);

  private AtomicInteger counter;

  @Test
  public void testGenericMPMCHyperLoop() {
    Affinity.bindTo(
        Affinity.Topology.socket(0).physicalCore(0).virtualCore(0));
    for (int i = 0; i < 5; i++) {
      System.gc();
      counter =  new AtomicInteger();
      runStress("#" + i);
    }
  }

  private void runStress(String label) {
    System.out.println("=====in Round "+label);
    int numRunnings0 = numOfRunningThreads();

    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch endLatch = new CountDownLatch(7*2);

    String[] values = new String[RUNS];
    ThreadLocalRandom rnd = ThreadLocalRandom.current();
    IntStream.range(0, RUNS).forEach( i->
        values[i]=String.valueOf(rnd.nextLong(0, Long.MAX_VALUE))
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
    System.out.printf("GenericMPMCQueueStressTest3 " +
        "done with costed time: %,d\n",time);

    //for safety, we wait 1s for the termination of all consumers .
    uncheck(() -> Thread.sleep(3000L));
    System.out.printf("this test start with %d threads, and end with %d threads.\n",
        numRunnings0, numOfRunningThreads());
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
      GenericMPMCQueue<String> loop,
      String[] expectedValues,
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

          String v= null;
          while ((counter.get()<TOTAL_RUNS)) {
            while ( null ==(v=loop.poll()) &&
              (counter.get()<TOTAL_RUNS) ) {
//              Thread.yield();
            }
            if (v!= null) {
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
      GenericMPMCQueue<String> loop,
      String[] values,
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
