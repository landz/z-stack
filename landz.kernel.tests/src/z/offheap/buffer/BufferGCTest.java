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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static z.util.Throwables.uncheck;

/**
 */
public class BufferGCTest {

  @Test
  public void testPrimitivesARMAndGC() {

    try (ByteBuffer buffer = Buffer.create(16)) {
      buffer
          .clear()
          .write((byte)1)
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
    }

//    buffer.close(); //ouf-of-scope

    System.gc();
    uncheck(()-> Thread.sleep(3000L));
  }


  @Test
  public void testPrimitivesARMAndGC2() {
    armAndgc();
    System.gc();
    uncheck(()-> Thread.sleep(3000L));
  }

  private void armAndgc() {
    try (ByteBuffer buffer = Buffer.create(16)) {
      buffer
          .clear()
          .write((byte)1)
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
    }

//    buffer.close(); //ouf-of-scope

    System.gc();
    uncheck(()-> Thread.sleep(3000L));
  }


  @Test
  public void testPrimitivesGC() {
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

    buffer = null;

    System.gc();
    uncheck(()-> Thread.sleep(3000L));
  }



}
