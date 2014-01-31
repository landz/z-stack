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

package z.offheap.buffer;

import org.junit.Test;
import z.util.Unsafes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeThat;
import static z.util.Throwables.uncheck;

/**
 */
public class BufferTest {

  @Test
  public void testPrimitives() {
    assumeThat(Unsafes.isArchX86(), is(true));
    System.out.println("start to run under x86 platform.");

    ByteBuffer buffer = Buffer.create(16);

    buffer
        .clear()
        .write((byte) 1)
        .skipWriteTo(2)
        .nativeOrder()
        .writeShort((short)2)
        .writeInt(4)
        .writeLong(-2L);


    long r = buffer.nativeOrder().readLong();
    assertThat(r, is(Long.reverseBytes(0x01_00_02_00_04_00_00_00L)));
    assertThat(r, is(0x00_00_00_04_00_02_00_01L));//little-endian

    r = buffer.nativeOrder().readLong();
    assertThat(r, is(0xFFFF_FFFF_FFFF_FFFEL));

    buffer.close();
    buffer.close();
  }

  @Test
  public void testPrimitivesNetworkOrder() {
    assumeThat(Unsafes.isArchX86(), is(true));
    System.out.println("start to run under x86 platform.");

    ByteBuffer buffer = Buffer.create(16);

    buffer
        .clear()
        .write((byte) 1)
        .skipWriteTo(2)
        .networkOrder()
        .writeShortN((short) 2)
        .writeIntN(4)
        .writeLongN(-2L);


    long r = buffer.networkOrder().readLongN();
    assertThat(r, is(0x01_00_00_02_00_00_00_04L));

    r = buffer.networkOrder().readLongN();
    assertThat(r, is(0xFFFF_FFFF_FFFF_FFFEL));

    buffer.close();
  }


  @Test
  public void testPrimitivesLiitleEndianOrder() {
    assumeThat(Unsafes.isArchX86(), is(true));
    System.out.println("start to run under x86 platform.");

    ByteBuffer buffer = Buffer.create(16);

    buffer
        .clear()
        .write((byte) 1)
        .skipWriteTo(2)
        .littleEndianOrder()
        .writeShortLE((short) 2)
        .writeIntLE(4)
        .writeLongLE(-2L);


    long r = buffer.littleEndianOrder().readLongLE();
    assertThat(r, is(Long.reverseBytes(0x01_00_02_00_04_00_00_00L)));
    assertThat(r, is(0x00_00_00_04_00_02_00_01L));//little-endian

    r = buffer.littleEndianOrder().readLongLE();
    assertThat(r, is(0xFFFF_FFFF_FFFF_FFFEL));

    buffer.close();
  }


  @Test
  public void testPrimitivesMixedOrder() {
    assumeThat(Unsafes.isArchX86(), is(true));
    System.out.println("start to run under x86 platform.");

    ByteBuffer buffer = Buffer.create(16);

    buffer
        .clear()
        .write((byte) 1)
        .skipWriteTo(2)
        .littleEndianOrder()
        .writeShortLE((short)2)
        .writeIntLE(4)
        .networkOrder()
        .writeShortN((short) 0x1234)
        .writeCharN('a')//0x61
        .write((byte)0xab)
        .write((byte)0xcd)
        .nativeOrder()
        .writeChar('b');
    //0x0100_0200_0400_0000 0x1234_0061_ab_cd_62_00

    long r = buffer.networkOrder().readLongN();
    //
    assertThat(r, is(0x0100_0200_0400_0000L));

    r = buffer.networkOrder().readLongN();
    assertThat(r, is(0x1234_0061_ab_cd_62_00L));

    buffer.close();
  }



}
