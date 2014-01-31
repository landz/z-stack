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

package z.offheap.zmalloc.contrast;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import org.junit.Test;
import sun.misc.VM;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class AllocatorBenchmarkTestNetty15B {

  private static final int COUNT1 = 10_000_000;

  private static final ByteBufAllocator NETTY_POOLED_ALLOCATOR_DIRECT
      = new PooledByteBufAllocator(true);

  //XXX: netty use Deque in its test
  private final Deque<ByteBuf> queue = new ArrayDeque<ByteBuf>();

  @Test
  public void timeAllocAndThenFree() {
    System.setProperty("io.netty.noResourceLeakDetection","true");

    System.out.println("make sure your maxDirectMemory >= 1GB to run this test:");
    System.out.println("current maxDirectMemory: "+ VM.maxDirectMemory());
    System.out.println("========================================");

    for (int i = 0; i < 5; i++) {
      System.out.println("====size 15B====RUN#" + i);
      forNetty(15, COUNT1);
    }

  }

  public void forNetty(int size, int count) {
    ByteBuf[] bufs = new ByteBuf[count];
    System.out.println(NETTY_POOLED_ALLOCATOR_DIRECT.getClass().getSimpleName()+": ");

    long s =System.nanoTime();
    for (int i = 0; i < count; i++) {
      bufs[i] = NETTY_POOLED_ALLOCATOR_DIRECT.buffer(size);
    }
    long t = System.nanoTime() - s;
    System.out.println(
        NETTY_POOLED_ALLOCATOR_DIRECT.getClass().getSimpleName()+"[size:"+size+"]"+
        " allocate "+ count +" buffers cost: "+
        TimeUnit.NANOSECONDS.toMillis(t)+" millis");


    s =System.nanoTime();
    for (int i = 0; i < count; i++) {
      bufs[i].release();
    }
    t = System.nanoTime() - s;
    System.out.println(
        NETTY_POOLED_ALLOCATOR_DIRECT.getClass().getSimpleName()+"[size:"+size+"]"+
            " free " + count + " buffers cost: " +
            TimeUnit.NANOSECONDS.toMillis(t) + " millis");

    assertThat(bufs[0].isReadable(), is(false));
  }

}
