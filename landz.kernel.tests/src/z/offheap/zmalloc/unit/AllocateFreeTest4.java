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
import z.util.SystemProperty;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AllocateFreeTest4 {

  @Test
  public void testFreeToGP() {
    SystemProperty.ZMALLOC_FREEPAGES_NUM_TORETURN.setValue("55");

    int COUNT = 10_000_000;
    long[] chunks = new long[COUNT];

//    System.out.println("before test:");
//    System.out.println("AvailableGPages:"+Allocator.ManagedPoolStats.currentNumOfGPAvaiablePages());
    System.out.println("=================");
    System.out.println("start allocate/free 15...");
    for (int i = 0; i < COUNT; i++) {
      chunks[i] = Allocator.allocate(15);//->
    }
    for (int i = 0; i < COUNT; i++) {
      Allocator.free(chunks[i]);
    }

    assertThat(Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(1),
        is(Allocator.ManagedPoolStats.currentNumOfTLPFreePages()));
    assertThat(Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(1),
        greaterThanOrEqualTo(65L-Integer.parseInt(SystemProperty.ZMALLOC_FREEPAGES_NUM_TORETURN.value())));
    assertThat(Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(1),
        lessThan(64L));

    System.out.println("=========================================");
    COUNT = 490;
    chunks = new long[COUNT];
    System.out.println("start allocate/free 2000_000...");
//    System.out.println("run#" + 0);
//    System.out.println("AvailableGPages:" +
//        Allocator.ManagedPoolStats.currentNumOfGPAvaiablePages());
//    System.out.println("TLP availablePages of #1:" +
//        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(1));
//    System.out.println("TLP freePages:" +
//        Allocator.ManagedPoolStats.currentNumOfTLPFreePages());

    for (int i = 0; i < COUNT; i++) {
      chunks[i] = Allocator.allocate(1500_000);//->~2M
    }
    for (int i = 0; i < COUNT; i++) {
      Allocator.free(chunks[i]);
    }

    assertThat(Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(1),
        is(65L - Long.parseLong(SystemProperty.ZMALLOC_FREEPAGES_NUM_TORETURN.value())));
    assertThat(Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(1)
        +Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(34),
        is(Allocator.ManagedPoolStats.currentNumOfTLPFreePages()));

    assertThat(Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(34),
        lessThan(Long.parseLong(SystemProperty.ZMALLOC_FREEPAGES_NUM_TORETURN.value())));
  }

}
