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

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import z.offheap.zmalloc.Allocator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static z.offheap.zmalloc.AllocatorPrivatesBridge.*;

public class OperationsForGPTest {

  @Test
  public void testOperationsForGP() {
    long gpAvailablepages = totalAvailablepages();

//    System.out.println("address of GPHead_AvailablePagesHead: "+
//        Long.toHexString(allocator_get_addressGPHead_AvailablePagesHead()));

    MatcherAssert.assertThat(Allocator.ManagedPoolStats.currentNumOfGPAvaiablePages(),
        is(gpAvailablepages));

    long page1 = gp_Page_poll();
    assertThat(Allocator.ManagedPoolStats.currentNumOfGPAvaiablePages(),
        is(gpAvailablepages - 1L));

    long page2 = gp_Page_poll();
    assertThat(Allocator.ManagedPoolStats.currentNumOfGPAvaiablePages(),
        is(gpAvailablepages - 2L));

    long page3 = gp_Page_poll();
    assertThat(Allocator.ManagedPoolStats.currentNumOfGPAvaiablePages(),
        is(gpAvailablepages - 3L));

    assertThat(page2 - page1, is(SIZE_ZMPAGE));
    assertThat(page3-page2,is(SIZE_ZMPAGE));

    gp_Page_offer(page1);
    assertThat(Allocator.ManagedPoolStats.currentNumOfGPAvaiablePages(),
        is(gpAvailablepages - 2L));
    gp_Page_offer(page2);
    assertThat(Allocator.ManagedPoolStats.currentNumOfGPAvaiablePages(),
        is(gpAvailablepages - 1L));
    gp_Page_offer(page3);
    assertThat(Allocator.ManagedPoolStats.currentNumOfGPAvaiablePages(),
        is(gpAvailablepages));

    long[] pages = new long[(int)gpAvailablepages];
    for (int i = 0; i < gpAvailablepages; i++) {
      pages[i] = gp_Page_poll();
      assertThat(gpAvailablepages-i-1,
          is(Allocator.ManagedPoolStats.currentNumOfGPAvaiablePages()));
    }
    //return all pages
    for (int i = 0; i < gpAvailablepages; i++) {
      gp_Page_offer(pages[i]);
    }

    //finally last page is page3
    assertThat(pages[(int)gpAvailablepages-1],
        is(page3));
  }

}
