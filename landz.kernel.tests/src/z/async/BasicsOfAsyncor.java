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
//
//import java.util.concurrent.atomic.AtomicLong;
//
//import static z.async.Asyncors.async;
//
//public class BasicsOfAsyncor {
//  public static final int COUNT = 30_000_000;
//
//  public static final AtomicLong ct = new AtomicLong();
//
//  @Test
//  public void testIExternalInvoke() {
//    int parallelism = 7;
//
//    long s = System.nanoTime();
//    for (int i = 0; i < COUNT; i++) {
//      async(() -> ct.incrementAndGet());
//    }
//    long t1 = System.nanoTime()-s;
//
//    while (ct.get()!=COUNT) {}
//    long t2 = System.nanoTime()-s;
//
//    System.out.printf("costed %,d to submit %,d tasks, and %,d to complete\n",
//        t1, COUNT, t2);
//
//    System.out.println("done");
//  }
//
//}
