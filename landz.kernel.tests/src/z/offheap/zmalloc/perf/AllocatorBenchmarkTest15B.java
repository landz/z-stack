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

import java.util.concurrent.TimeUnit;

/**
 *
 jemalloc:
 10000000 allocations in 269ms (5241878/s)
 10000000 deallocations in 324ms (4352053/s)
 10000000 allocations in 262ms (5381929/s)
 10000000 deallocations in 324ms (4352053/s)
 10000000 allocations in 265ms (5321001/s)
 10000000 deallocations in 324ms (4352053/s)

 glibc(ptmalloc):
 10000000 allocations in 282ms (5000231/s)
 10000000 deallocations in 118ms (11949706/s)
 10000000 allocations in 143ms (9860597/s)
 10000000 deallocations in 120ms (11750545/s)
 10000000 allocations in 143ms (9860597/s)
 10000000 deallocations in 118ms (11949706/s)

 zmalloc:
 ====size 15B====RUN#0
 ZMalloc:
 Allocator[size:15] allocate 10000000 chunks cost: 203 millis
 Allocator[size:15] free 10000000 chunks cost: 48 millis
 ====size 15B====RUN#1
 ZMalloc:
 Allocator[size:15] allocate 10000000 chunks cost: 120 millis
 Allocator[size:15] free 10000000 chunks cost: 34 millis
 ====size 15B====RUN#2
 ZMalloc:
 Allocator[size:15] allocate 10000000 chunks cost: 105 millis
 Allocator[size:15] free 10000000 chunks cost: 43 millis
 ====size 15B====RUN#3
 ZMalloc:
 Allocator[size:15] allocate 10000000 chunks cost: 102 millis
 Allocator[size:15] free 10000000 chunks cost: 43 millis
 */
public class AllocatorBenchmarkTest15B {

  private static final int COUNT1 = 10_000_000;

  @Test
  public void timeAllocAndThenFree() {
//    Intrinsics.warmup();

    for (int i = 0; i < 5; i++) {
      System.out.println("====size 15B====RUN#"+i);
      forZMalloc(15, COUNT1);
    }

  }


  public void forZMalloc(int size, int count) {
    long[] chunks = new long[count];
    System.out.println("ZMalloc: ");

    long s =System.nanoTime();
    for (int i = 0; i < count; i++) {
      chunks[i] = (Allocator.allocate(size));
    }
    long t = System.nanoTime() - s;
    System.out.println(
        Allocator.class.getSimpleName()+"[size:"+size+"]"+
            " allocate "+ count +" chunks cost: "+
            TimeUnit.NANOSECONDS.toMillis(t)+" millis");

    s =System.nanoTime();
    for (int i = 0; i < count; i++) {
      Allocator.free(chunks[i]);
    }
    t = System.nanoTime() - s;
    System.out.println(
        Allocator.class.getSimpleName()+"[size:"+size+"]"+
            " free " + count + " chunks cost: " +
            TimeUnit.NANOSECONDS.toMillis(t) + " millis");

  }

}
