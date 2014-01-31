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

package z.util.concurrent;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static z.util.Throwables.uncheck;

/**
 * Created by jin on 1/8/14.
 */
public class ThreadLocalPoolTestMT {

  static class A implements AutoCloseable {
    boolean closed = false;

    public void close() {
      closed=true;
      System.out.println("closed!");
    }
  }

  ThreadLocalPool<A> aPool = new ThreadLocalPool(4,2,A::new);

  @Test
  public void sanityCheckMT() {
    int N = 50;
    CountDownLatch latch = new CountDownLatch(N);
    for (int i = 0; i < N; i++) {
      new Thread(()-> {
        for (int j = 0; j < 30_000_000; j++) {
          doCheck();
        }
        latch.countDown();
      }).start();
    }

    System.out.println("wait to be done...");
    uncheck(()->latch.await());
    System.out.println("done.");
  }

  private void doCheck() {
    ThreadLocalPool.Item<A> item1 = aPool.item();
    A a1 = item1.get();

    ThreadLocalPool.Item<A> item2 = aPool.item();
    A a2 = item2.get();

    item2.close();

    ThreadLocalPool.Item<A> item3 = aPool.item();
    A a3 = item3.get();

    assertThat(item2 == item3, is(true));
    assertThat(a2 == a3, is(true));

    ThreadLocalPool.Item<A> item4 = aPool.item();
    A a4 = item4.get();

    assertThat(item4 != item3, is(true));
    assertThat(a4 != a3, is(true));

    ThreadLocalPool.Item<A> item5 = aPool.item();
    A a5 = item5.get();

    assertThat(item5 != item4, is(true));
    assertThat(a5 != a4, is(true));

    assertThat(aPool.item(), nullValue());

    item1.close();
    item3.close();
    item4.close();
    item5.close();
  }


}
