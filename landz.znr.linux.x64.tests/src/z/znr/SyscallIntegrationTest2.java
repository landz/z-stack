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
import z.znr.socket.SocketOptions;
import z.znr.socket.Sockets;

import z.znr.socket.SocketAddressInet;
import z.znr.socket.Sockets;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static z.offheap.zmalloc.Allocator.allocate;
import static z.offheap.zmalloc.Allocator.free;
import static z.util.Throwables.uncheck;
//import static z.util.Unsafes.UNSAFE;
import static z.znr.Syscall.*;
import static org.xnio.nativeimpl.Native.*;

/**
 */
public class SyscallIntegrationTest2 {

  public static final long N = 1024*512;

  protected static SocketAddress getServerAddress() throws UnknownHostException {
    return new InetSocketAddress(InetAddress.getByAddress(new byte[]{127, 0, 0, 1}), 12345);
  }

  @Test
  public void test_syscall_socket() throws Exception {
    int serverfd, suc, clientfd;
    long size;
//    final String MSG = "hello";

    serverfd = socketTcp();
    assertThat(serverfd, greaterThan(0));
//    System.out.println(sockfd);

//    SocketAddressInet addr = new SocketAddressInet(SocketAddressInet.LOOPBACK_ADDRESS,12345);
    byte[] addr = encodeSocketAddress(getServerAddress());
    suc = bind(serverfd,addr);
    assertThat(suc, is(0));

    suc = listen(serverfd, 10);
    assertThat(suc, is(0));


    //start client
    clientThreadznr();
//    clientThreadxnio();



    System.out.println("server thread will block on accepting...");
    while ( (clientfd = accept(serverfd)) < 0) {
      if (clientfd==-Errno.EAGAIN) {
//        System.out.println("try again..");
      }
    }
    assertThat(clientfd, greaterThan(0));

    System.out.println("server thread got a clientfd:"+clientfd);

    //ReceiveBuffer
    int sizeRcvBuf = SocketOptions.getOptReceiveBuffer(clientfd);
    assertThat(sizeRcvBuf, greaterThan(1024));
    System.out.println("ReceiveBuffer of clientfd:"+sizeRcvBuf);

    byte[] sBuffer = new byte[(int)N];
    System.out.println("server thread will block on reading...");
    while ( (size = readH(clientfd, sBuffer, 0, (int)N))<0 ) {
//      if (size==-Errno.EAGAIN) {
//        System.out.println("try read again..");
//      }
    }

//    assertThat(size,is(N));
    System.out.println("=====server thread received "+size+" bytes data.");

//    StringBuilder msg = new StringBuilder(5);
//    for (int i = 0; i < size; i++) {
//      msg.append((char)UNSAFE.getByte(sBuffer+i));
//    }
//
//    System.out.println("and we got msg:"+msg);
//    assertThat(msg.toString(),is(MSG));

    suc = sys_close(serverfd);
    assertThat(suc, is(0));
//    free(sBuffer);
    System.out.println("whole test done.");
  }


  public void clientThreadznr() {
    //======================================================
    //client thread
    new Thread(()->{
      uncheck(()->Thread.sleep(5000L));

      System.out.println("client thread start to work...");
      int cfd =
          sys_socket(
              Sockets.DomainFamily.INET,
              Sockets.SocketType.SOCK_STREAM,
              0);

      SocketAddressInet addressInet = new SocketAddressInet(SocketAddressInet.LOOPBACK_ADDRESS,12345);
      System.out.println("client thread now try to connect to server...");
      int csuc = Sockets.connect(cfd, addressInet);
      assertThat(csuc, is(0));
      System.out.println("client thread connected.");

      long cBuffer = allocate(N);

      System.out.println("client thread will block on writing...");
      long csize = sys_write(cfd, cBuffer, N);
      assertThat(csize,is(N));

      //SendBuffer
      int sb = SocketOptions.getOptSendBuffer(cfd);
      assertThat(sb, greaterThan(1024));
      System.out.println("SendBuffer:"+sb);//

      System.out.println("client thread has sent out "+csize+" bytes data.");



//      //TODO: shutdown
//      csuc = sys_shutdown(cfd,Sockets.ShutDownType.SHUT_RD);
//      assertThat(csuc, is(0));
//
//      csuc = sys_shutdown(cfd,Sockets.ShutDownType.SHUT_WR);
//      assertThat(csuc, is(0));
//
//      //fail for repeating call?
//      csuc = sys_shutdown(cfd,Sockets.ShutDownType.SHUT_RDWR);
//      assertThat(csuc, is(0));
//
//      csuc = sys_close(cfd);
//      assertThat(csuc, is(0));
//      free(cBuffer);

      uncheck(()->Thread.sleep(10_000L));
      System.out.println("client thread done.");
    }).start();
    //======================================================

  }


  public void clientThreadxnio() {
    //======================================================
    //client thread
    new Thread(()->{
      uncheck(()->Thread.sleep(5000L));

      System.out.println("client thread start to work...");
      int cfd = socketTcp();
      byte[] addr = null;
      try {
      addr = encodeSocketAddress(getServerAddress());
      }catch (Exception e){e.printStackTrace();}

      System.out.println("client thread now try to connect to server...");
      int csuc=0;
      while ( (csuc = connect(cfd, addr)) <0 ) {
      }
      assertThat(csuc, is(0));
      System.out.println("client thread connected.");

      byte[] cBuffer = new byte[(int)N];//allocate(N);
      for (int i = 0; i < cBuffer.length; i++) {
        cBuffer[i] = 66;
      }

      System.out.println("client thread will block on writing...");
      long csize = writeH(cfd, cBuffer, 0, (int)N);
      assertThat(csize,is(N));

      //SendBuffer
      int sb = SocketOptions.getOptSendBuffer(cfd);
      assertThat(sb, greaterThan(1024));
      System.out.println("SendBuffer:"+sb);//

      System.out.println("client thread has sent out "+csize+" bytes data.");
//
//      //TODO: shutdown
//      csuc = sys_shutdown(cfd,Sockets.ShutDownType.SHUT_RD);
//      assertThat(csuc, is(0));
//
//      csuc = sys_shutdown(cfd,Sockets.ShutDownType.SHUT_WR);
//      assertThat(csuc, is(0));
//
//      //fail for repeating call?
//      csuc = sys_shutdown(cfd,Sockets.ShutDownType.SHUT_RDWR);
//      assertThat(csuc, is(0));
//
//      csuc = sys_close(cfd);
//      assertThat(csuc, is(0));
//      free(cBuffer);

      uncheck(()->Thread.sleep(10_000L));
      System.out.println("client thread done.");
    }).start();
    //======================================================

  }


}
