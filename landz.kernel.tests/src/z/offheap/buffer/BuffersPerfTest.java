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

import static z.offheap.buffer.Buffers.clear;
import static z.offheap.buffer.Buffers.copy;
import static z.offheap.buffer.Buffers.fill;
import static z.util.Unsafes.UNSAFE;
import static z.util.Unsafes.systemAllocateMemory;
import static z.util.Unsafes.systemFreeMemory;

/**
 */
public class BuffersPerfTest {

  @Test
  public void testClear() {
    long s,t;
    int length = 2*1024*1024;
    int[] a = new int[length];
    long[] b = new long[length];
    ThreadLocalRandom rnd = ThreadLocalRandom.current();
    for (int i = 0; i < length; i++) {
      a[i] = rnd.nextInt(1, 32);
      b[i] = systemAllocateMemory(a[i]);
    }

    s = System.nanoTime();
    for (int i = 0; i < length; i++) {
      clear(b[i],a[i]);
    }
    t = System.nanoTime() - s;

    System.out.printf("time cost of %d times clear %,d nanoseconds\n", length, t);


    s = System.nanoTime();
    for (int i = 0; i < length; i++) {
      UNSAFE.setMemory(b[i], a[i], (byte)0);
    }
    t = System.nanoTime() - s;

    System.out.printf("time cost of %d times setMemory %,d nanoseconds\n", length, t);


    for (int i = 0; i < length; i++) {
      systemFreeMemory(b[i]);
    }

  }


  @Test
  public void testClear2() {
    long s,t;
    int length = 2*1024*1024;
    int[] a = new int[length];
    long[] b = new long[length];
    ThreadLocalRandom rnd = ThreadLocalRandom.current();
    for (int i = 0; i < length; i++) {
      a[i] = rnd.nextInt(36,72);
      b[i] = systemAllocateMemory(a[i]);
    }

    s = System.nanoTime();
    for (int i = 0; i < length; i++) {
      clear(b[i],a[i]);
    }
    t = System.nanoTime() - s;

    System.out.printf("time cost of %d times clear2 %,d nanoseconds\n", length, t);


    s = System.nanoTime();
    for (int i = 0; i < length; i++) {
      UNSAFE.setMemory(b[i], a[i], (byte)0);
    }
    t = System.nanoTime() - s;

    System.out.printf("time cost of %d times setMemory2 %,d nanoseconds\n", length, t);


    for (int i = 0; i < length; i++) {
      systemFreeMemory(b[i]);
    }

  }


  @Test
  public void testFill() {
    long s,t;
    int length = 2*1024*1024;
    int[] a = new int[length];
    long[] b = new long[length];
    ThreadLocalRandom rnd = ThreadLocalRandom.current();
    for (int i = 0; i < length; i++) {
      a[i] = rnd.nextInt(1, 5);
      b[i] = systemAllocateMemory(a[i]);
    }

    s = System.nanoTime();
    for (int i = 0; i < length; i++) {
      fill(b[i], a[i], (byte) 123);
    }
    t = System.nanoTime() - s;

    System.out.printf("time cost of %d times fill %,d nanoseconds\n", length, t);


    s = System.nanoTime();
    for (int i = 0; i < length; i++) {
      UNSAFE.setMemory(b[i], a[i], (byte)123);
    }
    t = System.nanoTime() - s;

    System.out.printf("time cost of %d times setMemory(fill) %,d nanoseconds\n", length, t);


    for (int i = 0; i < length; i++) {
      systemFreeMemory(b[i]);
    }

  }


  @Test
  public void testCopy() {
    long s,t;
    int length = 2*1024*1024;
    int[] a = new int[length];
    long[] b1 = new long[length];
    long[] b2 = new long[length];
    ThreadLocalRandom rnd = ThreadLocalRandom.current();
    for (int i = 0; i < length; i++) {
      a[i] = rnd.nextInt(1, 32);
      b1[i] = systemAllocateMemory(a[i]);
      b2[i] = systemAllocateMemory(a[i]);
    }

    s = System.nanoTime();
    for (int i = 0; i < length; i++) {
      copy(b1[i],b2[i],a[i]);
    }
    t = System.nanoTime() - s;

    System.out.printf("time cost of %d times copy %,d nanoseconds\n", length, t);


    s = System.nanoTime();
    for (int i = 0; i < length; i++) {
      UNSAFE.copyMemory(b1[i], b2[i], a[i]);
    }
    t = System.nanoTime() - s;

    System.out.printf("time cost of %d times copyMemory %,d nanoseconds\n", length, t);


    for (int i = 0; i < length; i++) {
      systemFreeMemory(b1[i]);
      systemFreeMemory(b2[i]);
    }

  }


  @Test
  public void testCopy2() {
    long s,t;
    int length = 2*1024*1024;
    int a = 16;
    long[] b1 = new long[length];
    long[] b2 = new long[length];
    for (int i = 0; i < length; i++) {
      b1[i] = systemAllocateMemory(a);
      b2[i] = systemAllocateMemory(a);
    }

    s = System.nanoTime();
    for (int i = 0; i < length; i++) {
      copy(b1[i],b2[i],a);
    }
    t = System.nanoTime() - s;

    System.out.printf("time cost of %d times copy %,d nanoseconds\n", length, t);


    s = System.nanoTime();
    for (int i = 0; i < length; i++) {
      UNSAFE.copyMemory(b1[i], b2[i], a);
    }
    t = System.nanoTime() - s;

    System.out.printf("time cost of %d times copyMemory %,d nanoseconds\n", length, t);


    for (int i = 0; i < length; i++) {
      systemFreeMemory(b1[i]);
      systemFreeMemory(b2[i]);
    }

  }


}
