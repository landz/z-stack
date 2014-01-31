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

import org.junit.Test;
import z.znr.socket.SocketAddressInet;
import z.znr.socket.SocketAddressInet6;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.Is.is;
import static z.znr.Syscall.*;
import static z.znr.socket.Sockets.*;

/**
 * Created by jin on 12/15/13.
 */
public class SyscallTest {
  static {
    System.setProperty("jnr.invoke.compile.dump", "false");
  }


  @Test
  public void test_sys_epoll_create_and_close() {
    int epfd0 = sys_epoll_create(512);
    int epfd1 = sys_epoll_create(1024);

    assertThat(epfd0, greaterThan(0));
    assertThat(epfd1, greaterThan(0));
    assertThat(epfd1-epfd0, lessThan(10));//relax for easier life

    int suc = sys_close(epfd0);
    assertThat(suc, is(0));
    suc = sys_close(epfd1);
    assertThat(suc, is(0));
  }

  @Test
  public void test_sys_getrlimit() {
    Rlimit rlimit = new Rlimit();
    int suc = sys_getrlimit(Rlimit.RLIMIT_NOFILE,rlimit);
//    System.out.println(rlimit.rlim_cur);
//    System.out.println(rlimit.rlim_max);
    assertThat(suc, is(0));
    assertThat(rlimit.rlim_cur, greaterThan(1L));
  }

  @Test
  public void test_sys_setrlimit() {
    long rlim_cur = 102400;
    long rlim_max = rlim_cur;
    int suc = sys_setrlimit(Rlimit.RLIMIT_NOFILE,rlim_cur,rlim_max);
    assertThat(suc, is(0));

    Rlimit rlimit = new Rlimit();
    suc = sys_getrlimit(Rlimit.RLIMIT_NOFILE,rlimit);
//    System.out.println(rlimit.rlim_cur);
//    System.out.println(rlimit.rlim_max);
    assertThat(suc, is(0));
    assertThat(rlimit.rlim_cur, is(rlim_cur));
  }

  @Test
  public void test_sys_socket() {
    int sockfd =
        sys_socket(
            DomainFamily.INET,
            SocketType.SOCK_STREAM | SocketType.SOCK_NONBLOCK,
            0);
    assertThat(sockfd, greaterThan(0));
//    System.out.println(sockfd);

    int suc = sys_close(sockfd);
    assertThat(suc, is(0));

    int oldSockfd = sockfd;
    sockfd =
        sys_socket(
            DomainFamily.INET,
            SocketType.SOCK_STREAM | SocketType.SOCK_CLOEXEC,
            0);
    assertThat(sockfd, is(oldSockfd));

    sockfd =
        sys_socket(
            DomainFamily.INET,
            SocketType.SOCK_STREAM | SocketType.SOCK_NONBLOCK,
            0);
    assertThat(sockfd, greaterThan(oldSockfd));
//    System.out.println(sockfd);

  }

  @Test
  public void test_sys_bind() {
    //ip
    int sockfd =
        sys_socket(
            DomainFamily.INET,
            SocketType.SOCK_STREAM,
            0);
    assertThat(sockfd, greaterThan(0));
//    System.out.println(sockfd);

    SocketAddressInet addr = new SocketAddressInet("127.0.0.0",12345);
//    System.out.println("addr:"+ Bytes.asList(addr.getAddr()));
//    System.out.println("port:"+ addr.getPort());

    int suc = bind(sockfd,addr);
    assertThat(suc, is(0));

    suc = sys_close(sockfd);
    assertThat(suc, is(0));

    //ipv6
    sockfd =
        sys_socket(
            DomainFamily.INET6,
            SocketType.SOCK_STREAM,
            0);
    assertThat(sockfd, greaterThan(0));
//    System.out.println(sockfd);

    SocketAddressInet6 addr6 = new SocketAddressInet6(SocketAddressInet6.ANY_ADDRESS,55555);
//    System.out.println("addr:"+ Bytes.asList(addr6.getAddr()));
//    System.out.println("port:"+ addr6.getPort());

    suc = bind(sockfd,addr6);
    assertThat(suc, is(0));

    suc = sys_close(sockfd);
    assertThat(suc, is(0));
  }

  @Test
  public void test_sys_shutdown() {
    int sockfd =
        sys_socket(
            DomainFamily.INET,
            SocketType.SOCK_STREAM,
            0);
    assertThat(sockfd, greaterThan(0));

    int csuc = sys_shutdown(sockfd, ShutDownType.SHUT_RDWR);
    assertThat(csuc, is(-Errno.ENOTCONN));

    csuc = sys_close(sockfd);
    assertThat(csuc, is(0));

    System.out.println("done.");
  }


}
