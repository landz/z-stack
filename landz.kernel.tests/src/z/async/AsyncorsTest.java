///**
// * Copyright 2013, Landz and its contributors. All rights reserved.
// *
// *    Licensed under the Apache BuiltinLicense, Version 2.0 (the "BuiltinLicense");
// *    you may not use this file except in compliance with the BuiltinLicense.
// *    You may obtain a copy of the BuiltinLicense at
// *
// *        http://www.apache.org/licenses/LICENSE-2.0
// *
// *    Unless required by applicable law or agreed to in writing, software
// *    distributed under the BuiltinLicense is distributed on an "AS IS" BASIS,
// *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *    See the BuiltinLicense for the specific language governing permissions and
// *    limitations under the BuiltinLicense.
// */
//
//package z.async;
//
//import org.junit.Test;
//import z.util.primitives.Ints;
//
//import java.util.concurrent.ThreadLocalRandom;
//
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.is;
//import static z.async.Asyncors.async;
//import static z.util.Throwables.uncheck;
//
///**
// * Created by jin on 7/31/13.
// */
//public class AsyncorsTest {
//
//  volatile int x = 0;
//
//  @Test
//  public void sanityCheck() {
//
//    async(() -> {
////      uncheck(() -> Thread.sleep(100));
//      assertThat(x,is(0));
//      x = 2;
//    });
//    assertThat(x, is(0));
//
////    uncheck(() -> Thread.sleep(200_000));
//    assertThat(x, is(2));
//  }
//
//  @Test
//  public void testOnCondition() {
//    int COUNT = 100;
//    ThreadLocalRandom rnd = ThreadLocalRandom.current();
//    int[] a = new int[COUNT];
//    for (int i = 0; i < COUNT; i++) {
//        a[i] = rnd.nextInt(0,10);
//    }
//
//    int[] b = new int[COUNT];
//    for (int i = 0; i < COUNT; i++) {
//      b[i] = rnd.nextInt(100,109);
//    }
//
//    message(int.class).
//    async(() -> {
//      int v = Ints.max(a);
//      send(v);
//    });
//
//    message(int.class).
//    async(() -> {
//      int b = Ints.max(a);
//    }).to();
//
//    onPrimitiveMessage(pm->pm&FFF0000L==123).
//    async((Result ar, Result br) -> {
//      int am = ar.value;
//      int bm = br.value;
//      int v = am > bm ? am:bm;
//      System.out.printf("Rhe max value in array a and b is %d\n", v);
//    });
//
//  }
//
//  class Result {
//    int value;
//    public long encode(int result){
//
//    }
//  }
//
//
//
//}
