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
import sun.nio.ch.IOUtil;
import sun.nio.ch.Net;
import org.xnio.nativeimpl.Native;
import z.znr.socket.Sockets;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static z.util.MethodHandles.LOOKUP;
import static z.util.Throwables.uncheckTo;
import static z.znr.Syscall.*;

/**
 *
 test_sys_socket: spawn 50000 socket costs 46,085,626 nanos with last sockfd 50025
 test_net_socket: spawn 50000 socket costs 54,567,699 nanos with last sock 100025
 test_xnio_socket: spawn 50000 socket costs 46,118,654 nanos with last sock 150026
 *
 */
public class SyscallPerfTest {
  static {
    System.setProperty("jnr.invoke.compile.dump", "false");
  }

  private static final int COUNT = 50_000;

  static {
    //set rlimit
    long rlim_cur = COUNT*3+1000;
    long rlim_max = rlim_cur;
    int suc = sys_setrlimit(Rlimit.RLIMIT_NOFILE,rlim_cur,rlim_max);
    assertThat(suc, is(0));
  }


  @Test
  public void test_xnio_socket() {
    int sockfd = 0;

    long s = System.nanoTime();
    for (int i = 0; i < COUNT; i++) {
      sockfd = Native.socketTcp();
    }
    long t = System.nanoTime() - s;
    assertThat(sockfd, greaterThan(COUNT));

    System.out.printf(
        "test_xnio_socket: spawn %d socket costs %,d nanos with last sock %d\n",
        COUNT, t, sockfd);
  }


  @Test
  public void test_sys_socket() {
    int sockfd = 0;

    long s = System.nanoTime();
    for (int i = 0; i < COUNT; i++) {
      sockfd =
          sys_socket(
              Sockets.DomainFamily.INET,
              Sockets.SocketType.SOCK_STREAM,
              0);
    }
    long t = System.nanoTime() - s;
    assertThat(sockfd, greaterThan(COUNT));

    System.out.printf(
        "test_sys_socket: spawn %d socket costs %,d nanos with last sockfd %d\n",
        COUNT, t, sockfd);
  }


  @Test
  public void test_net_socket() {
    IOUtil.load();//loading native library

    int sockfd = 0;

    long s = System.nanoTime();
    for (int i = 0; i < COUNT; i++) {
      sockfd = socket0(false,true,false);
    }
    long t = System.nanoTime() - s;
    assertThat(sockfd, greaterThan(COUNT));

    System.out.printf(
        "test_net_socket: spawn %d socket costs %,d nanos with last sock %d\n",
        COUNT, t, sockfd);
  }


  private static final MethodHandle mh_net_socket0 = uncheckTo(() ->
      LOOKUP.findStatic(Net.class, "socket0",
          MethodType.methodType(int.class,
              boolean.class, boolean.class, boolean.class)));

  public static int socket0(boolean preferIPv6, boolean stream, boolean reuse) {
    return (int)uncheckTo(()-> mh_net_socket0.invoke(preferIPv6,stream,reuse));
  }



}
