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

package z.offheap.zmalloc.stress;

import org.junit.Test;
import z.channel.MPMCQueue;
import z.offheap.zmalloc.Allocator;
import z.util.Unsafes;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static z.util.Throwables.uncheck;

/**
 * in this test, we run multiple times in the same different threads
 */
public class AllocatorBenchmarkTest15BMTStress {

  private static final int COUNT1 = 10_000_000;//costs 77*2M zmpage
  private static MPMCQueue msgQueueA = new MPMCQueue(4);
  private static MPMCQueue msgQueueB = new MPMCQueue(4);
  private static final int TERMINATION = -1;
  private static final int RUNS = 5;

  @Test
  public void timeAllocAndThenFree() {
    forZMalloc(15, COUNT1);
  }


  public void forZMalloc(int size, int count) {
    CountDownLatch endLatch = new CountDownLatch(2);
    long[] chunks = new long[count];

    printStats("before running test");

    new Thread(() -> {
      int ct=0;
      for(;;) {
        long v;
        while (MPMCQueue.NULL==(v=msgQueueB.poll())) {
          Unsafes.UNSAFE.park(false,1000L);
        }
        if (v==TERMINATION) {
          break;
        }

        long s = System.nanoTime();
        for (int i = 0; i < count; i++) {
          chunks[i] = (Allocator.allocate(size));
        }
        long t = System.nanoTime() - s;
        System.out.println("\n[size:" + size + "]" +
            " allocate " + count + " chunks cost: " +
            TimeUnit.NANOSECONDS.toMillis(t) + " millis");
        printStats("in thread A");
        msgQueueA.offer((ct++)==RUNS?TERMINATION:ct);
      }

      endLatch.countDown();
    }).start();

    new Thread(() -> {
      msgQueueB.offer(0);//start the test
      for (; ; ) {
        long v;
        while (MPMCQueue.NULL == (v = msgQueueA.poll())) {
          Unsafes.UNSAFE.park(false, 1000L);
        }
        if (v == TERMINATION) {
          msgQueueB.offer(TERMINATION);
          break;
        }

        long s = System.nanoTime();
        for (int i = 0; i < count; i++) {
          Allocator.free(chunks[i]);
        }
        long t = System.nanoTime() - s;
        System.out.println("\n[size:" + size + "]" +
            " free " + count + " chunks cost: " +
            TimeUnit.NANOSECONDS.toMillis(t) + " millis");
        printStats("in thread B");
        msgQueueB.offer(0);
      }


      endLatch.countDown();
    }).start();

    uncheck(() -> endLatch.await());
    System.out.println("all done!");
  }

  private static void printStats(String label) {
    System.out.println("==================stats==="+label);
    System.out.println(" gp AP:"+
        Allocator.ManagedPoolStats.currentNumOfGPAvaiablePages());
    System.out.println(" num of tlps:"+
        Allocator.ManagedPoolStats.currentNumOfTLPs());
    System.out.println(" tlp freePages:"+
        Allocator.ManagedPoolStats.currentNumOfTLPFreePages());
    System.out.println(" tlp AP:"+
        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(1));//#1 for 16B
    System.out.println("==================stats");
  }

}
