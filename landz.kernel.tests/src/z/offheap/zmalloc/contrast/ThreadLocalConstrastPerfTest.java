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

package z.offheap.zmalloc.contrast;

import org.junit.Test;
import z.offheap.zmalloc.Allocator;
import z.util.concurrent.ThreadLocalPool;
import z.util.concurrent.ThreadLocalValueHolder;

import java.nio.ByteBuffer;

/**
 allocateAndFreeMemoryZValue cost(ns):          25,566,778 with buffer:java.nio.HeapByteBuffer[pos=0 lim=4096 cap=4096]
 allocateAndFreeMemoryNewPlain cost(ns):       157,011,453 with num:4096
 allocateAndFreeMemoryThreadLocal cost(ns):    184,493,958 with buffer:java.nio.HeapByteBuffer[pos=0 lim=4096 cap=4096]
 allocateAndFreeMemoryZPool cost(ns):          489,961,349 with buffer:java.nio.HeapByteBuffer[pos=0 lim=4096 cap=4096]
 allocateAndFreeMemoryCostZMAlloc cost(ns):    927,099,592, with address: 139869368082496
 allocateAndFreeMemoryNew cost(ns):         15,176,478,551 with buffer:java.nio.HeapByteBuffer[pos=0 lim=4096 cap=4096]

 */
public class ThreadLocalConstrastPerfTest {

  private static ThreadLocal<ByteBuffer> tlBuffer =
      new ThreadLocal<ByteBuffer>().withInitial(()-> {
        return ByteBuffer.allocate(4096);
      });

  private static ThreadLocalPool<ByteBuffer> zpool =
      new ThreadLocalPool(1,()->ByteBuffer.allocate(4096));

  private static ThreadLocalValueHolder<ByteBuffer> zvalue =
      new ThreadLocalValueHolder(()->ByteBuffer.allocate(4096));

  private static final long COUNT = 50_000_000L;

  @Test
  public void allocateAndFreeMemoryCostZMAlloc() {
    long s,t;
    long address = 0;
    s = System.nanoTime();
    for (long i = 0; i < COUNT; i++) {
      address = Allocator.allocate(4096);
      Allocator.free(address);
    }
    t = System.nanoTime() -s;
    System.out.printf("allocateAndFreeMemoryCostZMAlloc cost(ns):%,d, with address: %d\n",t,address);

  }

  @Test
  public void allocateAndFreeMemoryThreadLocal() {
    long s,t;
    ByteBuffer buffer = null;
    s = System.nanoTime();
    for (long i = 0; i < COUNT; i++) {
      buffer = tlBuffer.get();
    }
    t = System.nanoTime() -s;
    System.out.printf("allocateAndFreeMemoryThreadLocal cost(ns):%,d with buffer:%s\n",t, buffer.toString());
  }

  @Test
  public void allocateAndFreeMemoryNew() {
    long s,t;
    ByteBuffer buffer = null;
    s = System.nanoTime();
    for (long i = 0; i < COUNT; i++) {
      buffer = ByteBuffer.allocate(4096);
    }
    t = System.nanoTime() -s;
    System.out.printf("allocateAndFreeMemoryNew cost(ns):%,d with buffer:%s\n",t, buffer.toString());
  }

  @Test
  public void allocateAndFreeMemoryNewPlain() {
    long s,t;
    Integer num = new Integer(4096);
    s = System.nanoTime();
    for (long i = 0; i < COUNT; i++) {
      num = new Integer(4096);
    }
    t = System.nanoTime() -s;
    System.out.printf("allocateAndFreeMemoryNewPlain cost(ns):%,d with num:%s\n",t, num.toString());
  }

  @Test
  public void allocateAndFreeMemoryZPool() {
    long s,t;
    ByteBuffer buffer = null;
    for (int i = 0; i < 500_000; i++) {
      try (ThreadLocalPool.Item<ByteBuffer> item = zpool.item()) {
        buffer = item.get();
      }
    }

    s = System.nanoTime();
    for (long i = 0; i < COUNT; i++) {
      try (ThreadLocalPool.Item<ByteBuffer> item = zpool.item()) {
        buffer = item.get();
      }
    }
    t = System.nanoTime() -s;
    System.out.printf("allocateAndFreeMemoryZPool cost(ns):%,d with buffer:%s\n",t, buffer.toString());
  }


  @Test
  public void allocateAndFreeMemoryZValue() {
    long s,t;
    ByteBuffer buffer = null;
    s = System.nanoTime();
    for (long i = 0; i < COUNT; i++) {
      buffer = zvalue.get();
    }
    t = System.nanoTime() -s;
    System.out.printf("allocateAndFreeMemoryZValue cost(ns):%,d with buffer:%s\n",t, buffer.toString());
  }

}
