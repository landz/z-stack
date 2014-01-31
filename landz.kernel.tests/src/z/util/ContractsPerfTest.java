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

package z.util;

import org.junit.Test;
import z.exception.ContractViolatedException;

import java.util.function.BooleanSupplier;

import static z.util.Throwables.uncheck;

public class ContractsPerfTest {

  public static final int COUNT = 200_000_000;

  @Test
  public void testContractLambda() {
    long s ,t;
    int r = 0;
//    int AL = 128;
//    int a[] = new int[AL];
//    ThreadLocalRandom rnd = ThreadLocalRandom.current();
//    for (int i = 0; i < AL; i++) {
//      a[i] = rnd.nextInt(1,1023);
//    }

    //warmup all
    for (int i = 0; i < 100_000; i++) {
      contractLandz(i);
      contractPlain(i);//a[i%AL]
      noContract(i);
    }

    //==============================
    System.gc();
    uncheck(()->Thread.sleep(1000));

    r = 0;
    s = System.nanoTime();
    for (int i = 0; i < COUNT; i++) {
      r += contractLandz(i);
    }
    t = System.nanoTime() - s;
    System.out.printf("time cost of contractLandz: %,d with result: %d\n", t,r);

    //==============================
    System.gc();
    uncheck(()->Thread.sleep(1000));

    r = 0;
    s = System.nanoTime();
    for (int i = 0; i < COUNT; i++) {
      r += contractPlain(i);
    }
    t = System.nanoTime() - s;
    System.out.printf("time cost of contractPlain: %,d with result: %d\n", t,r);

    //==============================
    System.gc();
    uncheck(()->Thread.sleep(1000));

    r = 0;
    s = System.nanoTime();
    for (int i = 0; i < COUNT; i++) {
      r += noContract(i);
    }
    t = System.nanoTime() - s;
    System.out.printf("time cost of noContract: %,d with result: %d\n", t,r);

  }


  private static int contractLandz(int a) {
    contract(() ->
        a >= 0 && (Math.sin(Math.sqrt(a)) != Integer.MAX_VALUE) );
    return ((a-16)*12345)%123+5;
  }

  private static int contractPlain(int a) {
    if (a<0 || (Math.sin(Math.sqrt(a)) == Integer.MAX_VALUE) )
      throw new IllegalStateException();
    return ((a-16)*12345)%123+5;
  }

  private static int noContract(int a) {
    return ((a-16)*12345)%123+5;
  }

//  public static void dummy(BooleanSupplier dummy){}


  private static final boolean TRICK = false;

  public static void contract(BooleanSupplier contractSupplier) {
    if (TRICK && !contractSupplier.getAsBoolean())
      throw new ContractViolatedException("[Contract Breached]: " +
          "the contract "+ contractSupplier + " fails to be kept!");
  }


}
