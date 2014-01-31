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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import static z.offheap.zmalloc.AllocatorPrivatesBridge.sizeClassIndex;

public class AllocatorTest {

  @Test
  public void testSizeTypeIndex() {
    int sizeOfBytes;

    sizeOfBytes = 7;
    assertThat(sizeClassIndex(sizeOfBytes), is(0));//8

    sizeOfBytes = 9;
    assertThat(sizeClassIndex(sizeOfBytes), is(1));//16

    sizeOfBytes = 15;
    assertThat(sizeClassIndex(sizeOfBytes), is(1));//16

    sizeOfBytes = 9;
    assertThat(sizeClassIndex(sizeOfBytes), is(1));//16

    sizeOfBytes = 17;
    assertThat(sizeClassIndex(sizeOfBytes), is(2));//24

    sizeOfBytes = 24;
    assertThat(sizeClassIndex(sizeOfBytes), is(2));//24

    sizeOfBytes = 25;
    assertThat(sizeClassIndex(sizeOfBytes), is(3));//32

    sizeOfBytes = 31;
    assertThat(sizeClassIndex(sizeOfBytes), is(3));//32

    sizeOfBytes = 32;
    assertThat(sizeClassIndex(sizeOfBytes), is(3));//32

    sizeOfBytes = 33;
    assertThat(sizeClassIndex(sizeOfBytes), is(4));//48

    sizeOfBytes = 48;
    assertThat(sizeClassIndex(sizeOfBytes), is(4));//48

    sizeOfBytes = 63;
    assertThat(sizeClassIndex(sizeOfBytes), is(5));//64

    sizeOfBytes = 65;
    assertThat(sizeClassIndex(sizeOfBytes), is(6));//96

    sizeOfBytes = 128;
    assertThat(sizeClassIndex(sizeOfBytes), is(7));

    sizeOfBytes = 129;
    assertThat(sizeClassIndex(sizeOfBytes), is(8));

    sizeOfBytes = 1024;
    assertThat(sizeClassIndex(sizeOfBytes), is(13));

    sizeOfBytes = 1023;
    assertThat(sizeClassIndex(sizeOfBytes), is(13));

    sizeOfBytes = 1025;
    assertThat(sizeClassIndex(sizeOfBytes), is(14));

    sizeOfBytes = 1535;
    assertThat(sizeClassIndex(sizeOfBytes), is(14));

    sizeOfBytes = 8192;
    assertThat(sizeClassIndex(sizeOfBytes), is(19));

    sizeOfBytes = 8193;
    assertThat(sizeClassIndex(sizeOfBytes), is(20));

    sizeOfBytes = 12288;
    assertThat(sizeClassIndex(sizeOfBytes), is(20));

    sizeOfBytes = 32768;
    assertThat(sizeClassIndex(sizeOfBytes), is(23));
    sizeOfBytes = 32769;
    assertThat(sizeClassIndex(sizeOfBytes), is(24));
    sizeOfBytes = 49152;
    assertThat(sizeClassIndex(sizeOfBytes), is(24));
    sizeOfBytes = 65536;
    assertThat(sizeClassIndex(sizeOfBytes), is(25));
    sizeOfBytes = 65537;
    assertThat(sizeClassIndex(sizeOfBytes), is(26));
    sizeOfBytes = 98303;
    assertThat(sizeClassIndex(sizeOfBytes), is(26));
    sizeOfBytes = 98304;
    assertThat(sizeClassIndex(sizeOfBytes), is(26));
    sizeOfBytes = 98305;
    assertThat(sizeClassIndex(sizeOfBytes), is(27));
    sizeOfBytes = 131072;
    assertThat(sizeClassIndex(sizeOfBytes), is(27));
    sizeOfBytes = 196608;
    assertThat(sizeClassIndex(sizeOfBytes), is(28));
    sizeOfBytes = 262144;
    assertThat(sizeClassIndex(sizeOfBytes), is(29));
    sizeOfBytes = 393216;
    assertThat(sizeClassIndex(sizeOfBytes), is(30));
    sizeOfBytes = 524288;
    assertThat(sizeClassIndex(sizeOfBytes), is(31));
    sizeOfBytes = 786432;
    assertThat(sizeClassIndex(sizeOfBytes), is(32));
    sizeOfBytes = 786433;
    assertThat(sizeClassIndex(sizeOfBytes), is(33));

    sizeOfBytes = 1048575;
    assertThat(sizeClassIndex(sizeOfBytes), is(33));
    sizeOfBytes = 1048576;
    assertThat(sizeClassIndex(sizeOfBytes), is(33));
    sizeOfBytes = 1048577;
    assertThat(sizeClassIndex(sizeOfBytes), is(34));

    sizeOfBytes = 1572863;
    assertThat(sizeClassIndex(sizeOfBytes), is(34));
    sizeOfBytes = 1572864;
    assertThat(sizeClassIndex(sizeOfBytes), is(34));
    sizeOfBytes = 1572865;
    assertThat(sizeClassIndex(sizeOfBytes), is(35));

    sizeOfBytes = 1<<21;
    assertThat(sizeClassIndex(sizeOfBytes), is(35));
    sizeOfBytes = 1<<28;
    assertThat(sizeClassIndex(sizeOfBytes), is(35));
    sizeOfBytes = 1<<30;
    assertThat(sizeClassIndex(sizeOfBytes), is(35));


  }

  @Test
  public void testNumOfMaxChunksForSizeClass() {

  }

//  @Test
//  public void testOperationsForGP() {
//    long TOTAL_AVAILABLEPAGES = allocator_TOTAL_AVAILABLEPAGES();
//
////    System.out.println("address of GPHead_AvailablePagesHead: "+
////        Long.toHexString(allocator_get_addressGPHead_AvailablePagesHead()));
//
//    MatcherAssert.assertThat(Allocator.ManagedPoolStats.currentNumOfGPAvaiablePages(),
//        is(TOTAL_AVAILABLEPAGES));
//
//    long page1 = gp_Page_pop();
//    assertThat(Allocator.ManagedPoolStats.currentNumOfGPAvaiablePages(),
//        is(TOTAL_AVAILABLEPAGES - 1L));
//
//    long page2 = gp_Page_pop();
//    assertThat(Allocator.ManagedPoolStats.currentNumOfGPAvaiablePages(),
//        is(TOTAL_AVAILABLEPAGES - 2L));
//
//    long page3 = gp_Page_pop();
//    assertThat(Allocator.ManagedPoolStats.currentNumOfGPAvaiablePages(),
//        is(TOTAL_AVAILABLEPAGES - 3L));
//
//    assertThat(page1-page2,is(SIZE_ZMPAGE));
//    assertThat(page2-page3,is(SIZE_ZMPAGE));
//
//    gp_Page_push(page1);
//    assertThat(Allocator.ManagedPoolStats.currentNumOfGPAvaiablePages(),
//        is(TOTAL_AVAILABLEPAGES - 2L));
//    gp_Page_push(page2);
//    assertThat(Allocator.ManagedPoolStats.currentNumOfGPAvaiablePages(),
//        is(TOTAL_AVAILABLEPAGES - 1L));
//    gp_Page_push(page3);
//    assertThat(Allocator.ManagedPoolStats.currentNumOfGPAvaiablePages(),
//        is(TOTAL_AVAILABLEPAGES));
//
//    long[] pages = new long[(int)TOTAL_AVAILABLEPAGES];
//    for (int i = 0; i < TOTAL_AVAILABLEPAGES; i++) {
//      pages[i] = gp_Page_pop();
//      assertThat(TOTAL_AVAILABLEPAGES-i-1,
//          is(Allocator.ManagedPoolStats.currentNumOfGPAvaiablePages()));
//    }
//    //return all pages
//    for (int i = 0; i < TOTAL_AVAILABLEPAGES; i++) {
//      gp_Page_push(pages[i]);
//    }
//
//    //finally last page is at addressGP
////    System.out.println("addressGP: "+
////        Long.toHexString(allocator_get_addressGP()));
//    assertThat(pages[(int)TOTAL_AVAILABLEPAGES-1],
//        is(allocator_get_addressGP()));
//  }
//
//
//  @Test
//  public void testOperationsForTLP00() {
//    int sci = 0;
//    long tid = getCurrentThreadId();
//
////    assertThat(Allocator.ManagedPoolStats.currentNumOfTLPs(),is(0L));
//    assertThat(Allocator.ManagedPoolStats.currentNumOfTLPFreePages(),is(0L));
//
//    //sizeClass - 8B
//    assertThat(Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(0),is(0L));
//
//    //then request a page from gp
//    long page = gp_Page_pop();
//    //initialize  with 8B sizeClass
//    assertThat(page,not(0L));
//    assertThat(isPageAligned(page),is(true));
//    pg_setupPage(page,sci,tid);
//
//    assertThat(Allocator.ManagedPoolStats.currentNumOfTLPs(),is(1L));
//
//    tlp_AvailablePages_push(tid,sci,page);
//    assertThat(Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(sci),is(1L));
//
//
//    long fakeFullPage = tlp_AvailablePages_pop(tid,sci);
//    assertThat(Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(sci),is(0L));
//
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfPGAvailableChunks(fakeFullPage),
//        is((SIZE_ZMPAGE-ZMPAGE_RAWCHUNK_OFFSET)/8));
//    long chunk = pg_AvailableChunks_pop(fakeFullPage);
//
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfPGAvailableChunks(fakeFullPage),
//        is((SIZE_ZMPAGE-ZMPAGE_RAWCHUNK_OFFSET)/8-1));
////    System.out.println(Long.toHexString(fakeFullPage));
////    System.out.println(Long.toHexString(chunk));
//    assertThat(fakeFullPage, is(chunk-(chunk%SIZE_ZMPAGE)));
//    assertThat(fakeFullPage, is(chunk+8L-(SIZE_ZMPAGE)));
//
//  }
//
//  @Test
//  public void testOperationsForTLP01() {
//    //XXX: test sizeClass 16B
//    int sci = 1;
//    long tid = getCurrentThreadId();
//
//    tlp_ini(tid);
//    assertThat(Allocator.ManagedPoolStats.currentNumOfTLPs(),is(1L));
//
//    //then request a page from gp
//    long page = gp_Page_pop();
//    pg_setupPage(page,sci,tid);
//    //initialize  with 8B sizeClass
//    assertThat(page,not(0L));
//    assertThat(isPageAligned(page),is(true));
//
//    tlp_AvailablePages_push(tid,sci,page);
//    assertThat(Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(sci),is(1L));
//
//
//    long fakeFullPage = tlp_AvailablePages_pop(tid,sci);
//    assertThat(Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(sci),is(0L));
//
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfPGAvailableChunks(fakeFullPage),
//        is(((SIZE_ZMPAGE)-ZMPAGE_RAWCHUNK_OFFSET)/16));
//    long chunk = pg_AvailableChunks_pop(fakeFullPage);
//
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfPGAvailableChunks(fakeFullPage),
//        is(((SIZE_ZMPAGE)-ZMPAGE_RAWCHUNK_OFFSET)/16-1L));
////    System.out.println(Long.toHexString(fakeFullPage));
////    System.out.println(Long.toHexString(chunk));
//    assertThat(fakeFullPage, is(chunk-(chunk%SIZE_ZMPAGE)));
//    assertThat(fakeFullPage, is(chunk+16L-(SIZE_ZMPAGE)));
//  }
//
//  @Test
//  public void testAllocateFreeSmall() {
//    long chunk0 = Allocator.allocate(15);
//    long chunk1 = Allocator.allocate(15);
//    long chunk2 = Allocator.allocate(15);
//    long page = chunk0-(chunk0%SIZE_ZMPAGE);
//    assertThat(chunk0-(chunk0%SIZE_ZMPAGE),
//        is(chunk2-(chunk2%SIZE_ZMPAGE)));
//
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfPGAvailableChunks(page),
//        is(((SIZE_ZMPAGE)-ZMPAGE_RAWCHUNK_OFFSET)/16-3L));
//
//    Allocator.free(chunk0);
//    Allocator.free(chunk1);
//    Allocator.free(chunk2);
//
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfPGAvailableChunks(page),
//        is(((SIZE_ZMPAGE)-ZMPAGE_RAWCHUNK_OFFSET)/16));
//
//    long chunk3 = Allocator.allocate(12);
//    long chunk4 = Allocator.allocate(13);
//    long chunk5 = Allocator.allocate(14);
//
//    assertThat(chunk3,is(chunk2));
//    assertThat(chunk4,is(chunk1));
//    assertThat(chunk5,is(chunk0));
//  }
//
//  @Test
//  public void testAllocateFree512k() {
//    long chunk0 = Allocator.allocate(500_000);//->524288
//    long page0 = chunk0-(chunk0%SIZE_ZMPAGE);
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfPGAvailableChunks(page0),is(0L));
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(31),//->524288
//        is(0L));
//
//    long chunk1 = Allocator.allocate(500_000);
//    long page1 = chunk1-(chunk1%SIZE_ZMPAGE);
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfPGAvailableChunks(page1),is(0L));
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(31),//->524288
//        is(0L));
//
//    Allocator.free(chunk0);
//    Allocator.free(chunk1);
//
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(31),//->524288
//        is(2L));
//
//    long chunk2 = Allocator.allocate(500_001);
//    long chunk3 = Allocator.allocate(500_002);
//
//    assertThat(chunk2,is(chunk1));
//    assertThat(chunk3,is(chunk0));
//  }
//
//  @Test
//  public void testFree1() {
//    long[] addresses = new long[6];
//    for (int i = 0; i < 6; i++) {
//      addresses[i]  = Allocator.allocate(256*1024);
//    }
//
//    long TOTAL_AVAILABLEPAGES = allocator_TOTAL_AVAILABLEPAGES();
//
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfGPAvaiablePages(),
//        is(TOTAL_AVAILABLEPAGES-2));
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(29),//->262144
//        is(0L));
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfTLPFreePages(),
//        is(0L));
//
//    Allocator.free(addresses[0]);
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(29),//->262144
//        is(1L));
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfTLPFreePages(),
//        is(0L));
//
//    Allocator.free(addresses[1]);
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(29),//->262144
//        is(1L));
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfTLPFreePages(),
//        is(0L));
//
//    Allocator.free(addresses[2]);
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(29),//->262144
//        is(0L));
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfTLPFreePages(),
//        is(1L));
//
//    Allocator.free(addresses[3]);
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(29),//->262144
//        is(1L));
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfTLPFreePages(),
//        is(1L));
//
//    Allocator.free(addresses[4]);
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(29),//->262144
//        is(1L));
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfTLPFreePages(),
//        is(1L));
//
//    Allocator.free(addresses[5]);
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(29),//->262144
//        is(0L));
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfTLPFreePages(),
//        is(2L));
//
//  }
//
//
//  @Test
//  public void testFree2() {
//    long[] addresses29 = new long[3];
//    for (int i = 0; i < 3; i++) {
//      addresses29[i]  = Allocator.allocate(256*1024);
//    }
//
//    long[] addresses30 = new long[3];
//    for (int i = 0; i < 3; i++) {
//      addresses30[i]  = Allocator.allocate(300*1024);
//    }
//
//    long TOTAL_AVAILABLEPAGES = allocator_TOTAL_AVAILABLEPAGES();
//
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfGPAvaiablePages(),
//        is(TOTAL_AVAILABLEPAGES-3));
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(29),
//        is(0L));
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(30),
//        is(1L));
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfTLPFreePages(),
//        is(0L));
//
//
//    Allocator.free(addresses29[0]);
//    Allocator.free(addresses30[0]);
//    Allocator.free(addresses29[1]);
//    Allocator.free(addresses30[1]);
//    Allocator.free(addresses29[2]);
//    Allocator.free(addresses30[2]);
//
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(29),
//        is(0L));
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(30),
//        is(0L));
//    assertThat(
//        Allocator.ManagedPoolStats.currentNumOfTLPFreePages(),
//        is(3L));
//
//  }

}
