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

import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static z.offheap.zmalloc.Allocator.*;

/**
 */
public class RandomAllocateFreeTest {

  @Test
  public void smallSizeAllocationTest() {
    ThreadLocalRandom rnd = ThreadLocalRandom.current();

    long RUNS = 2000L;
    for (long i = 0; i < RUNS; i++) {
//      int length = rnd.nextInt(1,100);
      int length = 19;
      long addr = allocate(length);
      assertThat(addr, greaterThan(1000_000_000L));
      free(addr);
    }
  }


}
