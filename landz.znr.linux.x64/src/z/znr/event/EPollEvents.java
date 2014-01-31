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

import static z.util.Unsafes.UNSAFE;

/**
 * EPollEvent related constants, dup from sys/epoll.h
 */
public class EPollEvents {
  //TODO: javadoc

  public static final int EPOLLIN = 0x001;
  public static final int EPOLLPRI = 0x002;
  public static final int EPOLLOUT = 0x004;
  public static final int EPOLLRDNORM = 0x040;
  public static final int EPOLLRDBAND = 0x080;
  public static final int EPOLLWRNORM = 0x100;
  public static final int EPOLLWRBAND = 0x200;
  public static final int EPOLLMSG = 0x400;
  public static final int EPOLLERR = 0x008;
  public static final int EPOLLHUP = 0x010;
  public static final int EPOLLRDHUP = 0x2000;
  public static final int EPOLLWAKEUP = 1 << 29;
  public static final int EPOLLONESHOT = 1 << 30;

  /**
   * Its value is, in fact, same to Integer.MIN_VALUE in Java.
   */
  public static final int EPOLLET = 1 << 31;


  public static final int getEventMask(long addressEvent) {
    return UNSAFE.getInt(addressEvent);
  }

  public static final void setEventMask(long addressEvent, int eventMask) {
    UNSAFE.putInt(addressEvent, eventMask);
  }

  public static final int getFileDescriptor(long addressEvent) {
    return UNSAFE.getInt(addressEvent + EPollEvent.OFFSET_EVENT_DATA);
  }

  public static final void setFileDescriptor(long addressEvent, int fd) {
    UNSAFE.putInt(addressEvent + EPollEvent.OFFSET_EVENT_DATA, fd);
  }

  public static final long getPointer(long addressEvent) {
    return UNSAFE.getLong(addressEvent + EPollEvent.OFFSET_EVENT_DATA);
  }

  public static final void setPointer(long addressEvent, long pointer) {
    UNSAFE.putLong(addressEvent + EPollEvent.OFFSET_EVENT_DATA, pointer);
  }


}
