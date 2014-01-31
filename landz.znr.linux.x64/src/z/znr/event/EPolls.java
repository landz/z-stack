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

package z.znr.event;

import static z.offheap.zmalloc.Allocator.*;
import static z.znr.Syscall.*;

/**
 * sys/epoll.h
 */
public class EPolls {

  /* Valid opcodes ( "op" parameter ) to issue to epoll_ctl().  */
  /** Add a file descriptor to the interface.  */
  public static final int EPOLL_CTL_ADD = 1;
  /** Remove a file descriptor from the interface.  */
  public static final int EPOLL_CTL_DEL = 2;
  /** Change file descriptor epoll_event structure.  */
  public static final int EPOLL_CTL_MOD = 3;


  public static final int epollCreate() {
    return sys_epoll_create1(0);
  }


  public static final int epollAdd(int epfd, int fd, EPollEvent event) {
    return sys_epoll_ctl(epfd, EPOLL_CTL_ADD, fd, event.address());
  }

  public static final int epollAddFD(int epfd, int fd, int eventMask) {
    long event = allocate(EPollEvent.SIZE);
    EPollEvents.setEventMask(event, eventMask);
    EPollEvents.setFileDescriptor(event, fd);
    int errno = sys_epoll_ctl(epfd, EPOLL_CTL_ADD, fd, event);
    free(event);
    return errno;
  }

  public static final int epollAddPointer(
      int epfd, int fd, int eventMask, long pointer) {
    long event = allocate(EPollEvent.SIZE);
    EPollEvents.setEventMask(event, eventMask);
    EPollEvents.setPointer(event, pointer);
    int errno = sys_epoll_ctl(epfd, EPOLL_CTL_ADD, fd, event);
    free(event);
    return errno;
  }

  public static final int epollModify(int epfd, int fd, EPollEvent event) {
    return sys_epoll_ctl(epfd, EPOLL_CTL_MOD, fd, event.address());
  }

  public static final int epollModifyFD(int epfd, int fd, int eventMask) {
    long event = allocate(EPollEvent.SIZE);
    EPollEvents.setEventMask(event, eventMask);
    EPollEvents.setFileDescriptor(event, fd);
    int errno = sys_epoll_ctl(epfd, EPOLL_CTL_MOD, fd, event);
    free(event);
    return errno;
  }

  public static final int epollModifyPointer(
      int epfd, int fd, int eventMask, long pointer) {
    long event = allocate(EPollEvent.SIZE);
    EPollEvents.setEventMask(event, eventMask);
    EPollEvents.setPointer(event, pointer);
    int errno = sys_epoll_ctl(epfd, EPOLL_CTL_MOD, fd, event);
    free(event);
    return errno;
  }

  public static final int epollRemove(int epfd, int fd) {
    return sys_epoll_ctl(epfd, EPOLL_CTL_DEL, fd, 0L);
  }

  public static final int epollWait(int epfd, EPollEventArray events) {
    return epoll_wait(epfd, events, -1);
  }

  public static final int epollWaitNonBlock(int epfd, EPollEventArray events) {
    return epoll_wait(epfd, events, 0);
  }

  public static final int epollWait(
      int epfd, long events, int maxevents, int timeout) {
    return sys_epoll_wait(epfd, events, maxevents, timeout);
  }

  /**
   *
   * returns events about ready file descriptors from the epoll instance
   * referred to by the file descriptor epfd.
   *
   * @param timeout -
   *                –1, block until an event occurs or a signal is caught;
   *                0, perform a nonblocking check;
   *                greater than 0, block for up to timeout milliseconds,
   *                until an event occurs or a signal is caught;
   */
  public static final int epoll_wait(
      int epfd , EPollEventArray events , int timeout) {
    return sys_epoll_wait(
        epfd, events.address(), EPollEventArray.MAX_EVENTS, timeout);
  }

}
