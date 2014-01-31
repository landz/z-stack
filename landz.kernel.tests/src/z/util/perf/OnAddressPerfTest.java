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

package z.util.perf;

import com.sun.org.apache.xpath.internal.SourceTree;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static z.util.Unsafes.*;

/**
 * Created by jin on 11/21/13.
 */
public class OnAddressPerfTest {
  private static final int RUNS = 1000_000;

  @Test
  public void testOnAddressPerf() {
    long size = 1<<22;//TODO
    long addrChunk4MB = systemAllocateMemory(size);

    long s = System.nanoTime();
    for (int i = 0; i < RUNS; i++) {

      onAddressRun(addrChunk4MB);

    }
    long t = System.nanoTime()-s;
    System.out.printf("cost time: %,d\n", t);

    systemFreeMemory(addrChunk4MB);
  }

  private void onAddressRun(long addrChunk) {
    long end1 =
        onAddress(addrChunk)
            .put(0x12345678)
            .endAddress();
    if (isArchX86()) {
      //LITTLE ENDIAN on x86
      assertThat(UNSAFE.getByte(end1 - 1), is((byte) 0x12));
      assertThat(UNSAFE.getByte(end1 - 2), is((byte) 0x34));
      assertThat(UNSAFE.getByte(end1 - 3), is((byte) 0x56));
      assertThat(UNSAFE.getByte(end1 - 4), is((byte) 0x78));
    }

    long end2 =
        onAddress(end1).self()
            .paddedToNextPageAlignedAddress()
            .endAddress();

    assertThat(isPageAligned(end2),is(true));
//    assertThat(end2 - end1, is(SIZE_PAGE-4L-2*SIZE_ADDRESS));//only valid linux/glibc
    assertThat(end2 - end1, lessThan(SIZE_PAGE - 4L));
    assertThat(UNSAFE.getByte(end2-1), is((byte)0));
    assertThat(UNSAFE.getByte(end2-100), is((byte)0));

    long end3 =
        onAddress(end2).self()
            .paddedBy(4, (byte) 108)
            .endAddress();

    assertThat(UNSAFE.getByte(end3 - 1), is((byte) 108));
    assertThat(UNSAFE.getByte(end3 - 2), is((byte) 108));
    assertThat(UNSAFE.getByte(end3 - 3), is((byte) 108));
    assertThat(UNSAFE.getByte(end3 - 4), is((byte) 108));

    long end4 =
        onAddress(end3)
            .put(0x1234567887654321L)
            .paddedBy(52)
            .paddedToNextNearestCacheLineAlignedAddress()
            .endAddress();

    assertThat(end4, is(end3+60));
    assertThat(UNSAFE.getByte(end4 - 1), is((byte) 0));
    assertThat(UNSAFE.getByte(end4 - 52), is((byte)0));
    assertThat(UNSAFE.getByte(end4 - 53), is((byte)0x12));
  }

}
