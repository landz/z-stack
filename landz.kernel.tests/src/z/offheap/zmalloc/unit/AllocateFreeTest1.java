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

public class AllocateFreeTest1 {

  @Test
  public void testFree1() {
    long[] addresses = new long[6];
    for (int i = 0; i < 6; i++) {
      addresses[i]  = Allocator.allocate(512*1024);
    }

    long TOTAL_AVAILABLEPAGES = totalAvailablepages();

    assertThat(
        Allocator.ManagedPoolStats.currentNumOfGPAvaiablePages(),
        is(TOTAL_AVAILABLEPAGES-2));
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(31),//->512k
        is(0L));
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPFreePages(),
        is(0L));

    Allocator.free(addresses[0]);
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(31),
        is(1L));
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPFreePages(),
        is(0L));

    Allocator.free(addresses[1]);
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(31),
        is(1L));
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPFreePages(),
        is(0L));

    Allocator.free(addresses[2]);
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(31),
        is(1L));//note: freePage is "virtual"
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPFreePages(),
        is(1L));

    Allocator.free(addresses[3]);
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(31),
        is(2L));
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPFreePages(),
        is(1L));

    Allocator.free(addresses[4]);
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(31),
        is(2L));
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPFreePages(),
        is(1L));

    Allocator.free(addresses[5]);
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(31),
        is(2L));
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPFreePages(),
        is(2L));//all availablePages are freePages

  }


  @Test
  public void testFree2() {
    long[] addresses31 = new long[3];
    for (int i = 0; i < 3; i++) {
      addresses31[i]  = Allocator.allocate(512*1024);
    }

    long[] addresses32 = new long[3];
    for (int i = 0; i < 3; i++) {
      addresses32[i]  = Allocator.allocate(666*1024);//786432
    }

    long TOTAL_AVAILABLEPAGES = totalAvailablepages();

    assertThat(
        Allocator.ManagedPoolStats.currentNumOfGPAvaiablePages(),
        is(TOTAL_AVAILABLEPAGES-3));
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(31),
        is(0L));
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(32),
        is(1L));
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPFreePages(),
        is(0L));


    Allocator.free(addresses31[0]);
    Allocator.free(addresses32[0]);
    Allocator.free(addresses31[1]);
    Allocator.free(addresses32[1]);
    Allocator.free(addresses31[2]);
    Allocator.free(addresses32[2]);

    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(31),
        is(1L));
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(32),
        is(2L));
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPFreePages(),
        is(3L));//all availablePages are freePages

  }

}
