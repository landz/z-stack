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

package z.offheap.zmalloc.unit;

import org.junit.Test;
import z.offheap.zmalloc.Allocator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static z.offheap.zmalloc.AllocatorPrivatesBridge.SIZE_ZMPAGE;

public class AllocateFreeTest3 {

  @Test
  public void testFreeToGP() {
    int COUNT = 65;
    long[] chunks = new long[COUNT];

    for (int i = 0; i < COUNT; i++) {
      chunks[i] = Allocator.allocate(1536_000);//->1536k
//      long page1 = chunk1-(chunk1%SIZE_ZMPAGE);
    }

    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(34),//->1536k
        is(0L));

    for (int i = 0; i < COUNT-1; i++) {
      Allocator.free(chunks[i]);
    }

    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(34),//->1536k
        is(64L));
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(34),//->1536k
        is(64L));

    Allocator.free(chunks[COUNT-1]);
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(34),//->1536k
        is(33L));
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(34),//->1536k
        is(33L));


  }

}
