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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.junit.AfterClass;
import org.junit.Test;
import z.exception.ContractViolatedException;
import z.offheap.buffer.Buffer;
import z.offheap.buffer.ByteBuffer;
import z.util.Unsafes;

import java.util.concurrent.CountDownLatch;
import java.util.function.BooleanSupplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static z.offheap.zmalloc.Allocator.*;
import static z.offheap.buffer.Buffers.*;

/**
 * Netty ByteBuf: time cost of 50,000,000 times OP in 720,535,569 nanoseconds with sum -2935351363880267904
 */
public class ContrastTestNettyByteBuf {
  public static final int WARMUP_COUNT = 20_000;
  private static final int COUNT = 50_000_000;

  @Test
  public void testNettyByteBuf() throws Exception {
    long s,t, sum=0;

    ByteBuf bn = PooledByteBufAllocator.DEFAULT.directBuffer(16);

    //warm-up
    for (int i = 0; i < WARMUP_COUNT; i++) {
      doWithNettyByteBuf(bn, i);
    }

    //================================netty contrast
    System.gc();
    Thread.sleep(3000L);

    s = System.nanoTime();
    for (int i = 0; i < COUNT; i++) {
      sum += doWithNettyByteBuf(bn, i);
    }
    t = System.nanoTime() - s;
    System.out.printf(
        "Netty ByteBuf: time cost of %,d times OP in %,d nanoseconds with sum %d\n", COUNT,t,sum);
  }


  private long doWithNettyByteBuf(ByteBuf bn, int i) {
    bn.clear()
        .writeByte(i)
        .writeByte(i)
        .writeShort(i)
        .writeInt(i)
        .writeLong(i);

    long rnb1 = bn.readLong();
    long rnb2 = bn.readLong();

    return rnb1+rnb2;
  }

}
