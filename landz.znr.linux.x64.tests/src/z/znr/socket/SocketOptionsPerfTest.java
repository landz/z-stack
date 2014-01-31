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
import org.xnio.nativeimpl.Native;

import static z.znr.Syscall.sys_close;
import static z.znr.Syscall.sys_socket;

/**
 *
 * no warm up, for reference:
 xnio's in testUsagesOfSockOptions costs: 2,226,045,783
 znr's testUsagesOfSockOptions costs: 2,193,521,319
 */
public class SocketOptionsPerfTest {
  public static final long COUNT = 1000_000L;

  @Test
  public void testUsagesOfSockOptions() throws Exception {
    long s,t;

    //xnio
    s = System.nanoTime();
    for (long i = 0; i < COUNT; i++) {

      int sockfd = Native.socketTcp();
//      assertThat(sockfd, greaterThan(0));

      boolean enabled = Native.getOptReuseAddr(sockfd)==0?false:true;
//      assertThat(enabled,is(false));

      Native.setOptReuseAddr(sockfd,true);

      enabled = Native.getOptReuseAddr(sockfd)==0?false:true;
//      assertThat(enabled,is(true));

      int suc = sys_close(sockfd);
//      assertThat(suc, is(0));
    }
    t = System.nanoTime()-s;
    System.out.printf("xnio's in testUsagesOfSockOptions costs: %,d\n", t);


    System.gc();
    Thread.sleep(2000L);


    s = System.nanoTime();
    for (long i = 0; i < COUNT; i++) {
      int sockfd =
          sys_socket(
              Sockets.DomainFamily.INET,
              Sockets.SocketType.SOCK_STREAM,
              0);

      boolean enabled = SocketOptions.getOptReuseAddr(sockfd);

      SocketOptions.setOptReuseAddr(sockfd, true);

      enabled = SocketOptions.getOptReuseAddr(sockfd);

      int suc = sys_close(sockfd);
//      assertThat(suc, is(0));
    }
    t = System.nanoTime()-s;
    System.out.printf("znr's testUsagesOfSockOptions costs: %,d\n", t);

  }


}
