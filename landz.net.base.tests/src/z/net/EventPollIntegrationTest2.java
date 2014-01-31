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

package z.net;

import org.junit.Test;
import z.znr.event.EPollEvent;
import z.znr.event.EPollEvents;
import z.znr.event.EPolls;
import z.znr.socket.SocketAddressInet;
import z.znr.socket.Sockets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static z.offheap.zmalloc.Allocator.allocate;
import static z.offheap.zmalloc.Allocator.free;
import static z.util.Throwables.uncheck;
import static z.util.Unsafes.UNSAFE;

import static z.znr.Syscall.*;
import static z.znr.event.EPollEvents.*;
import static z.znr.socket.Sockets.*;

/**
 *
 * from the testcase, we got following conclusions
 * for epoll in ET/Oneshot mode(kernel 3.12.7):
 *
 * 1. the read/write-already-ready socket will be notified after
 *    concerning(adding/modifying) read/write on it;
 * 2. if you do not consume the notified events(include but not limit to, read or write something),
 *    they will be still reported after you re-concern these events;(this shows some consistence with #1)
 *    (This character may be benefited from Oneshot?)
 *
 * 3. if the peer client socket have been in write-shutdown(SHUT_WR) or closed,
 *    the concerning read/write will always return immediately;
 *    ("SHUT_RD can’t be used meaningfully for TCP sockets")
 * 4. the return mask may include EPOLLIN, EPOLLOUT, EPOLLRDHUP, EPOLLHUP, EPOLLERR
 *    but not include EPOLLET, EPOLLONESHOT.
 * 5. if concerns two or more event masks would cause to return the actual ready event masks.
 *    That is, not-happened concern will not return for you.(this shows some consistence with #4)
 *    But, which belongs to the actual ready event masks is subtle:
 *    5a. if you meet the kinds of event out of the concerned masks, you will fail to
 *        recognize the event because the return event masks is in your previous concerned masks;
 *    5b. some cases:
 *        a> if connected sockets do not send anything, you may still get EPOLLOUT;
 *        b> if EPOLLIN and EPOLLOUT will always be immediately returned as well if
 *            concerned previously when the peer socket shutdown/closed, but the
 *            EPOLLRDHUP should be truely concerned;
 *
 */
public class EventPollIntegrationTest2 {
  static {
    System.setProperty("jnr.invoke.compile.dump", "false");
  }

  public static final String MSG = "world";


  @Test
  public void test_syscall_socket() {
    int serverfd, suc, clientfd;
    long size;
    EventPoll eventPoll = new EventPoll();

    serverfd =
        sys_socket(
            Sockets.DomainFamily.INET,
            Sockets.SocketType.SOCK_STREAM,
            0);
    assertThat(serverfd,  greaterThan(0));
//    System.out.println(sockfd);

    SocketAddressInet addr = new SocketAddressInet(SocketAddressInet.LOOPBACK_ADDRESS,12345);

    suc = bind(serverfd, addr);
    assertThat(suc, is(0));

    suc = sys_listen(serverfd, 10);
    assertThat(suc, is(0));

    suc = eventPoll.addForRead(serverfd);
    assertThat(suc, is(0));

    //client
    spawnClientThread(addr);
    //

//    for (; ; ) {
      System.out.println("wait on the epoll instance: " + eventPoll);
      EventArray events = eventPoll.poll();
      assertThat(events, notNullValue());

      System.out.println("got " + events.availableNumEvents + " ready fd now.");//TODO: sometimes n=0(signal)

      for (int i = 0; i < events.availableNumEvents; i++) {
        long addrEvent = events.getEventAddress(i);
        long mask = EPollEvents.getEventMask(addrEvent);
        long pointer = EPollEvents.getPointer(addrEvent);
        assertThat((int)pointer,  is(serverfd));

        System.out.println("server thread will block on accepting...");
        clientfd = acceptBlock(serverfd);
        assertThat(clientfd,  greaterThan(0));
        System.out.println("server thread got a clientfd:" + clientfd);

        //=================================================
        // wait 5s for read-ready status has been arrived before concerning
        System.out.println("+++++wait 6s...");
        uncheck(() -> Thread.sleep(6_000L));
        System.out.println("+++++wait 6s done...");

        long event = allocate(EPollEvent.SIZE);
        EPollEvents.setEventMask(event, EPOLLERR | EPOLLHUP | EPOLLIN | EPOLLOUT | EPOLLRDHUP | EPOLLET | EPOLLONESHOT);
        EPollEvents.setPointer(event , clientfd);
        int errno = sys_epoll_ctl(eventPoll.epfd, EPolls.EPOLL_CTL_ADD, clientfd, event);
        free(event);
        assertThat(suc,  is(0));

        System.out.println("1st polling for addForRead:");

        EventArray es = eventPoll.poll();
        assertThat(events.availableNumEvents,is(1));
        addrEvent = es.getEventAddress(0);
        int msk = EPollEvents.getEventMask(addrEvent);
        long pt = EPollEvents.getPointer(addrEvent);
        assertThat((int)pt,  is(clientfd));
        System.out.println("event mask is:"+msk);
        assertThat(msk, is(EPOLLIN | EPOLLOUT | EPOLLRDHUP));
        System.out.println("can be writen with number of events:"+es.availableNumEvents);

        int fd = (int)pt;
        long sBuffer = allocate(8);//to hold "hello"
        size = sys_read(fd, sBuffer, 4);
        assertThat(size,  is(0L));//nothing to read

        System.out.println("server thread come back.");
        es.close();

        StringBuilder msg = new StringBuilder(5);
        for (int j = 0; j < size; j++) {
          msg.append((char) UNSAFE.getByte(sBuffer + j));
        }

        System.out.println("and we got msg:" + msg);
//         assertThat(msg.toString(),  is(MSG));

        //==========================================
        suc = sys_close(serverfd);
         assertThat(suc,  is(0));
        free(sBuffer);
        System.out.println("whole test done.");
      }

    events.close();

//    }

  }


  private void spawnClientThread(SocketAddressInet addr) {

    //client thread
    new Thread(()->{
      uncheck(() -> Thread.sleep(3_000L));

      System.out.println("client thread start to work...");
      int cfd =
          sys_socket(
              Sockets.DomainFamily.INET,
              Sockets.SocketType.SOCK_STREAM,
              0);

      System.out.println("client thread now try to connect to server...");
      int csuc = connect(cfd, addr);
       assertThat(csuc,  is(0));
      System.out.println("client thread connected.");

//      long cBuffer = allocate(8);
//
//      for (int i = 0; i < MSG.length(); i++) {
//        UNSAFE.putByte(cBuffer+i, (byte)MSG.charAt(i));
//      }
//
//      System.out.println("client thread will block on writing...");
//      long csize = sys_write(cfd, cBuffer, 5);
//       assertThat(csize,  is(5L));
//      System.out.println("client thread come back.");
//      free(cBuffer);

      System.out.println("client thread wait 2s to shutdown/close...");
      uncheck(() -> Thread.sleep(2_000L));
      //TODO: shutdown
//      csuc = sys_shutdown(cfd, Sockets.ShutDownType.SHUT_RD);
//       assertThat(csuc,  is(0));
//      System.out.println("shutdown read");
//
//      csuc = sys_shutdown(cfd, Sockets.ShutDownType.SHUT_WR);
//       assertThat(csuc,  is(0));
//      System.out.println("shutdown write");

//      csuc = sys_shutdown(cfd, Sockets.ShutDownType.SHUT_RDWR);
//       assertThat(csuc,  is(0));
//      System.out.println("shutdown close");

      csuc = sys_close(cfd);
       assertThat(csuc, is(0));
      //
      System.out.println("client thread done.");
    }).start();

  }


}
