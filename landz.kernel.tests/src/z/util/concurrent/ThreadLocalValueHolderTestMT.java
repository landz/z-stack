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
public class ThreadLocalValueHolderTestMT {

  static class A implements AutoCloseable {
    boolean closed = false;

    public void close() {
      closed=true;
      System.out.println("closed!");
    }
  }

  ThreadLocalValueHolder<A> aholder = new ThreadLocalValueHolder(A::new);

  @Test
  public void sanityCheckMT() {
    int N = 50;
    CountDownLatch latch = new CountDownLatch(N);
    for (int i = 0; i < N; i++) {
      new Thread(()-> {
        for (int j = 0; j < 100_000_000; j++) {
          doCheck();
        }
        latch.countDown();
      }).start();
    }

    System.out.println("wait to be done...");
    uncheck(() -> latch.await());
    System.out.println("close the aholder...");
    uncheck(() -> aholder.close());
    System.out.println("done.");
  }

  private void doCheck() {
    assertThat(aholder.get(), is(aholder.get()));
    assertThat(aholder.get().closed, is(false));
  }


}
