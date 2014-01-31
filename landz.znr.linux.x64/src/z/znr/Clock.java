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

import z.util.Unsafes;

import java.lang.invoke.MethodHandle;

import static jnr.x86asm.Asm.*;
import static z.znr.MethodHandles.asm;

public class Clock {
  //rdtsc
  private static final MethodHandle mh_rdtsc = MethodHandles.asm(
      long.class,
      // param regs are %rdi, %rsi, %rdx, %rcx, %r8 and %r9
      a -> {
        a.xor_(rax, rax);//need?
        a.rdtsc();
        a.shl(rdx, imm(32));
        a.or_(rax, rdx);
        a.ret();
      }
  );

  /**
   * hardware counter with a small JNI overhead, which may has a larger
   * instruction throughput than {@link System#nanoTime()} in some
   * cases. But, please make sure you know all kinds of it's pitfall
   * (http://lwn.net/Articles/388188/).
   * <p>
   * Note:
   * it is better to use this hardware counter with {@link Affinity},
   * although invariant TSC will mitigate this. So only recommend to only use
   * in the benchmark environment.
   * <p>see more, http://en.wikipedia.org/wiki/Time_Stamp_Counter and/or Intel
   * ASDM.
   */
  public static final long rdtsc(){
    try {
      return (long)mh_rdtsc.invokeExact();
    } catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    }
  }

  private static final long ESTIMATED_TSC_FREQUENCY = estimateTSCFrequency();
  private static final long TSC_LOADTIME            = rdtsc();
  private static final long PARK_TIME               = 1000_000_000L;

  //rdtscp
  private static final MethodHandle mh_rdtscp = MethodHandles.asm(
      long.class,
      // param regs are %rdi, %rsi, %rdx, %rcx, %r8 and %r9
      a -> {
        a.xor_(rax, rax);//need?
        a.rdtscp();
        a.shl(rdx, imm(32));
        a.or_(rax, rdx);
        a.ret();
      }
  );

  /**
   * serializing variant of rdtsc. see more {@link #rdtsc()}.<p>
   * Note: only available for Nehalem+.
   */
  public static final long rdtscp(){
    try {
      return (long)mh_rdtscp.invokeExact();
    } catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    }
  }

  /**
   * TODO: only works for invariant TSC?
   */
  public static final long estimateTSCFrequency() {
    long tsc = rdtsc();
    Unsafes.UNSAFE.park(false,PARK_TIME);
    return rdtsc()-tsc;
  }

  /**
   * Note: there will be less computation if using {@link #tscToNano}.
   *
   * @return returned nanoseconds are relative to some time point of the class
   * {@link Clock} loading.
   */
  public static final long nanoTSCTime(){
    try {
      return
          ((long)mh_rdtsc.invokeExact() -TSC_LOADTIME)
              *PARK_TIME/ESTIMATED_TSC_FREQUENCY;
    } catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    }
  }

  /**
   * convert TSC number to nanoseconds by estimated TSC frequency
   */
  public static final long tscToNano(long tsc){
    return tsc*PARK_TIME/ESTIMATED_TSC_FREQUENCY;
  }


}
