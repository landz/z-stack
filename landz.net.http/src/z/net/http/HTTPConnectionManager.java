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

package z.net.http;

import static z.offheap.zmalloc.Allocator.allocate;
import static z.offheap.zmalloc.Allocator.free;
import static z.util.Unsafes.*;
import static z.net.NetModule.*;
import static z.net.AsyncIOThreadPool.*;

/**
 * FIXME:
 *   here thera are not any fence in that we assume we have somethings
 *   , like task queue, to guarantee this.
 */
public class HTTPConnectionManager implements AutoCloseable {

  private static final int SIZE_CONNECTION = SIZE_CACHE_LINE_PADDING;

//  private static final long OFFSET_CONNECTION_STATUS = 0;
  private static final long OFFSET_CONNECTION_BUFFER = 0;

//  public static final byte CONNECTION_STATUS_WORKING = 0;
//  public static final byte CONNECTION_STATUS_ERROR = 1;

  private final long connections;

  HTTPConnectionManager() {
    int size = MAX_SUPPORTED_SOCKS * SIZE_CONNECTION;
    this.connections = systemAllocateMemory(size);
    systemClearMemory(this.connections,size);
  }

  public long getConnectionBuffer(int fd) {
    long address = UNSAFE.getAddress(
        connections + fd * SIZE_CONNECTION + OFFSET_CONNECTION_BUFFER);
    if (address!=0L) {
      return address;
    } else {
      //HTTPConnectionManager-MEMCHK-1
      address = allocate(SIZE_NET_READ_BUFFER*2);
      UNSAFE.putAddress(
          connections + fd * SIZE_CONNECTION + OFFSET_CONNECTION_BUFFER,
          address);
      return address;
    }
  }


  public void clean(int fd) {
    long bufferAddr = UNSAFE.getAddress(
        connections + fd * SIZE_CONNECTION + OFFSET_CONNECTION_BUFFER);
    if (bufferAddr!=0L) {
      //HTTPConnectionManager-MEMCHK-1
      free(bufferAddr);
      UNSAFE.putAddress(
          connections + fd * SIZE_CONNECTION + OFFSET_CONNECTION_BUFFER, 0L);
    }
  }

  @Override
  /**
   * FIXME: this close method is guarded with checking, so double closing
   *       will kill the allocator...
   */
  public void close() {
    //TODO free all buffer individually?
    systemFreeMemory(connections);
  }
}
