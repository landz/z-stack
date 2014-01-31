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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import org.junit.Test;
import z.offheap.zmalloc.Allocator;
import z.offheap.zmalloc.AllocatorPrivatesBridge;
import z.util.SystemProperty;
import z.util.Unsafes;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static z.util.Unsafes.onAddress;

/**
 * note: this test is just for basic evaluation.
 *
 * Algorithms of JU, netty and ZM are different to each others.So the result
 * can not compared directly.
 *
 */
public class Base64BenchmarkTest {

  private static final int COUNT = 5000_000;//2000_000

  private static final ByteBufAllocator NETTY_POOLED_ALLOCATOR_DIRECT
      = new PooledByteBufAllocator(true);

  @Test
  public void base64Benchmark() {
    System.setProperty("io.netty.noResourceLeakDetection","true");//netty bug?

    byte[] src = new byte[6];

    byte[] dst0 = new byte[8];
    byte[] dst1 = new byte[8];
    byte[] dst2 = new byte[8];
    byte[] dst3 = new byte[8];

    byte[] back0 = new byte[6];
    byte[] back1 = new byte[6];
    byte[] back2 = new byte[6];
    byte[] back3 = new byte[6];

    ThreadLocalRandom.current().nextBytes(src);

    long s, t;

    s = System.nanoTime();
    for (int i = 0; i < COUNT; i++) {
      Base64.getEncoder().encode(src, dst0);
      Base64.getDecoder().decode(dst0, back0);
    }
    t = System.nanoTime() - s;
    System.out.println("testJUBase64Raw: " + t);

    s = System.nanoTime();
    for (int i = 0; i < COUNT; i++) {
      testJUBase64NIOEncode(src, dst1);
      testJUBase64NIODecode(dst1, back1);
    }
    t = System.nanoTime() - s;
    System.out.println("testJUBase64NIO: " + t);

    s = System.nanoTime();
    for (int i = 0; i < COUNT; i++) {
      testNettyBase64Encode(src, dst2);
      testNettyBase64Decode(dst2, back2);
    }
    t = System.nanoTime() - s;
    System.out.println("testNettyBase64: " + t);

    s = System.nanoTime();
    for (int i = 0; i < COUNT; i++) {
      ZMBase64.encode(src, dst3);
      back3 = ZMBase64.decode(dst3);
    }
    t = System.nanoTime() - s;
    System.out.println("ZMBase64       : " + t);

    assertThat(dst1, is(dst0));
    assertThat(dst2, is(dst1));
    assertThat(dst3, is(dst2));

    assertThat(src, is(back0));
    assertThat(src, is(back1));
    assertThat(src, is(back2));
    assertThat(src, is(back3));
  }


  private void testJUBase64NIOEncode(byte[] src, byte[] dst) {
    Base64.getEncoder().encode(src, dst);
  }

  private void testJUBase64NIODecode(byte[] src, byte[] dst) {
    Base64.getDecoder().decode(src, dst);
  }

  private void testNettyBase64Encode(byte[] src, byte[] dst) {
    ByteBuf srcBuf = NETTY_POOLED_ALLOCATOR_DIRECT.directBuffer(6);
    srcBuf.writeBytes(src);
    ByteBuf dstBuf = io.netty.handler.codec.base64.Base64.encode(srcBuf);
    dstBuf.readBytes(dst);
  }

  private void testNettyBase64Decode(byte[] src, byte[] dst) {
    ByteBuf srcBuf = NETTY_POOLED_ALLOCATOR_DIRECT.directBuffer(8);
    srcBuf.writeBytes(src);
    ByteBuf dstBuf = io.netty.handler.codec.base64.Base64.decode(srcBuf);
    dstBuf.readBytes(dst);
  }

  static class ZMBase64 {

    /**
     * This array is a lookup table that translates 6-bit positive integer
     * index values into their "Base64 Alphabet" equivalents as specified
     * in Table 1 of RFC 2045.
     */
    private static final char intToBase64[] = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };


    private static void encode(byte[] a, byte[] result) {
      int aLen = a.length;
      int numFullGroups = aLen / 3;
      int numBytesInPartialGroup = aLen - 3 * numFullGroups;
      int resultLen = 4 * ((aLen + 2) / 3);
      long resultAddress = Allocator.allocate(resultLen);

      // Translate all full groups from byte array elements to Base64
      Unsafes.OnAddressFollowBy resultOnAddress =
          onAddress(resultAddress).self();

      int inCursor = 0;
      for (int i = 0; i < numFullGroups; i++) {
        int byte0 = a[inCursor++] & 0xff;
        int byte1 = a[inCursor++] & 0xff;
        int byte2 = a[inCursor++] & 0xff;
        resultOnAddress
                .followBy((byte) intToBase64[byte0 >> 2])
                .followBy((byte) intToBase64[(byte0 << 4) & 0x3f | (byte1 >> 4)])
                .followBy((byte) intToBase64[(byte1 << 2) & 0x3f | (byte2 >> 6)])
                .followBy((byte) intToBase64[byte2 & 0x3f]);
      }

      // Translate partial group if present
      if (numBytesInPartialGroup != 0) {
        int byte0 = a[inCursor++] & 0xff;
        resultOnAddress.followBy((byte)intToBase64[byte0 >> 2]).endAddress();
        if (numBytesInPartialGroup == 1) {
          resultOnAddress.followBy((byte) intToBase64[(byte0 << 4) & 0x3f]);
          resultOnAddress.paddedBy(2, (byte) '=');
        } else {
          // assert numBytesInPartialGroup == 2;
          int byte1 = a[inCursor++] & 0xff;
          resultOnAddress.followBy((byte) intToBase64[(byte0 << 4) & 0x3f | (byte1 >> 4)]);
          resultOnAddress.followBy((byte) intToBase64[(byte1 << 2) & 0x3f]);
          resultOnAddress.followBy((byte) '=');
        }
      }
      // assert inCursor == a.length;
      // assert result.length() == resultLen;
      resultOnAddress.toByteArray(result);
      //Don't forget free!
      Allocator.free(resultAddress);
    }



    /**
     * This array is a lookup table that translates unicode characters
     * drawn from the "Base64 Alphabet" (as specified in Table 1 of RFC 2045)
     * into their 6-bit positive integer equivalents.  Characters that
     * are not in the Base64 alphabet but fall within the bounds of the
     * array are translated to -1.
     */
    private static final byte base64ToInt[] = {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54,
        55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4,
        5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
        24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34,
        35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
    };

    private static byte[] decode(byte[] src) {
      int sLen = src.length;
      int numGroups = sLen / 4;
      if (4 * numGroups != sLen)
        throw new IllegalArgumentException(
            "String length must be a multiple of four.");
      int missingBytesInLastGroup = 0;
      int numFullGroups = numGroups;
      if (sLen != 0) {
        if (src[sLen - 1] == '=') {
          missingBytesInLastGroup++;
          numFullGroups--;
        }
        if (src[sLen - 2] == '=')
          missingBytesInLastGroup++;
      }
      byte[] result = new byte[3 * numGroups - missingBytesInLastGroup];

      // Translate all full groups from base64 to byte array elements
      int inCursor = 0, outCursor = 0;
      for (int i = 0; i < numFullGroups; i++) {
        int ch0 = base64ToInt[src[inCursor++]];
        int ch1 = base64ToInt[src[inCursor++]];
        int ch2 = base64ToInt[src[inCursor++]];
        int ch3 = base64ToInt[src[inCursor++]];
        result[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));
        result[outCursor++] = (byte) ((ch1 << 4) | (ch2 >> 2));
        result[outCursor++] = (byte) ((ch2 << 6) | ch3);
      }

      // Translate partial group, if present
      if (missingBytesInLastGroup != 0) {
        int ch0 = base64ToInt[src[inCursor++]];
        int ch1 = base64ToInt[src[inCursor++]];
        result[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));

        if (missingBytesInLastGroup == 1) {
          int ch2 = base64ToInt[src[inCursor++]];
          result[outCursor++] = (byte) ((ch1 << 4) | (ch2 >> 2));
        }
      }
      // assert inCursor == s.length()-missingBytesInLastGroup;
      // assert outCursor == result.length;
      return result;
    }

  }

}
