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


import java.net.InetAddress;

import static z.offheap.buffer.Buffers.*;
/*
   netinet/in6.h
   struct sockaddr_in: 28B

   struct sockaddr_in6 {
     sa_family_t     sin6_family;      //2B
     in_port_t       sin6_port;        //2B
     uint32_t        sin6_flowinfo;    //4B
     struct in6_addr sin6_addr;
     uint32_t        sin6_scope_id;    //4B
   }
   struct in6_addr {
     uint8_t s6_addr[16]; //Unsigned 16B
   }

*/

/**
 * note:
 * SocketAddressInet is desgine as an immutable now
 */
public final class SocketAddressInet6 extends SocketAddress {
  private static final int SIZE = 28;

  public static final String LOOPBACK_ADDRESS = "::1";
  public static final String ANY_ADDRESS = "::";

  private static final int SIN6_FAMILY_OFFSET   = 0;
  private static final int SIN6_PORT_OFFSET     = 2;
  private static final int SIN6_FLOWINFO_OFFSET = 4;
  private static final int SIN6_ADDR_OFFSET     = 8;
  private static final int SIN6_SCOPE_ID_OFFSET = 24;

  /**
   * Note: we only support resolved IP address as address param. If you input
   *       the domain or localhost name, then an IllegalArgumentException will
   *       be thrown.
   *
   * @param addr - the IP address string in textual presentation.
   * @param port - the port number
   */
  public SocketAddressInet6(String addr, int port) {
    super(SIZE);
    Sockets.encodeSockAddrBytes(address,Sockets.DomainFamily.INET6,addr,port);
  }

  public SocketAddressInet6(InetAddress address, int port) {
    this(address.getHostAddress(),port);
  }


  public int getFamily() {
    return getShort(address + SIN6_FAMILY_OFFSET);
  }

  public int getPort() {
    return toUnsignedShort(getShortNonNative(address + SIN6_PORT_OFFSET));
  }

  public int getFlowinfo() {
    return 0;//TODO
  }

  public int getScopeId() {
    return 0;//TODO
  }

  public byte[] getAddr() {
    byte[] addr = new byte[16];
    for (int i = 0; i < 16; i++) {
      addr[i] = get(address + SIN6_ADDR_OFFSET + i);
    }
    return addr;
  }

  public int size() {
    return SIZE;
  }

//  public void setFamily(int family) {
//
//  }
//
//  public void setPort(int port) {
//
//  }
//
//  public void setFlowinfo(int flowinfo) {
//
//  }
//
//  public void setAddr(byte[] addr) {
//
//  }
//
//  public void setScopeId(int scopeId) {
//
//  }

}
