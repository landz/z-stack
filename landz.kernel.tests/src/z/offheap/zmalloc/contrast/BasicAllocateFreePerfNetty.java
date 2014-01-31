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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import z.testware.benchmark.BenchmarkOptions;
import z.testware.benchmark.BenchmarkRule;

import static z.util.Unsafes.UNSAFE;

public class BasicAllocateFreePerfNetty {

  private static final ByteBufAllocator NETTY_POOLED_ALLOCATOR_DIRECT
      = new PooledByteBufAllocator(true);

  public static final int COUNT = 10_000_000;

  public static final int SIZE_64 = 64;
  public static final int SIZE_1k = 1024;
  public static final int SIZE_128k = 128 * 1024;

  @Rule
  public TestRule benchmarkRun = new BenchmarkRule();


  @Test
  @BenchmarkOptions(benchmarkRounds = 2, warmupRounds = 2)
  public void allocateAndFreeMemoryCost64() {
    for (int i = 0; i < COUNT; i++) {
      ByteBuf buffer = NETTY_POOLED_ALLOCATOR_DIRECT.buffer(SIZE_64);
      buffer.release();
    }
  }

  @Test
  @BenchmarkOptions(benchmarkRounds = 2, warmupRounds = 2)
  public void allocateAndFreeMemoryCost1024() {
    for (int i = 0; i < COUNT; i++) {
      ByteBuf buffer = NETTY_POOLED_ALLOCATOR_DIRECT.buffer(SIZE_1k);
      buffer.release();
    }
  }


  @Test
  @BenchmarkOptions(benchmarkRounds = 2, warmupRounds = 2)
  public void allocateAndFreeMemoryCost128k() {
    for (int i = 0; i < COUNT; i++) {
      ByteBuf buffer = NETTY_POOLED_ALLOCATOR_DIRECT.buffer(SIZE_128k);
      buffer.release();
    }
  }




}
