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
import z.offheap.zmalloc.Allocator;

import java.util.concurrent.TimeUnit;

public class AllocatorBenchmarkMixedTest {

  private static final int COUNT1 = 10_000_000;
  private static final int COUNT2 =    900_000;
  private static final int COUNT3 =     9_000;

  @Test
  public void timeAllocAndThenFree() {
//    SystemProperty.ZMALLOC_FREEPAGES_NUM_TORETURN.setValue("55");

    int RUNS = 5;

    for (int i = 0; i < RUNS; i++) {
      System.out.println("====size 15B====RUN#" + i);
      forZMalloc(15, COUNT1);
    }

    for (int i = 0; i < RUNS; i++) {
      System.out.println("====size 1000B====RUN#" + i);
      forZMalloc(1000,COUNT2);
    }

    for (int i = 0; i < RUNS; i++) {
      System.out.println("====size 96000B====RUN#"+i);
      forZMalloc(96000,COUNT3);
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
