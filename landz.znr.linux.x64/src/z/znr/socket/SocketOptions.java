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

import z.znr.SyscallInvocationException;

import static z.znr.Syscall.*;

/**
 */
public class SocketOptions {
  //asm-generic/socket.h
  public static final int SOL_SOCKET   = 1;

  public static final int SO_REUSEADDR = 2;
  public static final int SO_TYPE      = 3;
  public static final int SO_ERROR     = 4;
  public static final int SO_DONTROUTE = 5;
  public static final int SO_BROADCAST = 6;
  public static final int SO_SNDBUF    = 7;
  public static final int SO_RCVBUF    = 8;
  public static final int SO_KEEPALIVE = 9;
  public static final int SO_OOBINLINE = 10;
  public static final int SO_NO_CHECK  = 11;
  public static final int SO_PRIORITY  = 12;
  public static final int SO_LINGER    = 13;
  public static final int SO_BSDCOMPAT = 14;
  public static final int SO_REUSEPORT = 15;

  public static final int IPPROTO_TCP  = 6; /* Transmission Control Protocol.*/

  public static final int TCP_NODELAY              = 1;/* Don't delay send to coalesce packets  */
  public static final int TCP_MAXSEG               = 2;/* Set maximum segment size  */
  public static final int TCP_CORK                 = 3;/* Control sending of partial frames  */
  public static final int TCP_KEEPIDLE             = 4;/* Start keeplives after this period */
  public static final int TCP_KEEPINTVL            = 5;/* Interval between keepalives */
  public static final int TCP_KEEPCNT              = 6;/* Number of keepalives before death */
  public static final int TCP_SYNCNT               = 7;/* Number of SYN retransmits */
  public static final int TCP_LINGER2              = 8;/* Life time of orphaned FIN-WAIT-2 state */
  public static final int TCP_DEFER_ACCEPT         = 9;/* Wake up listener only when data arrive */
  public static final int TCP_WINDOW_CLAMP         = 10;/* Bound advertised window */
  public static final int TCP_INFO                 = 11;/* Information about this connection. */
  public static final int TCP_QUICKACK             = 12;/* Bock/reenable quick ACKs.  */
  public static final int TCP_CONGESTION           = 13;/* Congestion control algorithm.  */
  public static final int TCP_MD5SIG               = 14;/* TCP MD5 Signature (RFC2385) */
  public static final int TCP_COOKIE_TRANSACTIONS  = 15;/* TCP Cookie Transactions */
  public static final int TCP_THIN_LINEAR_TIMEOUTS = 16;/* Use linear timeouts for thin streams*/
  public static final int TCP_THIN_DUPACK          = 17;/* Fast retrans. after 1 dupack */
  public static final int TCP_USER_TIMEOUT         = 18;/* How long for loss retry before timeout */
  public static final int TCP_REPAIR               = 19;/* TCP sock is under repair right now */
  public static final int TCP_REPAIR_QUEUE         = 20;/* Set TCP queue to repair */
  public static final int TCP_QUEUE_SEQ            = 21;/* Set sequence number of repaired queue. */
  public static final int TCP_REPAIR_OPTIONS       = 22;/* Repair TCP connection options */
  public static final int TCP_FASTOPEN             = 23;/* Enable FastOpen on listeners */
  public static final int TCP_TIMESTAMP            = 24;/* TCP time stamp */

  public static final int IPPROTO_IP   = 0; /* Dummy protocol for TCP.  */
  public static final int IPPROTO_IPV6 = 41;/* IPv6 header.  */

  public static final boolean getOptReuseAddr(int sockfd) {
    int rt = sys_getsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR);
    return rt==0 ? false:true;
  }

  public static final void setOptReuseAddr(int sockfd, boolean enabled) {
    int optval = (enabled==true ? 1:0);
    int rt = sys_setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, optval);
    if (rt<0) {
      throw new SyscallInvocationException(
          "Call to sys_getsockopt failed with errno "+(-rt));
    }
  }

  public static final int getOptReceiveBuffer(int sockfd) {
    return sys_getsockopt(sockfd, SOL_SOCKET, SO_RCVBUF);
  }

  public static final void setOptReceiveBuffer(int sockfd, int size) {
    int rt = sys_setsockopt(sockfd, SOL_SOCKET, SO_RCVBUF, size);
    if (rt<0) {
      throw new SyscallInvocationException(
          "Call to sys_getsockopt failed with errno "+(-rt));
    }
  }

  public static final int getOptSendBuffer(int sockfd) {
    return sys_getsockopt(sockfd, SOL_SOCKET, SO_SNDBUF);
  }

  public static final void setOptSendBuffer(int sockfd, int size) {
    int rt = sys_setsockopt(sockfd, SOL_SOCKET, SO_SNDBUF, size);
    if (rt<0) {
      throw new SyscallInvocationException(
          "Call to sys_getsockopt failed with errno "+(-rt));
    }
  }

  public static final boolean getOptKeepAlive(int sockfd) {
    int rt = sys_getsockopt(sockfd, SOL_SOCKET, SO_KEEPALIVE);
    return rt==0 ? false:true;
  }

  public static final void setOptKeepAlive(int sockfd, boolean enabled) {
    int optval = (enabled==true ? 1:0);
    int rt = sys_setsockopt(sockfd, SOL_SOCKET, SO_KEEPALIVE, optval);
    if (rt<0) {
      throw new SyscallInvocationException(
          "Call to sys_getsockopt failed with errno "+(-rt));
    }
  }

  public static final boolean getOptTcpNoDelay(int sockfd) {
    int rt = sys_getsockopt(sockfd, IPPROTO_TCP, TCP_NODELAY);
    return rt==0 ? false:true;
  }

  public static final void setOptTcpNoDelay(int sockfd, boolean enabled) {
    int optval = (enabled==true ? 1:0);
    int rt = sys_setsockopt(sockfd, IPPROTO_TCP, TCP_NODELAY, optval);
    if (rt<0) {
      throw new SyscallInvocationException(
          "Call to sys_getsockopt failed with errno "+(-rt));
    }
  }



}
