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

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

//XXX:please use the direct call when testing

//import static z.offheap.zmalloc.Allocator.*;

/**
 * see more {@link z.offheap.zmalloc.perf.RemoteFreedChunksMTPerfTest1}
 *
 */
public class TrivialCLQSingleThreadPerfTest {

  private static final int COUNT    = 10_000_000;

  private static ConcurrentLinkedQueue<Long> clq;


  @Test
  public void test() {
    for (int i = 0; i < 2; i++) {
      clq = new ConcurrentLinkedQueue();
      System.out.println("======RUNS#"+i);
      testCLQAddRemove();
    }
  }


  public void testCLQAddRemove() {

    //============================================
    Long[] chunks = new Long[COUNT];
    ThreadLocalRandom rnd = ThreadLocalRandom.current();
    for (int i = 0; i < COUNT; i++) {
      chunks[i] = rnd.nextLong(4_000_000_001L,Long.MAX_VALUE);
    }

    //ensure all address in chunks are valid
    for (int i = 0; i < COUNT; i++) {
      assertThat(chunks[i],greaterThan(4_000_000_000L));//mmapped memory in high address
    }

    System.out.println("start test...");
    //============================================

    long s = System.nanoTime();
    for (int i=0;i<COUNT;i++) {
      clq_add(chunks[i]);
    }
    long t = System.nanoTime()-s;
    System.out.println("add "+COUNT+" chunks to RemoteRreedChunks cost: "
        + TimeUnit.NANOSECONDS.toMillis(t) + " millis");

    Long[] removed = new Long[COUNT];
    s = System.nanoTime();
    for (int i=0;i<COUNT;i++) {
      removed[i] = clq_remove();
    }
    t = System.nanoTime()-s;
    System.out.println("remove "+COUNT+" chunks from RemoteRreedChunks cost: "
        + TimeUnit.NANOSECONDS.toMillis(t) + " millis");

    for (int i = 0; i < removed.length; i++) {
      assertThat(removed[i], greaterThan(4_000_000_000L));
    }

  }

  private Long clq_remove() {
    return clq.poll();
  }

  private void clq_add(Long chunk) {
    clq.offer(chunk);
  }

}
