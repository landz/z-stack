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

/**
 * Created by jin on 12/26/13.
 */
public class BufferStressTest {

  public static void test1() {
    try (ByteBuffer b = Buffer.create(8);) {
      b.write((byte)0);
    }
  }

  public static void test2() {
    ByteBuffer b = Buffer.create(8);
    b.write((byte)0);
    b.close();
    b = null;
  }

  @Test
  public void testARMStyleClose() {
    int COUNT = 100_000;
    for (int i = 0; i < COUNT; i++) {
      test1();
    }
  }

  @Test
  public void testGCStyle() throws Exception {
    int COUNT = 100_000;
    for (int i = 0; i < COUNT; i++) {
      test2();
    }
    //finalization panic...
//    Thread.sleep(1000L);
  }

}
