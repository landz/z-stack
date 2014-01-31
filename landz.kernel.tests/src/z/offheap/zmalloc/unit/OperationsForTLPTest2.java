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
import static z.offheap.zmalloc.AllocatorPrivatesBridge.*;
import static z.util.Unsafes.currentThreadId;
import static z.util.Unsafes.isPageAligned;

/**
 * tests for RemoteFreedChunks
 */
public class OperationsForTLPTest2 {

  @BeforeClass
  public static void tlp_ensure() {
    long tid = currentThreadId();
    tlp_ini(tid);
  }

  @Test
  public void testRemoteFreedChunksAddRemove() {
    //XXX: test sizeClass 16B
    int sci = 1;
    long tid = currentThreadId();

    //prepare TLP variables
    long addressTLP = addressTLPs() + TLP_ITEM() *tid;
    long addrAvailablePages = addressTLP + TLP_AVAILABLEPAGES_OFFSET();
    long addrAvailablePageHead = addrAvailablePages +
        sci* SIZE_TLP_AVAILABLEPAGES_ITEM();
    assertThat(Allocator.ManagedPoolStats.currentNumOfTLPs(),is(1L));

    long addrRemoteFreedChunksHead =
        addressTLP + TLP_REMOTEFREEDCHUNKS_HEAD_OFFSET();
    long addrRemoteFreedChunksTail =
        addressTLP + TLP_REMOTEFREEDCHUNKS_TAIL_OFFSET();
    long addrRemoteFreedChunksDummy =
        addressTLP + TLP_REMOTEFREEDCHUNKS_DUMMY_OFFSET();

    //then request a page from gp
    long page = gp_Page_poll();
    pg_setupPage(page, sci, tid);
    //initialize  with 8B sizeClass
    assertThat(page,not(0L));
    assertThat(isPageAligned(page),is(true));

    tlp_AvailablePages_addToHead(addrAvailablePageHead, page);
    assertThat(
        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(sci),is(1L));

    //============================================
    assertThat(tlp_RemoteFreedChunksHead_remove(addrRemoteFreedChunksHead,
        addrRemoteFreedChunksTail,
        addrRemoteFreedChunksDummy),
        is(0L));//NULL_ADDRESS

    long c0 = pg_AvailableChunks_pop(page);
    long c1 = pg_AvailableChunks_pop(page);
    long c2 = pg_AvailableChunks_pop(page);

    tlp_RemoteFreedChunksHead_add(addrRemoteFreedChunksTail,c0);
    long cc0 = tlp_RemoteFreedChunksHead_remove(addrRemoteFreedChunksHead,
        addrRemoteFreedChunksTail,
        addrRemoteFreedChunksDummy);

    assertThat(cc0, is(c0));
    assertThat(tlp_RemoteFreedChunksHead_remove(addrRemoteFreedChunksHead,
        addrRemoteFreedChunksTail,
        addrRemoteFreedChunksDummy),
        is(0L));//NULL_ADDRESS

    tlp_RemoteFreedChunksHead_add(addrRemoteFreedChunksTail,c0);
    tlp_RemoteFreedChunksHead_add(addrRemoteFreedChunksTail,c1);
    tlp_RemoteFreedChunksHead_add(addrRemoteFreedChunksTail,c2);
    assertThat(tlp_RemoteFreedChunksHead_remove(addrRemoteFreedChunksHead,
        addrRemoteFreedChunksTail,
        addrRemoteFreedChunksDummy),
        is(c0));
    assertThat(tlp_RemoteFreedChunksHead_remove(addrRemoteFreedChunksHead,
        addrRemoteFreedChunksTail,
        addrRemoteFreedChunksDummy),
        is(c1));
    assertThat(tlp_RemoteFreedChunksHead_remove(addrRemoteFreedChunksHead,
        addrRemoteFreedChunksTail,
        addrRemoteFreedChunksDummy),
        is(c2));
    assertThat(tlp_RemoteFreedChunksHead_remove(addrRemoteFreedChunksHead,
        addrRemoteFreedChunksTail,
        addrRemoteFreedChunksDummy),
        is(0L));//NULL_ADDRESS

  }

}
