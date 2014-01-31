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

package z.offheap.contrast.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runners.MethodSorters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SlabFragmentationTest {

  private static final ByteBufAllocator POOLED_ALLOCATOR_DIRECT = new PooledByteBufAllocator(true);

  private static final int[] sizes = {0, 256, 1024, 4096, 65536, 256 * 1024, 1024 * 1024};
  private static final ByteBufAllocator[] allocators = {POOLED_ALLOCATOR_DIRECT};// UNPOOLED_ALLOCATOR_DIRECT};

  //XXX: ByteBuf instances should be on heap
  private ByteBuf[] bufs1;
  private ByteBuf[] bufs2;
  private ByteBuf[] bufs3;
  private ByteBuf[] bufs4;
  private ByteBuf[] bufs5;

  @Rule
  public ErrorCollector collector = new ErrorCollector();


  @Test
  public void testSlabClassification1() {
    //============== sweep all
    bufs1 = new ByteBuf[100_000];
    bufs2 = new ByteBuf[300_000];
    bufs3 = new ByteBuf[150_000];
    bufs4 = new ByteBuf[800_000];
    bufs5 = new ByteBuf[5_000];

    int size = 0;
    //============== < 800_000k initial free, but not sure exact number
    size = 7 * 1024;
    System.out.println("======1.1: apply 100_000*7k...");
    for (int i = 0; i < 100_000; i++) {
      bufs1[i] = (POOLED_ALLOCATOR_DIRECT.buffer(size));
    }
//    System.out.println(POOLED_ALLOCATOR_DIRECT);

    System.out.println("======1.2: free 50_000*7*1024...");
    for (int i = 0; i < 50_000; i++) {
      bufs1[i].release();
    }

    assertThat(bufs1[49_999].toString().contains("free"), is(true));
    assertThat(bufs1[50_000].toString().contains("free"), is(false));

    //============== 350_000k used
    size = 1024;
    System.out.println("======2.1: apply 300_000*1024(<50_000*7k)...");
    for (int i = 0; i < 300_000; i++) {
      bufs2[i] = (POOLED_ALLOCATOR_DIRECT.buffer(size));
    }

    System.out.println("======2.2: free all 1k bufs...");
    for (int i = 0; i < bufs2.length; i++) {
      bufs2[i].release();
    }

    assertThat(bufs2[0].toString().contains("free"), is(true));
    assertThat(bufs2[1].toString().contains("free"), is(true));

    //============== 350_000k used
    size = 2 * 1024;
    System.out.println("======3.1: apply 150_000*2k(=300_000*1024)...");
    for (int i = 0; i < 150_000; i++) {
      bufs3[i] = (POOLED_ALLOCATOR_DIRECT.buffer(size));
    }

    System.out.println("======3.2: free 100_000 2k bufs...");
    for (int i = 0; i < 100_000; i++) {
      bufs3[i].release();
    }

    assertThat(bufs3[99_999].toString().contains("free"), is(true));
    assertThat(bufs3[100_000].toString().contains("free"), is(false));

    //============== 450_000k used
    size = 256;
    System.out.println("======4.1: apply 800_000*256(=200_000*1024)...");
    for (int i = 0; i < 800_000; i++) {
      bufs4[i] = (POOLED_ALLOCATOR_DIRECT.buffer(size));
    }

//    System.out.println("======4.2: free all 800_000 256 bufs...");
//    for (int i = 0; i < bufs4.length; i++) {
//      bufs4[i].release();
//    }

    //============== 650_000k used
    size = 16 * 1024;
    System.out.println("======5.1: apply 5_000*16*1024(=80_000*1024)...");
    for (int i = 0; i < 5_000; i++) {
      bufs5[i] = (POOLED_ALLOCATOR_DIRECT.buffer(size));
    }

    //============== 730_000k used

    //============== free all
    for (ByteBuf buf : bufs1)
      if (buf != null && buf.refCnt() != 0) buf.release();
    for (ByteBuf buf : bufs2)
      if (buf != null && buf.refCnt() != 0) buf.release();
    for (ByteBuf buf : bufs3)
      if (buf != null && buf.refCnt() != 0) buf.release();
    for (ByteBuf buf : bufs4)
      if (buf != null && buf.refCnt() != 0) buf.release();
    for (ByteBuf buf : bufs5)
      if (buf != null && buf.refCnt() != 0) buf.release();

  }

  @Test
  public void testSlabClassification2() {
    //============== sweep all
    bufs1 = new ByteBuf[100_000];
    bufs2 = new ByteBuf[300_000];
    bufs3 = new ByteBuf[150_000];
    bufs4 = new ByteBuf[800_000];
    bufs5 = new ByteBuf[2000];

    int size = 0;
    //============== < 800_000k initial free, but not sure exact number
    size = 7 * 1024;
    System.out.println("======1.1: apply 100_000*7k...");
    for (int i = 0; i < 100_000; i++) {
      bufs1[i] = (POOLED_ALLOCATOR_DIRECT.buffer(size));
    }
//    System.out.println(POOLED_ALLOCATOR_DIRECT);

    System.out.println("======1.2: free 50_000*7*1024...");
    for (int i = 0; i < 100_000; i += 2) {
      bufs1[i].release();
    }

    assertThat(bufs1[49_999].toString().contains("free"), is(false));
    assertThat(bufs1[50_000].toString().contains("free"), is(true));

    //============== 350_000k used
    size = 1024;
    System.out.println("======2.1: apply 300_000*1024(<50_000*7k)...");
    for (int i = 0; i < 300_000; i++) {
      bufs2[i] = (POOLED_ALLOCATOR_DIRECT.buffer(size));
    }

    System.out.println("======2.2: free all 1k bufs...");
    for (int i = 0; i < bufs2.length; i++) {
      bufs2[i].release();
    }

    assertThat(bufs2[0].toString().contains("free"), is(true));
    assertThat(bufs2[1].toString().contains("free"), is(true));

    //============== 350_000k used
    size = 2 * 1024;
    System.out.println("======3.1: apply 150_000*2k(=300_000*1024)...");
    for (int i = 0; i < 150_000; i++) {
      bufs3[i] = (POOLED_ALLOCATOR_DIRECT.buffer(size));
    }

    System.out.println("======3.2: free 50_000 2k bufs from 0 to 50_000...");
    for (int i = 0; i < 50_000; i++) {
      bufs3[i].release();
    }

    System.out.println("======3.3: free 50_000 2k bufs from 50_000 to 150_000 in interleaved style...");
    for (int i = 50_000; i < 150_000; i += 2) {
      bufs3[i].release();
    }

    assertThat(bufs3[99_999].toString().contains("free"), is(false));
    assertThat(bufs3[100_000].toString().contains("free"), is(true));

    //============== 450_000k used
    size = 256;
    System.out.println("======4.1: apply 800_000*256(=200_000*1024)...");
    for (int i = 0; i < 800_000; i++) {
      bufs4[i] = (POOLED_ALLOCATOR_DIRECT.buffer(size));
    }

    //============== 650_000k used
    try {
      size = 16 * 1024;
      System.out.println("======5.1: apply 2000*16*1024(=32_000*1024)...");
      for (int i = 0; i < 2000; i++) {
        bufs5[i] = (POOLED_ALLOCATOR_DIRECT.buffer(size));
      }
    } catch (Exception e) {
      collector.addError(e);
    } finally {
      //============== free all
      System.out.println("======testSlabClassification2 free all...");
      for (ByteBuf buf : bufs1)
        if (buf != null && buf.refCnt() != 0) buf.release();
      for (ByteBuf buf : bufs2)
        if (buf != null && buf.refCnt() != 0) buf.release();
      for (ByteBuf buf : bufs3)
        if (buf != null && buf.refCnt() != 0) buf.release();
      for (ByteBuf buf : bufs4)
        if (buf != null && buf.refCnt() != 0) buf.release();
      for (ByteBuf buf : bufs5)
        if (buf != null && buf.refCnt() != 0) buf.release();
      System.out.println("======testSlabClassification2 done.");
    }

    //============== 682_000k used
  }


  @Test
  public void testSlabClassification3() {
    //============== sweep all
    bufs1 = new ByteBuf[500_000];
    bufs2 = new ByteBuf[200_000];
    bufs3 = null;
    bufs4 = null;
    bufs5 = null;

    try {
      int size = 0;
      //============== < 800_000k initial free, but not sure exact number
      size = 1024;
      System.out.println("======1.1: apply 500_000*1024...");
      for (int i = 0; i < 500_000; i++) {
        bufs1[i] = (POOLED_ALLOCATOR_DIRECT.buffer(size));
      }

      System.out.println("======1.2: free 250_000*1k bufs...");
      for (int i = 0; i < 500_000; i += 2) {
        bufs1[i].release();
      }

      assertThat(bufs1[250_000].toString().contains("free"), is(true));
      assertThat(bufs1[249_999].toString().contains("free"), is(false));

      //============== 250_000k used
      size = 256;
      System.out.println("======2.1: apply 200_000*256(=50_000*1024)...");
      for (int i = 0; i < 200_000; i++) {
        bufs2[i] = (POOLED_ALLOCATOR_DIRECT.buffer(size));
      }
    } catch (Exception e) {
      collector.addError(e);
    } finally {
      //============== free all
      System.out.println("======testSlabClassification3 free all...");
      for (ByteBuf buf : bufs1)
        if (buf != null && buf.refCnt() != 0) buf.release();
      for (ByteBuf buf : bufs2)
        if (buf != null && buf.refCnt() != 0) buf.release();
      System.out.println("======testSlabClassification3 done.");
    }
  }

}
