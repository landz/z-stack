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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import z.offheap.zmalloc.Allocator;
import z.testware.benchmark.BenchmarkOptions;
import z.testware.benchmark.BenchmarkRule;

/**
 * Created by jin on 8/28/13.
 */
public class BasicAllocateFreePerf {

  public static final int COUNT = 100_000_000;

  public static final int SIZE_64 = 64;
  public static final int SIZE_1k = 1024;
  public static final int SIZE_128k = 128 * 1024;

  @Rule
  public TestRule benchmarkRun = new BenchmarkRule();


  @Test
  @BenchmarkOptions(benchmarkRounds = 2, warmupRounds = 2)
  public void allocateAndFreeMemoryCost64() {
    for (int i = 0; i < COUNT; i++) {
      long address = Allocator.allocate(SIZE_64);
      Allocator.free(address);
    }
  }

  @Test
  @BenchmarkOptions(benchmarkRounds = 2, warmupRounds = 2)
  public void allocateAndFreeMemoryCost1024() {
    for (int i = 0; i < COUNT; i++) {
      long address = Allocator.allocate(SIZE_1k);
      Allocator.free(address);
    }
  }


  @Test
  @BenchmarkOptions(benchmarkRounds = 2, warmupRounds = 2)
  public void allocateAndFreeMemoryCost128k() {
    for (int i = 0; i < COUNT; i++) {
      long address = Allocator.allocate(SIZE_128k);
      Allocator.free(address);
    }
  }




}
