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

package z.znr;

import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.lang.invoke.MethodHandle;

import static jnr.x86asm.Asm.*;
import static z.znr.MethodHandles.asm;

/**
 * Created by jin on 11/3/13.
 */
public class MethodHandlesTest {
  static {
    System.setProperty("jnr.invoke.compile.dump", "true");
  }

  private static final MethodHandle ADD = asm(
      long.class, long.class, long.class,
      a -> {
//        a.lea(rax, ptr(rdx, rcx,0,0));
        a.add(rdx, rcx);
        a.mov(rax, rdx);
        a.ret();
      }
  );

  @Test
  public void testAdd() throws Throwable {
    int RUNS = 100_000_000;

    long sum = 0;
//    sum = (long)ADD.invokeExact(12L,21L);//warmup
    long s = System.nanoTime();
    for (int i = 0; i < RUNS; i++) {
      sum = (long)ADD.invokeExact(12L,21L);
    }
    long t = System.nanoTime()-s;
    assertThat(sum, is(33L));
    System.out.println(
        "run "+RUNS+" times of ADD cost: "+t+" nanos");
    System.out.println(
        "Or one shot cost of ADD is: "+ t*1.0/RUNS +" nanos");
    //my laptop: ~8.6ns, pure jni way gives ~8.2ns in the same config
  }
}
