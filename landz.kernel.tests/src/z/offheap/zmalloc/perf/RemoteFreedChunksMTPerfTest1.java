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

import org.junit.BeforeClass;
import org.junit.Test;
import z.offheap.zmalloc.Allocator;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static z.util.Unsafes.currentThreadId;
import static z.util.Unsafes.isPageAligned;


import static z.offheap.zmalloc.AllocatorPrivatesBridge.tlp_ini;
import static z.offheap.zmalloc.AllocatorPrivatesBridge.addressTLPs;
import static z.offheap.zmalloc.AllocatorPrivatesBridge.TLP_ITEM;
import static z.offheap.zmalloc.AllocatorPrivatesBridge.TLP_AVAILABLEPAGES_OFFSET;
import static z.offheap.zmalloc.AllocatorPrivatesBridge.SIZE_TLP_AVAILABLEPAGES_ITEM;
import static z.offheap.zmalloc.AllocatorPrivatesBridge.TLP_REMOTEFREEDCHUNKS_HEAD_OFFSET;
import static z.offheap.zmalloc.AllocatorPrivatesBridge.TLP_REMOTEFREEDCHUNKS_TAIL_OFFSET;
import static z.offheap.zmalloc.AllocatorPrivatesBridge.TLP_REMOTEFREEDCHUNKS_DUMMY_OFFSET;
import static z.offheap.zmalloc.AllocatorPrivatesBridge.gp_Page_poll;
import static z.offheap.zmalloc.AllocatorPrivatesBridge.pg_setupPage;
import static z.offheap.zmalloc.AllocatorPrivatesBridge.pg_AvailableChunks_pop;

//XXX:please use the direct call when testing
import static z.offheap.zmalloc.AllocatorPrivatesBridge.tlp_RemoteFreedChunksHead_add;
import static z.offheap.zmalloc.AllocatorPrivatesBridge.tlp_RemoteFreedChunksHead_remove;

//import static z.offheap.zmalloc.Allocator.*;
/**
 * the MH wrapped version has a bad performance.
 * So, please use the direct call when testing
 * (as such, to modify your source temporarily)
 *
 */
public class RemoteFreedChunksMTPerfTest1 {

  private static final int NPAGES =  1;//100;
  private static final int COUNT    = NPAGES *100_000;//16B,#0

  @BeforeClass
  public static void tlp_ensure() {
    long tid = currentThreadId();
    tlp_ini(tid);
  }

  @Test
  public void test() {
    for (int i = 0; i < 2; i++) {
      System.out.println("======RUNS#"+i);
      testRemoteFreedChunksAddRemoveMT();
    }
  }


  public void testRemoteFreedChunksAddRemoveMT() {
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
    long[] pages = new long[NPAGES];

    for (int i = 0; i < NPAGES; i++) {
      pages[i] = gp_Page_poll();
      pg_setupPage(pages[i], sci, tid);
      //initialize  with 8B sizeClass
      assertThat(pages[i],not(0L));
      assertThat(isPageAligned(pages[i]),is(true));
    }

    //============================================
    long[] chunks = new long[COUNT];

    int ct = 0;
    int NCT = COUNT/NPAGES;
    int pgct = 0;
    for (int i = 0; i < COUNT; i++) {
      if (ct!=NCT) {
        ct++;
      }else {
        ct=0;
        pgct++;
      }
      chunks[i] = pg_AvailableChunks_pop(pages[pgct]);
    }

    //ensure all address in chunks are valid
    for (int i = 0; i < COUNT; i++) {
      assertThat(chunks[i],greaterThan(4_000_000_000L));//mmapped memory in high address
    }

    System.out.println("start test...");
    //============================================

    long s = System.nanoTime();
    for (int i=0;i<COUNT;i++) {
      tlp_RemoteFreedChunksHead_add(addrRemoteFreedChunksTail, chunks[i]);
    }
    long t = System.nanoTime()-s;
    System.out.println("add "+COUNT+" chunks to RemoteRreedChunks cost: "
        + TimeUnit.NANOSECONDS.toMillis(t) + " millis");

    long[] removed = new long[COUNT];
    s = System.nanoTime();
    for (int i=0;i<COUNT;i++) {
      removed[i] = tlp_RemoteFreedChunksHead_remove(addrRemoteFreedChunksHead,
          addrRemoteFreedChunksTail,
          addrRemoteFreedChunksDummy);
    }
    t = System.nanoTime()-s;
    System.out.println("remove "+COUNT+" chunks from RemoteRreedChunks cost: "
        + TimeUnit.NANOSECONDS.toMillis(t) + " millis");

    for (int i = 0; i < removed.length; i++) {
      assertThat(removed[i], greaterThan(4_000_000_000L));
    }

  }

}
