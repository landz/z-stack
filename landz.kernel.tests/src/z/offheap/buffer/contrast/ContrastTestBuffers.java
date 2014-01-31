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

package z.offheap.buffer.contrast;

import org.junit.Test;

import static z.offheap.buffer.Buffers.*;
import static z.offheap.zmalloc.Allocator.allocate;
import static z.offheap.zmalloc.Allocator.free;

/**
 *
 * Landz Buffers: time cost of 50,000,000 times OP in 378,192,142 nanoseconds with sum -2935351363880267904
 *
 *
 */
public class ContrastTestBuffers {
  public static final int WARMUP_COUNT = 20_000;
  private static final int COUNT = 50_000_000;

  @Test
  public void testBuffers() throws Exception {
    long s,t, sum=0;

    long address = allocate(16);

    //warm-up
    for (int i = 0; i < WARMUP_COUNT; i++) {
      doWithBuffers(address, i);
    }

    //================================Buffers
    System.gc();
    Thread.sleep(3000L);

    s = System.nanoTime();
    for (int i = 0; i < COUNT; i++) {
      sum+=doWithBuffers(address, i);
    }
    t = System.nanoTime() - s;
    System.out.printf(
        "Landz Buffers: time cost of %,d times OP in %,d nanoseconds with sum %d\n", COUNT,t,sum);

    free(address);
  }


  private long doWithBuffers(long address, int i) {
    put(address, (byte) i);
    put(address + 1, (byte) i);
    putShortNonNative(address + 2, (short) i);
    putIntNonNative(address + 4, i);
    putLongNonNative(address + 8, i);

    long rbs1 = getLongNonNative(address);
    long rbs2 = getLongNonNative(address + 8);
    return rbs1+rbs2;
  }


}
