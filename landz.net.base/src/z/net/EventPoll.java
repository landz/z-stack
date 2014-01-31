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

import z.znr.event.EPollEvent;
import z.znr.event.EPollEventArray;
import z.znr.socket.Sockets;

import static z.offheap.zmalloc.Allocator.*;
import static z.util.Contracts.*;
import static z.znr.event.EPolls.*;
import static z.znr.event.EPollEvents.*;

/**
 * note: EventPoll only provides one layer for the convenience of landz's net
 *       module, so it will not provide all options for socket side. Especially,
 *       all events in this class are concerned with EPOLLET and EPOLLONESHOT
 *       flag.
 *       Please, directly use {@link z.znr.event.EPolls} for your other
 *       requirements.
 */
public class EventPoll implements AutoCloseable {

  public static final int MASK_READ  = EPOLLIN | EPOLLRDHUP
      | EPOLLERR | EPOLLHUP | EPOLLET | EPOLLONESHOT;

  public static final int MASK_WRITE = EPOLLOUT | EPOLLRDHUP
      | EPOLLERR | EPOLLHUP | EPOLLET | EPOLLONESHOT;

  public static final int MASK_ALL   = EPOLLIN | EPOLLOUT | EPOLLRDHUP
      | EPOLLERR | EPOLLHUP | EPOLLET | EPOLLONESHOT;


  final int epfd;
//  private final long addressState;

  public EventPoll() {
    this.epfd = epollCreate();
    contract(()-> epfd>0);
//    this.addressState = allocate(INITIAL_CONCERN_SOCKS);
//    systemClearMemory(addressState,INITIAL_CONCERN_SOCKS);
  }

  public int addForRead(int fd) {
    return epollAddFD(epfd,fd, MASK_READ );
  }

  public int addForRead(int fd, long pointer) {
    return epollAddPointer(epfd, fd, MASK_READ, pointer);
  }

  public int concernRead(int fd) {
    return epollModifyFD(epfd, fd, MASK_READ);
  }

  public int concernRead(int fd, long pointer) {
    return epollModifyPointer(epfd, fd, MASK_READ, pointer);
  }

  public int concernWrite(int fd) {
    return epollModifyFD(epfd, fd, MASK_WRITE);
  }

  public int concernWrite(int fd, long pointer) {
    return epollModifyPointer(epfd, fd, MASK_WRITE, pointer);
  }

  public int concernReadWrite(int fd) {
    return epollModifyFD(epfd, fd, MASK_ALL);
  }

  public int concernReadWrite(int fd, long pointer) {
    return epollModifyPointer(epfd, fd, MASK_ALL, pointer);
  }

  public int remove(int fd) {
    return epollRemove(epfd, fd);
  }

  //========================TODO

  /*
   * If the event mask does not contain any poll(2) event, we consider the
   * descriptor to be disabled. This condition is likely the effect of the
   * EPOLLONESHOT bit that disables the descriptor when an event is received,
   * until the next EPOLL_CTL_MOD will be issued.
   */
  public int unconcern(int fd) {
    return epollModifyFD(epfd, fd, 0);
  }

  public EventArray poll() {
    return poll(-1);
  }

  public EventArray pollNonBlock() {
    return poll(0);
  }

  public EventArray poll(int timeout) {
    //MEMCHK: release by client
    long addrEvents = allocate(EPollEvent.SIZE * EPollEventArray.MAX_EVENTS);
    int n = epollWait(epfd, addrEvents, EPollEventArray.MAX_EVENTS, timeout);
    if (n>0) {
      return new EventArray(addrEvents,EPollEventArray.MAX_EVENTS,n);
    } else {
      free(addrEvents);
      return null;
    }
  }


//  public int concernCheckedRead(int fd) {
//    //TODO: contract?
//    if (fd > MAX_CONCERN_SOCKS) {
//      throw new IllegalArgumentException( String.format(
//          "fd(=%,d) is larger than MAX_CONCERN_SOCKS %,d",
//          fd, MAX_CONCERN_SOCKS) );
//    }
//    long index = addressState+fd*4;
//    int state = UNSAFE.getInt(index);
//    int suc = 0;
//    if ( state == 0) {
//      suc = epollAddFD(epfd,fd,EPOLLIN);
//      if (suc==0) {
//        UNSAFE.putInt(index, EPOLLIN);
//      }
//    }else if ((state & EPOLLIN)==0) {
//      int newState = state|EPOLLIN;
//      suc = epollModifyFD(epfd,fd,newState);
//      if (suc==0) {
//        UNSAFE.putInt(index, newState);
//      }
//    }
//    return suc;
//  }
//
//
//  public int unconcernCheckedRead(int fd) {
//    //TODO: contract?
//    if (fd > MAX_CONCERN_SOCKS) {
//      throw new IllegalArgumentException( String.format(
//          "fd(=%,d) is larger than MAX_CONCERN_SOCKS %,d",
//          fd, MAX_CONCERN_SOCKS) );
//    }
//    long index = addressState+fd*4;
//    int state = UNSAFE.getInt(index);
//    int suc = 0;
//    if ( state != 0) {
//      if (state == EPOLLIN) {
//        suc = epollRemove(epfd,fd);
//        if (suc==0) {
//          UNSAFE.putInt(index, 0);
//        }
//      } else {
//        int newState = state^EPOLLIN;
//        suc = epollModifyFD(epfd,fd,newState);
//        if (suc==0) {
//          UNSAFE.putInt(index, newState);
//        }
//      }
//    }
//    return suc;
//  }


  @Override
  public void close() {
    Sockets.close(epfd);//ignore the errno
  }
}
