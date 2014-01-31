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

package z.offheap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import z.testware.common.Stopwatch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static z.util.Unsafes.*;

import java.util.concurrent.ThreadLocalRandom;

/**
 * NOTE: for UNSAFE.allocateMemory,
 * "The contents of the memory are uninitialized;
 *   they will generally be garbage."
 *
 */
public class DirectVsIndirectIndexingPerfTest {

  private static final int COUNT = 10_000_000;
  private static final int NUM_CHUNKS = 4096;
  private static final int CHUNK_SIZE = 512;
  private static final int CHUNK_ELEMENT = 5;

  long[] chunks = new long[NUM_CHUNKS];
  long singleBluk = 0;

  @Before
  public void setup() {
    for (int i = 0; i < NUM_CHUNKS; i++) {
      chunks[i] = UNSAFE.allocateMemory(CHUNK_SIZE);
      UNSAFE.setMemory(chunks[i],(long)CHUNK_SIZE,(byte)CHUNK_ELEMENT);
    }
    singleBluk = UNSAFE.allocateMemory(CHUNK_SIZE * NUM_CHUNKS);
    UNSAFE.setMemory(singleBluk,(long)CHUNK_SIZE * NUM_CHUNKS,(byte)CHUNK_ELEMENT);
  }

  @After
  public void clean() {
    for (int i = 0; i < NUM_CHUNKS; i++) {
      UNSAFE.freeMemory(chunks[i]);
    }
    UNSAFE.freeMemory(singleBluk);
  }

  @Test
  public void testRead() {
    System.out.println("testRead:");
    int[] offset = new int[COUNT];
    for (int i = 0; i < COUNT; i++) {
      offset[i] = ThreadLocalRandom.current().nextInt(CHUNK_SIZE * NUM_CHUNKS);
    }

    int sum2 = 0;
    Stopwatch watch2 = Stopwatch.create("for indirect indexing");
    watch2.start();
    for (int i = 0; i < COUNT; i++) {
      sum2 += UNSAFE.getByte(chunks[(offset[i]>>>9)]+(offset[i]&0x01FF));
    }
    watch2.stop();
    watch2.printMicros();
//    System.out.println("sum2 is "+sum2);

    int sum1 = 0;
    Stopwatch watch1 = Stopwatch.create("for direct indexing");
    watch1.start();
    for (int i = 0; i < COUNT; i++) {
      sum1 += UNSAFE.getByte(singleBluk+offset[i]);
    }
    watch1.stop();
    watch1.printMicros();
//    System.out.println("sum1 is "+sum1);

    assertThat(sum1, is(CHUNK_ELEMENT*COUNT));
    assertThat(sum2, is(CHUNK_ELEMENT*COUNT));
  }

  @Test
  public void testReadAndWrite() {
    System.out.println("testReadAndWrite:");
    int[] offset = new int[COUNT];
    for (int i = 0; i < COUNT; i++) {
      offset[i] = ThreadLocalRandom.current().nextInt(CHUNK_SIZE * NUM_CHUNKS);
    }


    /////////////////////////////////////////////////////////////////////
    int sum1 = 0;
    Stopwatch watch1 = Stopwatch.create("for direct indexing");
    watch1.start();
    for (int i = 0; i < COUNT; i++) {
      long address = singleBluk+offset[i];
      byte b = UNSAFE.getByte(address);
      sum1 += b;
      UNSAFE.putByte(address, (byte) (b * 3));
    }
    watch1.stop();
    watch1.printMicros();
//    System.out.println("sum1 is "+sum1);

    /////////////////////////////////////////////////////////////////////
    int sum2 = 0;
    Stopwatch watch2 = Stopwatch.create("for indirect indexing");
    watch2.start();
    for (int i = 0; i < COUNT; i++) {
      long address = chunks[(offset[i]>>>9)]+(offset[i]&0x01FF);
      byte b = UNSAFE.getByte(address);
      sum2 += b;
      UNSAFE.putByte(address, (byte)(b*3));
    }
    watch2.stop();
    watch2.printMicros();
//    System.out.println("sum2 is "+sum2);

    assertThat(sum1, is(sum2));
  }


}
