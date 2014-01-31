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

package z.async;

import org.junit.Test;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import static z.util.Throwables.uncheck;

public class BasicsOfForkJoin {
  public static final int COUNT = 30_000_000;

//  public static final AtomicLong ct = new AtomicLong();

  @Test
  public void testExternalInvoke() {
    int parallelism = 7;

    ForkJoinPool p = new ForkJoinPool(
        parallelism, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true);

    long s = System.nanoTime();
    for (int i = 0; i < COUNT; i++) {
      p.execute(()->{});//ct.incrementAndGet()
    }
    long t1 = System.nanoTime()-s;

    p.shutdown();
    uncheck(() ->
        p.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS));

//    while (ct.get()!=COUNT) {}
    long t2 = System.nanoTime()-s;

    System.out.printf("costed %,d to submit %,d tasks, and %,d to complete\n",
        t1, COUNT, t2);

    System.out.println("done");
  }

}
