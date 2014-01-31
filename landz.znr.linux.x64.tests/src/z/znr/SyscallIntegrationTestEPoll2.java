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
import z.znr.event.EPollEventArray;
import z.znr.event.FileDescriptorEPollEvent;
import z.znr.socket.SocketAddressInet;
import z.znr.socket.SocketOptions;
import z.znr.socket.Sockets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.xnio.nativeimpl.Native.*;
import static z.offheap.zmalloc.Allocator.allocate;
import static z.offheap.zmalloc.Allocator.free;
import static z.util.Throwables.uncheck;
import static z.znr.Syscall.*;
import static z.znr.event.EPollEvents.EPOLLIN;
import static z.znr.socket.Sockets.*;
import static z.znr.event.EPolls.*;

/**
 * do kinds of experiment with EPoll
 */
public class SyscallIntegrationTestEPoll2 {
  static {
    System.setProperty("jnr.invoke.compile.dump", "false");
  }

  public static final String MSG = "hello";

  public static final long N = 1024*512;


  @Test
  public void test_syscall_socket() {
    int serverfd, suc, clientfd;
    long size;

    serverfd = Sockets.socketTcp();
//        sys_socket(
//            Sockets.DomainFamily.INET,
//            Sockets.SocketType.SOCK_STREAM,
//            0);
    assertThat(serverfd, greaterThan(0));
//    System.out.println(sockfd);

    SocketAddressInet addr = new SocketAddressInet(SocketAddressInet.LOOPBACK_ADDRESS,12345);

    suc = bind(serverfd,addr);
    assertThat(suc, is(0));

    suc = sys_listen(serverfd, 10);
    assertThat(suc, is(0));

    //epoll
    int epfd = sys_epoll_create(512);
    assertThat(epfd, greaterThan(0));

    FileDescriptorEPollEvent e = new FileDescriptorEPollEvent();
    e.setEventMask(EPOLLIN);
    e.setFileDescriptor(serverfd);

    suc = epollAdd(epfd, serverfd, e);
    assertThat(suc, is(0));

    //client
    spawnClientThread(addr);
//    clientThreadxnio();
    //

    //wait on
    EPollEventArray events = new EPollEventArray();

//    for (; ; ) {
      System.out.println("wait on the epoll instance: " + epfd);
      int n =  epoll_wait(epfd, events, -1);
      assertThat(suc, greaterThanOrEqualTo(0));

      System.out.println("got " + n + " ready fd now.");//TODO: sometimes n=0(signal)

      for (int i = 0; i < n; i++) {

        FileDescriptorEPollEvent fdev =
            events.getEvent(i, FileDescriptorEPollEvent.class);
        assertThat(fdev.getFileDescriptor(), is(serverfd));

        System.out.println("server thread will block on accepting...");

        while ( (clientfd =
            acceptNonBlock(serverfd)) < 0 ) {
          //Sockets.SocketType.SOCK_NONBLOCK
        }
        assertThat(clientfd, greaterThan(0));
        System.out.println("server thread got a clientfd:" + clientfd);


        //ReceiveBuffer
        int sizeRcvBuf = SocketOptions.getOptReceiveBuffer(clientfd);
        assertThat(sizeRcvBuf, greaterThan(1024));
        System.out.println("ReceiveBuffer#1 of clientfd:"+sizeRcvBuf);


        long sBuffer = allocate(N);//to hold "hello"


        System.out.println("server thread will block on reading...");


        size = sys_read(clientfd, sBuffer, N);
//        while ( (size = sys_read(clientfd, sBuffer, N))<0){
//        }
//        byte[] b = new byte[(int)N];
//        while ( (size = readH(clientfd, b, 0, (int)N))<0){
//          //
//        }
//        assertThat(size, is(N));
        System.out.println("+++++server thread received "+ size + " bytes data.");

        //ReceiveBuffer
        sizeRcvBuf = SocketOptions.getOptReceiveBuffer(clientfd);
        assertThat(sizeRcvBuf, greaterThan(1024));
        System.out.println("ReceiveBuffer#2 of clientfd:"+sizeRcvBuf);

//        StringBuilder msg = new StringBuilder(5);
//        for (int j = 0; j < size; j++) {
//          msg.append((char) UNSAFE.getByte(sBuffer + j));
//        }
//
//        System.out.println("and we got msg:" + msg);
//        assertThat(msg.toString(), is(MSG));

        suc = sys_close(serverfd);
        assertThat(suc, is(0));
        free(sBuffer);
        System.out.println("whole test done.");
      }

//    }

  }


  private void spawnClientThread(SocketAddressInet addr) {

    //client thread
    new Thread(()->{
      uncheck(()->Thread.sleep(5_000L));

      System.out.println("client thread start to work...");
      int cfd =
          sys_socket(
              Sockets.DomainFamily.INET,
              Sockets.SocketType.SOCK_STREAM,
              0);

      System.out.println("client thread now try to connect to server...");
      int csuc = connect(cfd,addr);
      assertThat(csuc, is(0));
      System.out.println("client thread connected.");

      long cBuffer = allocate(N);

//      for (int i = 0; i < MSG.length(); i++) {
//        UNSAFE.putByte(cBuffer+i, (byte)MSG.charAt(i));
//      }

      //SendBuffer
      int sb = SocketOptions.getOptSendBuffer(cfd);
      assertThat(sb, greaterThan(1024));
      System.out.println("SendBuffer:"+sb);//

      System.out.println("client thread will block on writing after 20s...");
      uncheck(()->Thread.sleep(20_000L));
//      int N = 100_000_0000;//FIXME: send n times msg?
//      for (int j = 0; j < N; j++) {
        long csize = sys_write(cfd,cBuffer,N);
        assertThat(csize,is(N));
//        System.out.println("sent the "+j+"th msg.");
//      }

      //SendBuffer
      sb = SocketOptions.getOptSendBuffer(cfd);
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


//      csuc = sys_close(cfd);
//      assertThat(csuc, is(0));
//      free(cBuffer);
      //
      uncheck(()->Thread.sleep(10_000L));
      System.out.println("client thread done.");
    }).start();

  }


//  public void clientThreadxnio() {
//    //======================================================
//    //client thread
//    new Thread(()->{
//      uncheck(()->Thread.sleep(1000L));
//
//      System.out.println("client thread start to work...");
//      int cfd = socketTcp();
//      byte[] addr = null;
//      try {
//        addr = encodeSocketAddress(getServerAddress());
//      }catch (Exception e){e.printStackTrace();}
//
//      System.out.println("client thread now try to connect to server...");
//      int csuc=0;
//      while ( (csuc = connect(cfd, addr)) <0 ) {
//      }
//      assertThat(csuc, is(0));
//      System.out.println("client thread connected.");
//
//      byte[] cBuffer = new byte[(int)N];//allocate(N);
//      for (int i = 0; i < cBuffer.length; i++) {
//        cBuffer[i] = 66;
//      }
//
//      System.out.println("client thread will block on writing...");
//      long csize = writeH(cfd, cBuffer, 0, (int)N);
//      assertThat(csize,is(N));
//
//      //SendBuffer
//      int sb = SocketOptions.getOptSendBuffer(cfd);
//      assertThat(sb, greaterThan(1024));
//      System.out.println("SendBuffer:"+sb);//
//
//      System.out.println("client thread has sent out "+csize+" bytes data.");
////
////      //TODO: shutdown
////      csuc = sys_shutdown(cfd,Sockets.ShutDownType.SHUT_RD);
////      assertThat(csuc, is(0));
////
////      csuc = sys_shutdown(cfd,Sockets.ShutDownType.SHUT_WR);
////      assertThat(csuc, is(0));
////
////      //fail for repeating call?
////      csuc = sys_shutdown(cfd,Sockets.ShutDownType.SHUT_RDWR);
////      assertThat(csuc, is(0));
////
////      csuc = sys_close(cfd);
////      assertThat(csuc, is(0));
////      free(cBuffer);
//
//      uncheck(()->Thread.sleep(10_000L));
//      System.out.println("client thread done.");
//    }).start();
//    //======================================================
//
//  }
//
//
//  protected static SocketAddress getServerAddress() throws UnknownHostException {
//    return new InetSocketAddress(InetAddress.getByAddress(new byte[]{127, 0, 0, 1}), 12345);
//  }

}
