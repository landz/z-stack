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

package z.util;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static z.util.Unsafes.*;
import static z.util.Unsafes.currentThreadId;

public class UnsafesTest {

    static class A {
        public int a;
    }

    public static final int COUNT = 1000_000_000;

    @Test
    public void testUNSAFE() {
        Field testField = Throwables.uncheckTo(() -> A.class.getDeclaredField("a"));
        long testFieldOffset = UNSAFE.objectFieldOffset(testField);
        A test = new A();
        test.a = 123;

        int result;
        result = UNSAFE.getInt(test,testFieldOffset);
        assertThat(result, is(123));

        test.a = 456;
        result = UNSAFE.getInt(test,testFieldOffset);
        assertThat(result, is(456));
    }


  @Test
  public void testNextNearestPageAlignedAddress() {
    assertThat(nextNearestPageAlignedAddress(0L), is(0L));
    assertThat(nextNearestPageAlignedAddress(64L), is((long)SIZE_PAGE));
    assertThat(nextNearestPageAlignedAddress((1L<<21)-1), is(1L<<21));
  }

  @Test
  public void testNextNearestCacheLineAlignedAddress() {
    assertThat(nextNearestCacheLineAlignedAddress(0L), is(0L));
    assertThat(SIZE_CACHE_LINE, is(64));
    assertThat(nextNearestCacheLineAlignedAddress(32L), is(64L));
    assertThat(nextNearestCacheLineAlignedAddress(128L), is(128L));
    assertThat(nextNearestCacheLineAlignedAddress(1023L), is(1024L));
  }

  @Test
  public void testNextNearestMachineWordAlignedAddress() {
    assertThat(nextNearestMachineWordAlignedAddress(0L), is(0L));
    assertThat(nextNearestMachineWordAlignedAddress(7L), is(8L));
    assertThat(nextNearestMachineWordAlignedAddress(32L), is(32L));
    assertThat(nextNearestMachineWordAlignedAddress(2046L), is(2048L));
  }

  @Test
  public void testOnAddress() {
    long size = 1<<22;
    long addrChunk4MB = systemAllocateMemory(size);

    long end1 =
        onAddress(addrChunk4MB)
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

    systemFreeMemory(addrChunk4MB);
  }

  @Test
  public void testOnAddressToByteCode() {
    long addrChunk8B = systemAllocateMemory(8);

    OnAddressFollowBy addr = onAddress(addrChunk8B).self();

    addr.followBy((byte)12)
        .followBy((byte)34)
        .followBy((byte)56)
        .followBy((byte)78);

    byte[] rt = addr.toByteArray();
    assertThat(rt, is(new byte[]{12,34,56,78}));
    systemFreeMemory(addrChunk8B);

    //0 bytes
    addrChunk8B = systemAllocateMemory(8);
    rt = onAddress(addrChunk8B).self().toByteArray();
    assertThat(rt, is(new byte[0]));
    systemFreeMemory(addrChunk8B);
  }


  @Test
  public void testGetCurrentThreadId() {
    assertThat(currentThreadId(),greaterThanOrEqualTo(0L));
  }

  @Test
  public void testClearOffHeap() {
    long addr = systemAllocateMemory(4);
    UNSAFE.putInt(addr, 0x12345678);
    systemClearMemory(addr, 4);
    assertThat(UNSAFE.getInt(addr), is(0));
    systemFreeMemory(addr);


    addr = systemAllocateMemory(16);
    UNSAFE.putLong(addr, 0x1234567812345678L);
    UNSAFE.putLong(addr+8, 0x1234567812345678L);
    systemClearMemory(addr, 16);
    assertThat(UNSAFE.getInt(addr+6),is(0));
    assertThat(UNSAFE.getInt(addr+12),is(0));
    systemFreeMemory(addr);

    addr = systemAllocateMemory(64);
    UNSAFE.putLong(addr+32, 0x1234567812345678L);
    systemClearMemory(addr, 64);
    assertThat(UNSAFE.getInt(addr+33),is(0));
    systemFreeMemory(addr);
  }

}
