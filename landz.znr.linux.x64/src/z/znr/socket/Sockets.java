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

package z.znr.socket;

import sun.net.util.IPAddressUtil;

import static z.util.Unsafes.UNSAFE;
import static z.znr.Syscall.*;
import static z.znr.Syscall.sys_bind;
import static z.znr.socket.SocketOptions.*;
/**
 * bits/socket.h, bits/socket_type.h
 */
public final class Sockets {

  public final static class DomainFamily {
    /* IP protocol family.  */
    public static final int INET  = 2;
    /* IP version 6.  */
    public static final int INET6 = 10;
  }

  public static final class SocketType {
    /* Sequenced, reliable, connection-based byte streams.  */
    public static final int SOCK_STREAM = 1;
    /* Connectionless, unreliable datagrams of fixed maximum length.  */
    public static final int SOCK_DGRAM  = 2;

    /* Atomically set close-on-exec flag for the new descriptor(s).  */
    public static final int SOCK_CLOEXEC = 02000000;
    /* Atomically mark descriptor(s) as non-blocking.  */
    public static final int SOCK_NONBLOCK = 00004000;
  }

  //sys/socket.h
  public static final class ShutDownType {
    /** No more receptions.  */
    public static final int SHUT_RD   = 0;
    /** No more transmissions.  */
    public static final int SHUT_WR   = 1;
    /** No more receptions or transmissions.*/
    public static final int SHUT_RDWR = 2;
  }


  public static final int socketTcp() {
    return sys_socket(DomainFamily.INET, SocketType.SOCK_STREAM, IPPROTO_IP);
  }

  public static final int bind(int sockfd, String ipAddress, int port) {
    try ( SocketAddressInet addr =
              new SocketAddressInet(ipAddress, port) ) {
      return bind(sockfd, addr);
    }
  }

  public static final int bind(int sockfd, SocketAddress addr) {
    return sys_bind(sockfd, addr.address(), addr.size());
  }

  public static final int connect(int sockfd, String ipAddress, int port) {
    try ( SocketAddressInet addr =
              new SocketAddressInet(ipAddress, port) ) {
      return connect(sockfd, addr);
    }
  }

  public static final int connect(int sockfd, SocketAddress addr) {
    return sys_connect(sockfd, addr.address(), addr.size());
  }

  public static final int listen(int sockfd, int backlog) {
    return sys_listen(sockfd, backlog);
  }

  //bits/socket.h
  //TODO: set to a larger?
  private static final int SOMAXCONN = 128;

  public static final int listen(int sockfd) {
    return sys_listen(sockfd, SOMAXCONN);
  }

  public static final int acceptNonBlock(int sockfd) {
    return sys_accept4(sockfd, 0L, 0, SocketType.SOCK_NONBLOCK);
  }

  public static final int acceptBlock(int sockfd) {
    return sys_accept4(sockfd, 0L, 0, 0);
  }

  public static final int accept(int sockfd, SocketAddress addr, int flags) {
    return sys_accept4(sockfd, addr.address(), addr.size(), flags);
  }

  public static final int shutdownWrite(int sockfd) {
    return sys_shutdown(sockfd, ShutDownType.SHUT_WR);
  }

  public static final int shutdownAll(int sockfd) {
    return sys_shutdown(sockfd, ShutDownType.SHUT_RDWR);
  }

  public static final int close(int sockfd) {
    return sys_close(sockfd);
  }

  public static final long read(int fd, long address, long count) {
    return sys_read(fd,address,count);
  }

  public static final long write(int fd, long address, long count) {
    return sys_write(fd,address,count);
  }


  //TODO: this may be changed to be more efficient in the future
  public static final byte[] encodeSockAddrBytes(
      int domain, String address, int port) {
    if (domain==DomainFamily.INET) {
      byte[] rt = new byte[16];

      rt[0] = DomainFamily.INET;

      rt[2] = (byte) ((port>>>8) & 0xff);
      rt[3] = (byte) (port & 0xff);

      byte[] ip = IPAddressUtil.textToNumericFormatV4(address);
      rt[4] = ip[0];
      rt[5] = ip[1];
      rt[6] = ip[2];
      rt[7] = ip[3];

      return rt;

    } else if (domain==DomainFamily.INET6) {
      byte[] rt = new byte[28];

      rt[0] = DomainFamily.INET6;

      rt[2] = (byte) ((port>>>8) & 0xff);
      rt[3] = (byte) (port & 0xff);

      //TODO: sin6_flowinfo
      rt[4] = 0;
      rt[5] = 0;
      rt[6] = 0;
      rt[7] = 0;

      byte[] ipv6 = IPAddressUtil.textToNumericFormatV6(address);
      System.arraycopy(rt,8,ipv6,0,ipv6.length);

      //TODO: sin6_scope_id
      rt[24] = 0;
      rt[25] = 0;
      rt[26] = 0;
      rt[27] = 0;

      return rt;
    } else {
      throw new IllegalArgumentException(
          "can understand the socket domain family");
    }
  }


  public static final void encodeSockAddrBytes(
      long address, int domain, String addr, int port) {
    if (domain==DomainFamily.INET) {
      UNSAFE.putByte(address,(byte)DomainFamily.INET);
      UNSAFE.putByte(address+1,(byte)0);

      UNSAFE.putByte(address+2, (byte) ((port>>>8) & 0xff));
      UNSAFE.putByte(address+3, (byte) (port & 0xff));

      byte[] ip = IPAddressUtil.textToNumericFormatV4(addr);
      if (ip==null) {
        throw new IllegalArgumentException(
            "The socket address can not be recognized.");
      }

      UNSAFE.putByte(address+4, ip[0]);
      UNSAFE.putByte(address+5, ip[1]);
      UNSAFE.putByte(address+6, ip[2]);
      UNSAFE.putByte(address+7, ip[3]);
      //TODO is padded value significant?

    } else if (domain==DomainFamily.INET6) {
      UNSAFE.putByte(address,(byte)DomainFamily.INET6);
      UNSAFE.putByte(address+1,(byte)0);

      UNSAFE.putByte(address+2, (byte) ((port>>>8) & 0xff));
      UNSAFE.putByte(address+3, (byte) (port & 0xff));

      //TODO: sin6_flowinfo
      UNSAFE.putByte(address+4, (byte)0);
      UNSAFE.putByte(address+5, (byte)0);
      UNSAFE.putByte(address+6, (byte)0);
      UNSAFE.putByte(address+7, (byte)0);

      byte[] ipv6 = IPAddressUtil.textToNumericFormatV6(addr);
      if (ipv6==null) {
        throw new IllegalArgumentException(
            "The socket address can not be recognized.");
      }

      for (int i = 0; i < 16; i++) {
        UNSAFE.putByte(address+i+8, ipv6[i]);
      }

      //TODO: sin6_scope_id
      UNSAFE.putByte(address+24, (byte)0);
      UNSAFE.putByte(address+25, (byte)0);
      UNSAFE.putByte(address+26, (byte)0);
      UNSAFE.putByte(address+27, (byte)0);

    } else {
      throw new IllegalArgumentException(
          "can understand the socket domain family");
    }
  }

}
