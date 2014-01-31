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


import z.evil.Intrinsics;
import z.znr.invoke.linux.x64.NativeLibrary;

import java.lang.invoke.MethodHandle;

import static jnr.x86asm.Asm.*;
import static z.znr.MethodHandles.asm;
import static z.util.Unsafes.*;
import static z.util.Throwables.*;
import static z.offheap.zmalloc.Allocator.*;

/**
 * Functions  from LibC
 */
public class LibC {

  static {
    NativeLibrary.loadLibrary("c");
    Intrinsics.warmup();//TODO:?
  }

  //getpid
  private static final MethodHandle mh_getpid = asm(int.class, a ->
      a.jmp(imm(NativeLibrary.findSymbolAddress("getpid")))
  );

  public static int getpid() {
    try {
      return (int) mh_getpid.invokeExact();
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  //gettimeofday
  private static final MethodHandle mh_gettimeofday = asm(
      int.class, long.class,
      // param regs are %rdi, %rsi, %rdx, %rcx, %r8 and %r9
      a -> {
        a.mov(rdi, rdx);
        a.jmp(imm(NativeLibrary.findSymbolAddress("gettimeofday")));
      }
  );

  public static int gettimeofday(Timeval timeval) {
    try {
      long addrBuffer = timeval.addrBuffer();

      int rt = (int)mh_gettimeofday.invokeExact(addrBuffer);
      UNSAFE.putLong(timeval,Timeval.offset_tv_sec,
          UNSAFE.getLong(addrBuffer));
      UNSAFE.putLong(timeval,Timeval.offset_tv_usec,
          UNSAFE.getLong(addrBuffer+8));

      return rt;
    } catch (Throwable t) {
//      t.printStackTrace();
      return -1;
    }
  }


  //TODO: use landz.buffer in the future?
  public static final class Timeval {
    long tv_sec, tv_usec;

    private final long buffer;

    public Timeval() {
      buffer  = allocate(16);
    }

    public long addrBuffer() {
      return buffer;
    }

    static final long offset_tv_sec;
    static final long offset_tv_usec;
    static {
      offset_tv_sec = uncheckTo(()->
          UNSAFE.objectFieldOffset
            (Timeval.class.getDeclaredField("tv_sec"))
      );
      offset_tv_usec = uncheckTo(()->
          UNSAFE.objectFieldOffset
            (Timeval.class.getDeclaredField("tv_usec"))
      );
    }

    @Override
    public void finalize() {
      if (buffer!=0L)
        free(buffer);
//      System.out.println("buffer freed!");
    }
  }

  //sched_getaffinity
  private static final MethodHandle mh_sched_getaffinity = asm(
      int.class, int.class, long.class, long.class,
      a -> {// param regs are %rdi, %rsi, %rdx, %rcx, %r8 and %r9
        a.mov(rdi, rdx);//pid
        a.mov(rsi, rcx);//cpusetsize
        a.mov(rdx, r8);//*mask
        a.jmp(imm(NativeLibrary.findSymbolAddress("sched_getaffinity")));
      }

  );

  public static int getAffinity(int pid, Cpuset mask) {
    try {
      return (int)mh_sched_getaffinity.invokeExact(
          pid, Cpuset.SIZE_CPUSET_TYPE, mask.addrBuffer());
    } catch (Throwable t) {
//      t.printStackTrace();
      return -1;
    }
  }

  //sched_setaffinity
  /**
   * fixme: tested via {@link Affinity#bindTo}
   */
  private static final MethodHandle mh_sched_setaffinity = asm(
      int.class, int.class, long.class, long.class,
      a -> {// param regs are %rdi, %rsi, %rdx, %rcx, %r8 and %r9
        a.mov(rdi, rdx);//pid
        a.mov(rsi, rcx);//cpusetsize
        a.mov(rdx, r8);//*mask
        a.jmp(imm(NativeLibrary.findSymbolAddress("sched_setaffinity")));
      }

  );

  public static int setAffinity(int pid, Cpuset mask) {
    try {
      return (int)mh_sched_setaffinity.invokeExact(
          pid, Cpuset.SIZE_CPUSET_TYPE, mask.addrBuffer());
    } catch (Throwable t) {
//      t.printStackTrace();
      return -1;
    }
  }

  /**
   * now only support machine <= 64 cpus.
   * see more {@link Affinity.Topology}
   */
  public static final class Cpuset {
    public static final long SIZE_CPUSET_TYPE = 8;
    private final long buffer;

    public Cpuset(long mask) {
      buffer = allocate(SIZE_CPUSET_TYPE);
      UNSAFE.putLong(buffer,mask);
    }

    public Cpuset() {
      buffer = allocate(SIZE_CPUSET_TYPE);
      UNSAFE.putLong(buffer,0L);
    }

    public long getMask() {
      return UNSAFE.getLong(buffer);
    }

    public long addrBuffer() {
      return buffer;
    }

    @Override
    public void finalize() {
      if (buffer!=0L)
        free(buffer);
    }

  }


}
