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

package z.util.primitives;

import org.junit.Test;
import z.testware.common.Stopwatch;

import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class IntsPerfTest {

  private static final int COUNT = 10_000_000;

  @Test
  public void testfromBytesPerf() {
    byte[] ba = new byte[4];
    ThreadLocalRandom.current().nextBytes(ba);
    byte b1 = ba[0];
    byte b2 = ba[1];
    byte b3 = ba[2];
    byte b4 = ba[3];

    long rt1 = 0;
    long s = System.nanoTime();
    for (int i = 0; i < COUNT; i++) {
       rt1 += IntsPerfTest.fromBytes(b1, b2, b3, b4);
    }
    long t = System.nanoTime() - s;
    System.out.println(t);

    long rt2 = 0;
    s = System.nanoTime();
    for (int i = 0; i < COUNT; i++) {
      rt2 += IntsPerfTest.fromBytes(b1, b2, b3, b4);
    }
    t = System.nanoTime() - s;
    System.out.println(t);

    assertThat(rt1,is(rt2));
  }

  private static int fromBytes(byte b1, byte b2, byte b3, byte b4) {
    return b1 << 24 | (b2 & 0xFF) << 16 | (b3 & 0xFF) << 8 | (b4 & 0xFF);
  }

}
