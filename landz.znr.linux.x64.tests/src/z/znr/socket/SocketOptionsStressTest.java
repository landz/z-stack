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

package z.znr.socket;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static z.znr.Syscall.sys_close;
import static z.znr.Syscall.sys_socket;

/**
 */
public class SocketOptionsStressTest {
  public static final long COUNT = 1000_000L;

  @Test
  public void testUsagesOfSockOptions() {
    long s,t;

    s = System.nanoTime();
    for (long i = 0; i < COUNT; i++) {
      int sockfd =
          sys_socket(
              Sockets.DomainFamily.INET,
              Sockets.SocketType.SOCK_STREAM,
              0);
      assertThat(sockfd, greaterThan(0));

      boolean enabled = SocketOptions.getOptReuseAddr(sockfd);
      assertThat(enabled, is(false));

      SocketOptions.setOptReuseAddr(sockfd, true);

      enabled = SocketOptions.getOptReuseAddr(sockfd);
      assertThat(enabled, is(true));

      int suc = sys_close(sockfd);
      assertThat(suc, is(0));
    }
    t = System.nanoTime()-s;
    System.out.printf("SocketOptionsStressTest#testUsagesOfSockOptions cost: %,d", t);

  }


}
