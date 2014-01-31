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

import org.junit.BeforeClass;
import org.junit.Test;
import z.offheap.zmalloc.Allocator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static z.offheap.zmalloc.AllocatorPrivatesBridge.TLP_AVAILABLEPAGES_OFFSET;
import static z.offheap.zmalloc.AllocatorPrivatesBridge.addressTLPs;
import static z.util.Unsafes.*;
import static z.offheap.zmalloc.AllocatorPrivatesBridge.*;

public class OperationsForTLPTest {

  @BeforeClass
  public static void tlp_ensure() {
    long tid = currentThreadId();
    tlp_ini(tid);
  }

  @Test
  public void testOperationsForTLP00() {
    int sci = 0;
    long tid = currentThreadId();

    //prepare TLP variables
    long addressTLP = addressTLPs() + TLP_ITEM() *tid;
    long addrAvailablePages = addressTLP + TLP_AVAILABLEPAGES_OFFSET();
    long addrAvailablePageHead = addrAvailablePages +
        sci* SIZE_TLP_AVAILABLEPAGES_ITEM();

    assertThat(Allocator.ManagedPoolStats.currentNumOfTLPFreePages(), is(0L));

    //sizeClass - 8B
    assertThat(Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(sci),is(0L));

    //then request a page from gp
    long page = gp_Page_poll();
    //initialize  with 8B sizeClass
    assertThat(page,not(0L));
    assertThat(isPageAligned(page),is(true));
    pg_setupPage(page, sci, tid);

    assertThat(Allocator.ManagedPoolStats.currentNumOfTLPs(),is(1L));

    tlp_AvailablePages_addToHead(addrAvailablePageHead, page);
    assertThat(Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(sci),is(1L));


    tlp_AvailablePages_remove(addrAvailablePageHead, page);
    assertThat(Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(sci),is(0L));

    long fakeFullPage = page;
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfPageAvailableChunks(fakeFullPage),
        is(ZMPAGE_MAX_CHUNK_SIZE()/8));

    long chunk = pg_AvailableChunks_pop(fakeFullPage);
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfPageAvailableChunks(fakeFullPage),
        is(ZMPAGE_MAX_CHUNK_SIZE()/8-1));
//    System.out.println(Long.toHexString(fakeFullPage));
//    System.out.println(Long.toHexString(chunk));
    assertThat(fakeFullPage, is(chunk-(chunk%SIZE_ZMPAGE)));
    assertThat(fakeFullPage, is(chunk+8L-(SIZE_ZMPAGE)));//in that last chunk
  }

  @Test
  public void testOperationsForTLP01() {
    //XXX: test sizeClass 16B
    int sci = 1;
    long tid = currentThreadId();

    //prepare TLP variables
    long addressTLP = addressTLPs() + TLP_ITEM() *tid;
    long addrAvailablePages = addressTLP + TLP_AVAILABLEPAGES_OFFSET();
    long addrAvailablePageHead = addrAvailablePages +
        sci* SIZE_TLP_AVAILABLEPAGES_ITEM();
    assertThat(Allocator.ManagedPoolStats.currentNumOfTLPs(),is(1L));

    //then request a page from gp
    long page = gp_Page_poll();
    pg_setupPage(page, sci, tid);
    //initialize  with 8B sizeClass
    assertThat(page,not(0L));
    assertThat(isPageAligned(page),is(true));

    tlp_AvailablePages_addToHead(addrAvailablePageHead, page);
    assertThat(Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(sci),is(1L));


    tlp_AvailablePages_remove(addrAvailablePageHead, page);
    assertThat(Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(sci),is(0L));

    long fakeFullPage = page;
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfPageAvailableChunks(fakeFullPage),
        is(ZMPAGE_MAX_CHUNK_SIZE()/16));
    long chunk = pg_AvailableChunks_pop(fakeFullPage);

    assertThat(
        Allocator.ManagedPoolStats.currentNumOfPageAvailableChunks(fakeFullPage),
        is(ZMPAGE_MAX_CHUNK_SIZE()/16-1));
//    System.out.println(Long.toHexString(fakeFullPage));
//    System.out.println(Long.toHexString(chunk));
    assertThat(fakeFullPage, is(chunk-(chunk%SIZE_ZMPAGE)));
    assertThat(fakeFullPage, is(chunk+16L-(SIZE_ZMPAGE)));
  }

}
