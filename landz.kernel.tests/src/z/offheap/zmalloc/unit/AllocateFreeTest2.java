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
import static z.offheap.zmalloc.AllocatorPrivatesBridge.*;

public class AllocateFreeTest2 {

  @Test
  public void testAllocateFreeSmall() {
    long chunk0 = Allocator.allocate(15);
    long chunk1 = Allocator.allocate(15);
    long chunk2 = Allocator.allocate(15);
    long page = chunk0-(chunk0%SIZE_ZMPAGE);
    assertThat(chunk0-(chunk0%SIZE_ZMPAGE),
        is(chunk2-(chunk2%SIZE_ZMPAGE)));

    assertThat(
        Allocator.ManagedPoolStats.currentNumOfPageAvailableChunks(page),
        is(ZMPAGE_MAX_CHUNK_SIZE()/16-3));

    Allocator.free(chunk0);
    Allocator.free(chunk1);
    Allocator.free(chunk2);

    assertThat(
        Allocator.ManagedPoolStats.currentNumOfPageAvailableChunks(page),
        is(ZMPAGE_MAX_CHUNK_SIZE()/16));

    long chunk3 = Allocator.allocate(12);
    long chunk4 = Allocator.allocate(13);
    long chunk5 = Allocator.allocate(14);

    assertThat(chunk3,is(chunk2));
    assertThat(chunk4,is(chunk1));
    assertThat(chunk5,is(chunk0));
  }

  @Test
  public void testAllocateFree1536k() {
    long chunk0 = Allocator.allocate(1536_000);//->1536k
    long page0 = chunk0-(chunk0%SIZE_ZMPAGE);
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfPageAvailableChunks(page0),is(0));
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(34),//->1536k
        is(0L));

    long chunk1 = Allocator.allocate(1536_000);
    long page1 = chunk1-(chunk1%SIZE_ZMPAGE);
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfPageAvailableChunks(page1),is(0));
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(34),//->1536k
        is(0L));

    Allocator.free(chunk0);
    Allocator.free(chunk1);

    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(34),//->1536k
        is(2L));

    long chunk2 = Allocator.allocate(1536_000);

    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(34),//->1536k
        is(1L));

    long chunk3 = Allocator.allocate(1536_000);

    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(34),//->1536k
        is(0L));

    assertThat(chunk2,is(chunk0));
    assertThat(chunk3,is(chunk1));
  }

}
