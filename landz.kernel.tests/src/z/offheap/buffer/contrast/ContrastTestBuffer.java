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
import z.offheap.buffer.Buffer;
import z.offheap.buffer.ByteBuffer;

/**
 * disable contracts:
 * Landz Buffer: time cost of 50,000,000 times OP in 521,461,509 nanoseconds with sum -2935351363880267904
 * enable contracts:
 * Landz Buffer: time cost of 50,000,000 times OP in 726,165,205 nanoseconds with sum -2935351363880267904
 */
public class ContrastTestBuffer {
  public static final int WARMUP_COUNT = 20_000;
  private static final int COUNT = 50_000_000;

  @Test
  public void testBuffer() throws Exception {
    long s,t, sum=0;

    Buffer.disableContracts();

    ByteBuffer bz = Buffer.create(16);

    //warm-up
    for (int i = 0; i < WARMUP_COUNT; i++) {
      doWithBuffer(bz, i);
    }

    //================================Buffer
    System.gc();
    Thread.sleep(3000L);

    s = System.nanoTime();
    for (int i = 0; i < COUNT; i++) {
      sum+=doWithBuffer(bz, i);
    }
    t = System.nanoTime() - s;
    System.out.printf(
        "Landz Buffer: time cost of %,d times OP in %,d nanoseconds with sum %d\n", COUNT,t,sum);

    bz.close();
  }

  private long doWithBuffer(ByteBuffer bz, int i) {
    bz.reset()
        .networkOrder()
        .write((byte) i)
        .write((byte) i)
        .writeShortN((short) i)
        .writeIntN(i)
        .writeLongN(i);

    long rbz1 = bz.networkOrder().readLongN();
    long rbz2 = bz.networkOrder().readLongN();
    return rbz1+rbz2;
  }


}
