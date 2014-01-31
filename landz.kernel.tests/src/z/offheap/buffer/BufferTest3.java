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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.junit.Test;
import z.util.Unsafes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeThat;

/**
 * more contrast with Netty ByteBuf
 */
public class BufferTest3 {

  @Test
  public void sanitycheck() {
    System.out.println("start to sanity check...");

    ByteBuffer buffer = Buffer.create(5*8);

    ByteBuf nettyBuf = PooledByteBufAllocator.DEFAULT.directBuffer(5*8);

    buffer
        .clear()
        .write((byte) 1)
        .skipWriteTo(4)
        .networkOrder()
        .writeShortN((short) 2)
        .writeIntN(12345)
        .write((byte) 123)
        .writeCharN('[')
        .writeFloatN(3.14159f)
        .writeDoubleN(1.9999d)
        .writeLongN(-2L)
        .write((byte)-1)
        .writeCharN(']')
        .writeIntN(Integer.MIN_VALUE);
    //4+2+4+1+2+4+8+8+1+2
    //8+8+8+8+4 -> 5*Long


    nettyBuf.clear()
        .writeByte(1)
        .writeByte(0).writeByte(0).writeByte(0)
        .writeShort(2)
        .writeInt(12345)
        .writeByte(123)
        .writeChar('[')
        .writeFloat(3.14159f)
        .writeDouble(1.9999d)
        .writeLong(-2L)
        .writeByte(-1)
        .writeChar(']')
        .writeInt(Integer.MIN_VALUE);


    long r1 = buffer.networkOrder().readLongN();
    long r2 = nettyBuf.readLong();
    assertThat(r1, is(r2));

    r1 = buffer.networkOrder().readLongN();
    r2 = nettyBuf.readLong();
    assertThat(r1, is(r2));

    r1 = buffer.networkOrder().readLongN();
    r2 = nettyBuf.readLong();
    assertThat(r1, is(r2));

    r1 = buffer.networkOrder().readLongN();
    r2 = nettyBuf.readLong();
    assertThat(r1, is(r2));

    r1 = buffer.networkOrder().readLongN();
    r2 = nettyBuf.readLong();
    assertThat(r1, is(r2));


    buffer.close();
  }


}
