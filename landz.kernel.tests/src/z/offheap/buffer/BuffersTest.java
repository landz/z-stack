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

import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static z.offheap.buffer.Buffers.*;
import static z.util.Unsafes.*;

/**
 */
public class BuffersTest {

  @Test
  public void testPrimitives() {
    long a = systemAllocateMemory(16);

    put(a,(byte)1);
    putShort(a+2, (short)2);
    putInt(a+4, 4);

    putLong(a+8, -2L);

    assertThat(getLong(a), is(Long.reverseBytes(0x01_00_02_00_04_00_00_00L)));
    assertThat(getLong(a), is(0x00_00_00_04_00_02_00_01L));//little-endian

    assertThat(getLong(a+8), is(0xFFFF_FFFF_FFFF_FFFEL));

    systemFreeMemory(a);
  }


}
