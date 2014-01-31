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

import com.google.common.collect.HashMultiset;
import org.junit.Test;
import z.util.Unsafes;
import z.util.primitives.Ints;

import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.LongStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static z.offheap.zmalloc.Allocator.*;
import static z.util.Unsafes.onAddress;
import static z.offheap.zmalloc.AllocatorPrivatesBridge.sizeClassIndex;

public class Base64StressTest {

  @Test
  public void base64Stress() {
    ThreadLocalRandom rnd = ThreadLocalRandom.current();

    dumpGPLTPGlobalStats("0-----before run");

    int count;
    long[] addresses;
    int[]  dstSizes;

    count = 100;
    addresses = new long[count];
    dstSizes  = new int[count];
    runEncoding(rnd, addresses, dstSizes, count);

    dumpGPLTPGlobalStats("1+++++after count "+count);
    dumpTLPStats(HashMultiset.create(Ints.asList(dstSizes)),
        "2=====run with count "+count);


    //free all requested
    LongStream.of(addresses).forEachOrdered((address)->free(address));

    dumpGPLTPGlobalStats("3*****after free count "+count);
    dumpTLPStats(HashMultiset.create(Ints.asList(dstSizes)),
        "4#####run with free count "+ count);

    count = 20;
    addresses = new long[count];
    dstSizes  = new int[count];
    runEncoding(rnd, addresses, dstSizes, count);
    dumpGPLTPGlobalStats("after count "+count);
    dumpTLPStats(HashMultiset.create(Ints.asList(dstSizes)),
        "run with count "+ count);
    LongStream.of(addresses).forEachOrdered((address)->free(address));

  }

  private void runEncoding(ThreadLocalRandom rnd,
                           long[] addresses,
                           int[] dstSizes,
                           int count) {
    for (int i = 0; i < count; i++) {
      int srclength = rnd.nextInt(1,1<<18);
      int dstlength = (srclength+2)/3*4;

      byte[] src = new byte[srclength];
      rnd.nextBytes(src);

      byte[] dst0 = new byte[dstlength];
      byte[] dst1 = new byte[dstlength];

      Base64.getEncoder().encode(src, dst0);
      addresses[i] = ZMBase64.encode(src, dst1);

      dstSizes[i]  = sizeClassIndex(dstlength);

      assertThat(dst1, is(dst0));
    }
  }

  private void dumpTLPStats(HashMultiset<Integer> sizeClassSet, String label) {
    System.out.println("\n==================="+label+"===================");
    sizeClassSet.elementSet().forEach((sizeClass) -> {
      System.out.println("count for sizeClass "+sizeClass+":"+
          sizeClassSet.count(sizeClass));
      System.out.println(
          "current Number Of TLP(current) AvaiablePages for sizeClass " +
              sizeClass+ ": "+
              ManagedPoolStats.currentNumOfTLPAvaiablePages(sizeClass));
    });
  }

  private void dumpGPLTPGlobalStats(String label) {
    System.out.println("\n====================="+label+"=====================");
    System.out.println("current Number Of GP AvaiablePages:"+
        ManagedPoolStats.currentNumOfGPAvaiablePages());
    System.out.println("current Number Of TLPs:"+
        ManagedPoolStats.currentNumOfTLPs());
    System.out.println("current Number Of TLP(current) FreePages:"+
        ManagedPoolStats.currentNumOfTLPFreePages());
  }


  /**
   * NOTE: This Base64 is borrowed from Josh Bloch's j.u.p.Base64
   *
   */
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


    private static long encode(byte[] a, byte[] result) {
      int aLen = a.length;
      int numFullGroups = aLen / 3;
      int numBytesInPartialGroup = aLen - 3 * numFullGroups;
      int resultLen = 4 * ((aLen + 2) / 3);
      long resultAddress = allocate(resultLen);

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
//      Allocator.free(resultAddress);
      return resultAddress;
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
