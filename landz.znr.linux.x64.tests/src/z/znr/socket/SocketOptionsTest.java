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
import static z.znr.Syscall.*;

/**
 sysctl -a

 sysctl -n net.ipv4.tcp_rmem
 4096	87380	6291456
 sysctl -n net.ipv4.tcp_wmem
 4096	16384	4194304
 sysctl -n net.ipv4.tcp_mem
 380154	506874	760308
 */
public class SocketOptionsTest {

  @Test
  public void testUsagesOfSockOptions() {
    //ip
    int sockfd =
        sys_socket(
            Sockets.DomainFamily.INET,
            Sockets.SocketType.SOCK_STREAM,
            0);
    assertThat(sockfd, greaterThan(0));

    //ReuseAddr
    boolean enabled = SocketOptions.getOptReuseAddr(sockfd);
    assertThat(enabled, is(false));

    SocketOptions.setOptReuseAddr(sockfd, true);

    enabled = SocketOptions.getOptReuseAddr(sockfd);
    assertThat(enabled, is(true));
//    System.out.println(enabled);


    //KeepAlive
    enabled = SocketOptions.getOptKeepAlive(sockfd);
    assertThat(enabled, is(false));

    SocketOptions.setOptKeepAlive(sockfd, true);

    enabled = SocketOptions.getOptKeepAlive(sockfd);
    assertThat(enabled, is(true));

    //TcpNoDelay
    enabled = SocketOptions.getOptTcpNoDelay(sockfd);
    assertThat(enabled, is(false));

    SocketOptions.setOptTcpNoDelay(sockfd, true);

    enabled = SocketOptions.getOptTcpNoDelay(sockfd);
    assertThat(enabled, is(true));

    //ReceiveBuffer
    int size = SocketOptions.getOptReceiveBuffer(sockfd);
    assertThat(size, greaterThan(1024));
    System.out.println("ReceiveBuffer:"+size);

//    SocketOptions.setOptTcpNoDelay(sockfd,true);
//
//    enabled = SocketOptions.getOptTcpNoDelay(sockfd);
//    assertThat(enabled, is(true));

    //SendBuffer
    size = SocketOptions.getOptSendBuffer(sockfd);
    assertThat(size, greaterThan(1024));
    System.out.println("SendBuffer:"+size);


    int suc = sys_close(sockfd);
    assertThat(suc, is(0));
  }


}
