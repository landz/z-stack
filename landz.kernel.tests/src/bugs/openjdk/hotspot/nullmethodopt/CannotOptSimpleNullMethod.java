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

package bugs.openjdk.hotspot.nullmethodopt;

import z.offheap.buffer.Buffer;
import z.util.*;

import java.util.function.BooleanSupplier;

import static bugs.openjdk.hotspot.nullmethodopt.DoNothing.doNothing;
import static z.util.Throwables.uncheck;

import static z.util.Throwables.uncheckTo;
import static z.util.Contracts.*;

public class CannotOptSimpleNullMethod {
  public static final int COUNT = 500_000_000;

  public static void main(String[] args) throws Exception {
    new CannotOptSimpleNullMethod().dotest();
  }

  public void dotest() throws Exception {
    long s ,t;
    int r = 0;

    //warmup
    for (int i = 0; i < 1000_000; i++) {
      m1(i);
      m2(i);
    }

    System.out.println("start...");

    System.gc();
    Thread.sleep(1000);

    r = 0;
    s = System.nanoTime();
    for (int i = 0; i < COUNT; i++) {
      r += m1(i);
    }
    t = System.nanoTime() - s;
    System.out.printf("time cost of m1: %,d with result: %d\n", t,r);


    System.gc();
    Thread.sleep(1000);

    r = 0;
    s = System.nanoTime();
    for (int i = 0; i < COUNT; i++) {
      r += m2(i);
    }
    t = System.nanoTime() - s;
    System.out.printf("time cost of m2: %,d with result: %d\n", t,r);
  }

  public int m1(int a) {
//    doNothing(null);
//    int b = a;
//    b -= 32;
//    b *= 15;
//    b /= 3;
    return (a-32)*5;
  }

  public int m2(int a) {
//    donothing(null);

//    int b = a;
//    b -= 32;
//    b *= 15;
//    b /= 3;

    return (a-32)*5;
  }

}
