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

package z.offheap.contrast.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ByteBufAllocatorBenchmark {

  public static final int COUNT = 500_000;

  //XXX: too slow to disable
  private static final ByteBufAllocator UNPOOLED_ALLOCATOR_DIRECT = new UnpooledByteBufAllocator(true);

  private static final ByteBufAllocator POOLED_ALLOCATOR_DIRECT = new PooledByteBufAllocator(true);

  private static final int[] sizes = {0, 256, 1024, 4096, 65536, 256*1024, 1024*1024};
  private static final ByteBufAllocator[] allocators = {POOLED_ALLOCATOR_DIRECT};// UNPOOLED_ALLOCATOR_DIRECT};

  //XXX: netty use Deque in its test
  private final Deque<ByteBuf> queue = new ArrayDeque<ByteBuf>();

  @Test
  /**
   * because slab is cached, the single allocate and free is constant.
   *
   * this test reflects the instrinic/of best-time allocation latency.
   *
   * alloc+free rate ~= 5M to 9M alloc+free/s
   *
   */
  public void timeAllocAndFree() {
    for (int i = 0; i < allocators.length; i++) {
      ByteBufAllocator allocator = allocators[i];
      for (int j = 0; j < sizes.length; j++) {
        int size = sizes[j];
        forSize(allocator, size);
      }
    }
  }

  public void forSize(ByteBufAllocator alloc , int size) {
    long s =System.nanoTime();
    for (int i = 0; i < COUNT; i++) {
      queue.add(alloc.buffer(size));
      queue.removeFirst().release();
    }
    long t = System.nanoTime() - s;
    System.out.println("for size "+size+" and "+
        alloc.getClass().getSimpleName()+" cost: "+
        TimeUnit.NANOSECONDS.toMillis(t)+" millis");

    assertThat(queue.size(), is(0));
  }


  @Test
  /**
   * here,
   *   it simply test the dynamic charactiscs of one allocator in a high
   *   batch throughout
   *
   *   alloc rate ~ 1M alloc/s
   *   dealloc rate ~ 2.7M alloc/s
   */
  public void timeAllocAndThenFree() {
    for (int i = 0; i < allocators.length; i++) {
      ByteBufAllocator allocator = allocators[i];
      forAllocator(allocator, 1024);
    }
  }

  private final ByteBuf[] bufs = new ByteBuf[COUNT];
  public void forAllocator(ByteBufAllocator alloc, int size) {
    System.out.println(alloc.getClass().getSimpleName()+": ");

    long s =System.nanoTime();
    for (int i = 0; i < COUNT; i++) {
      bufs[i] = (alloc.buffer(size));
    }
    long t = System.nanoTime() - s;
    System.out.println(
        alloc.getClass().getSimpleName()+"[size:"+size+"]"+
        " allocate "+ COUNT +" buffers cost: "+
        TimeUnit.NANOSECONDS.toMillis(t)+" millis");


    s =System.nanoTime();
    for (int i = 0; i < COUNT; i++) {
      bufs[i].release();
    }
    t = System.nanoTime() - s;
    System.out.println(
        alloc.getClass().getSimpleName()+"[size:"+size+"]"+
            " free " + COUNT + " buffers cost: " +
            TimeUnit.NANOSECONDS.toMillis(t) + " millis");

    assertThat(bufs[0].isReadable(), is(false));
  }

}
