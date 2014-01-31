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

package z.offheap.zmalloc.perf;

import org.junit.Test;
import z.offheap.zmalloc.Allocator;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static z.util.Throwables.uncheck;

/**
 *
 * jemalloc:
 * 10000000 allocations in 248ms (5685747/s)
 * 10000000 deallocations in 374ms (3770228/s)
 *
 * glibc:
 * 10000000 allocations in 325ms (4338662/s)
 * 10000000 deallocations in 190ms (7421396/s)
 *
 * [size:15] allocate 10000000 chunks cost: 145 millis
 * [size:15] free 10000000 chunks cost: 173 millis
 */
public class AllocatorBenchmarkTest15BMT {

  private static final int COUNT1 = 10_000_000;//costs 77*2M zmpage

  @Test
  public void timeAllocAndThenFree() {
    int RUNS = 1;
    for (int i = 0; i < RUNS; i++) {
      System.out.println("\n====size 15B====RUN#" + i);
      forZMalloc(15, COUNT1);
    }

  }


  public void forZMalloc(int size, int count) {
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch endLatch = new CountDownLatch(1);
    long[] chunks = new long[count];
    System.out.println("before running test: ");
    printStats();

    new Thread(() -> {
      long s = System.nanoTime();
      for (int i = 0; i < count; i++) {
        chunks[i] = (Allocator.allocate(size));
      }
      long t = System.nanoTime() - s;
      System.out.println("\n[size:" + size + "]" +
              " allocate " + count + " chunks cost: " +
              TimeUnit.NANOSECONDS.toMillis(t) + " millis");

      printStats();
      uncheck(() -> Thread.sleep(1000L));
      startLatch.countDown();
    }).start();

    new Thread(() -> {
      uncheck(()->startLatch.await());

      long s = System.nanoTime();
      for (int i = 0; i < count; i++) {
        Allocator.free(chunks[i]);
      }
      long t = System.nanoTime() - s;
      System.out.println("\n[size:" + size + "]" +
              " free " + count + " chunks cost: " +
              TimeUnit.NANOSECONDS.toMillis(t) + " millis");

      printStats();
      endLatch.countDown();
    }).start();

    uncheck(() -> endLatch.await());
  }

  private static void printStats() {
    System.out.println("==================stats");
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
