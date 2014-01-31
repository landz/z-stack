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

import org.junit.BeforeClass;
import org.junit.Test;
import z.offheap.zmalloc.Allocator;

import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static z.offheap.zmalloc.AllocatorPrivatesBridge.*;
import static z.util.Throwables.uncheck;
import static z.util.Unsafes.currentThreadId;
import static z.util.Unsafes.isPageAligned;

/**
 * tests for RemoteFreedChunks with multi-threads
 * NOTE: tl;dr - Too long; didn't run:)
 * trick: use the direct call
 */
public class RemoteFreedChunksMTStressTest2 {

  private static final int NPAGES =  100;
  private static final int COUNT    = NPAGES *200_000;//8B,#0
  private static final int NTHREADS = 100;

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
    //XXX: test sizeClass 8B
    int sci = 0;
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
    assertThat(tlp_RemoteFreedChunksHead_remove(addrRemoteFreedChunksHead,
        addrRemoteFreedChunksTail,
        addrRemoteFreedChunksDummy),
        is(0L));//NULL_ADDRESS


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

    //============================================
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch endLatch = new CountDownLatch(NTHREADS);


    for (int i = 0; i < NTHREADS; i++) {
      spawnProducer(sci, tid, chunks, i, startLatch, endLatch).start();
    }


    long c;
    HashSet<Long> results = new HashSet();

    startLatch.countDown();
    for (int i = 0; i < COUNT; i++) {
      while (0==(c=tlp_RemoteFreedChunksHead_remove(
          addrRemoteFreedChunksHead,
          addrRemoteFreedChunksTail,
          addrRemoteFreedChunksDummy))) {
      }
      results.add(c);
    }

    assertThat(results.size(),is(COUNT));

    assertThat(tlp_RemoteFreedChunksHead_remove(addrRemoteFreedChunksHead,
        addrRemoteFreedChunksTail,
        addrRemoteFreedChunksDummy),
        is(0L));//NULL_ADDRESS

    //ensure all threads end, not necessary for this test
    uncheck(() -> endLatch.await());
  }


  private Thread spawnProducer(
      int sci,
      long ownedTid,
      long[] chunks,
      int mod,
      CountDownLatch startLatch,
      CountDownLatch endLatch) {
    return
        new Thread(() -> {
          uncheck(() -> startLatch.await());

          //prepare TLP variables
          long addressTLP = addressTLPs() + TLP_ITEM() *ownedTid;
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


          for (int i=0;i<COUNT;i++) {
            if ( (i%NTHREADS)==mod ) {
              assertThat(ownedTid,not(currentThreadId()));
              tlp_RemoteFreedChunksHead_add(addrRemoteFreedChunksTail,chunks[i]);
            }
          }

//          System.out.println("producer thread "+getCurrentThreadId()+" done!");

          endLatch.countDown();
        });
  }

}
