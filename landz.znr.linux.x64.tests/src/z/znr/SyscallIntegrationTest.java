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
import z.znr.socket.Sockets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static z.offheap.zmalloc.Allocator.*;
import static z.util.Throwables.uncheck;
import static z.util.Unsafes.UNSAFE;
import static z.znr.Syscall.*;
/**
 * this is an integration test for Syscall.
 * this demonstrates a simple blocking server/client program on top of znr.
 */
public class SyscallIntegrationTest {
  static {
    System.setProperty("jnr.invoke.compile.dump", "false");
  }


  @Test
  public void test_syscall_socket() {
    int serverfd, suc, clientfd;
    long size;
    final String MSG = "hello";

    serverfd =
        sys_socket(
            Sockets.DomainFamily.INET,
            Sockets.SocketType.SOCK_STREAM,
            0);
    assertThat(serverfd, greaterThan(0));
//    System.out.println(sockfd);

    SocketAddressInet addr = new SocketAddressInet(SocketAddressInet.LOOPBACK_ADDRESS,12345);

    suc = Sockets.bind(serverfd,addr);
    assertThat(suc, is(0));

    suc = sys_listen(serverfd, 10);
    assertThat(suc, is(0));

    //client thread
    new Thread(()->{
      uncheck(()->Thread.sleep(1000L));

      System.out.println("client thread start to work...");
      int cfd =
          sys_socket(
              Sockets.DomainFamily.INET,
              Sockets.SocketType.SOCK_STREAM,
              0);

      System.out.println("client thread now try to connect to server...");
      int csuc = Sockets.connect(cfd,addr);
      assertThat(csuc, is(0));
      System.out.println("client thread connected.");

      long cBuffer = allocate(8);

      for (int i = 0; i < MSG.length(); i++) {
        UNSAFE.putByte(cBuffer+i, (byte)MSG.charAt(i));
      }

      System.out.println("client thread will block on writing...");
      long csize = sys_write(cfd,cBuffer,5);
      assertThat(csize,is(5L));
      System.out.println("client thread come back.");
      free(cBuffer);

      //TODO: shutdown
      csuc = sys_shutdown(cfd,Sockets.ShutDownType.SHUT_RD);
      assertThat(csuc, is(0));

      csuc = sys_shutdown(cfd,Sockets.ShutDownType.SHUT_WR);
      assertThat(csuc, is(0));

      //fail for repeating call?
      csuc = sys_shutdown(cfd,Sockets.ShutDownType.SHUT_RDWR);
      assertThat(csuc, is(0));

      csuc = sys_close(cfd);
      assertThat(csuc, is(0));
      free(cBuffer);
      System.out.println("client thread done.");
    }).start();



    System.out.println("server thread will block on accepting...");
    clientfd = Sockets.acceptBlock(serverfd);
    assertThat(clientfd, greaterThan(0));
    System.out.println("server thread got a clientfd:"+clientfd);

    long sBuffer = allocate(8);//to hold "hello"
    System.out.println("server thread will block on reading...");

    size = sys_read(clientfd, sBuffer, 8);
    assertThat(size,is(5L));
    System.out.println("server thread come back.");

    StringBuilder msg = new StringBuilder(5);
    for (int i = 0; i < size; i++) {
      msg.append((char)UNSAFE.getByte(sBuffer+i));
    }

    System.out.println("and we got msg:"+msg);
    assertThat(msg.toString(),is(MSG));

    suc = sys_close(serverfd);
    assertThat(suc, is(0));
    free(sBuffer);
    System.out.println("whole test done.");
  }


}
