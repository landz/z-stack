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

import java.util.concurrent.TimeUnit;

import static z.offheap.zmalloc.AllocatorPrivatesBridge.sizeClassIndex;

/**
 * note: this test is abandoned in that the Allocator#sizeClassIndex has been
 * hidden.
 *
 * the MH based version is 100x slower than that of direct calling
 */
public class SizeClassIndexPerfTest {

  private static final long COUNT = 1000;//500_000_000
  private static final int LIMIT_MASK = (1<<21)-1;

  @Test
  /**
   * lookup table way is,
   * 1/2 faster with preloading
   * 1/3 faster without preloading
   */
  public void testSizeTypeIndexPerf() {
//    Intrinsics.warmup();
//    Allocator.sizeClassIndex(123);

    int sum;
    long s,t;

    sum=0;
    s = System.nanoTime();
    for (int i = 0; i < COUNT; i++) {
      sum += sizeClassIndex(i & LIMIT_MASK);
    }
    t = System.nanoTime()-s;
    System.out.println("sum is: "+sum);
    System.out.println("time cost(nanos): "+t);
    System.out.println("time cost(millis): "+
        TimeUnit.NANOSECONDS.toMillis(t));
    System.out.println("or "+(1.0*COUNT)/t*1e9+" OPs/sec");
  }

}
