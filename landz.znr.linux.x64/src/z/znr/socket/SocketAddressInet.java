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
   netinet/in.h
   struct sockaddr_in: 16B

   struct sockaddr_in {
    short int          sin_family;  //2B
    unsigned short int sin_port;    //2B
    struct in_addr     sin_addr;
    unsigned char      sin_zero[8]; //pad
   }

   struct in_addr {
     in_addr_t s_addr; //Unsigned 4B
   };

*/

/**
 * <p>
 * note:
 * SocketAddressInet is desgine as an immutable now
 */
public final class SocketAddressInet extends SocketAddress {
  private static final int SIZE = 16;

  public static final String LOOPBACK_ADDRESS = "127.0.0.1";
  public static final String ANY_ADDRESS = "0.0.0.0";

  private static final int SIN_FAMILY_OFFSET = 0;
  private static final int SIN_PORT_OFFSET   = 2;
  private static final int SIN_ADDR_OFFSET   = 4;

  /**
   * Note: we only support resolved IP address as address param. If you input
   *       the domain or localhost name, then an IllegalArgumentException will
   *       be thrown.
   *
   * @param addr - the IP address string in textual presentation.
   * @param port - the port number
   */
  public SocketAddressInet(String addr, int port) {
    super(SIZE);
    Sockets.encodeSockAddrBytes(address,Sockets.DomainFamily.INET,addr,port);
  }

  public SocketAddressInet(InetAddress address, int port) {
    this(address.getHostAddress(),port);
  }

  public int getFamily() {
    return getShort(address+SIN_FAMILY_OFFSET);
  }

  public int getPort() {
    return toUnsignedShort(getShortNonNative(address + SIN_PORT_OFFSET));
  }

  public byte[] getAddr() {
    byte[] addr = new byte[4];
    for (int i = 0; i < 4; i++) {
      addr[i] = get(address + SIN_ADDR_OFFSET + i);
    }
    return addr;
  }

  public int size() {
    return SIZE;
  }

//  public void setFamily(int family) {
//    UNSAFE.putByte(buffer + SIN_FAMILY_OFFSET, (byte) family);
//  }
//
//  public void setPort(int port) {
//    UNSAFE.putByte(buffer+SIN_PORT_OFFSET, (byte) ((port>>>8) & 0xff));
//    UNSAFE.putByte(buffer+SIN_PORT_OFFSET+1, (byte) (port & 0xff));
//  }
//
//  public void setAddr(byte[] addr) {
//    for (int i = 0; i < 4; i++) {
//      UNSAFE.putByte(buffer + SIN_ADDR_OFFSET + i, addr[i]);
//    }
//  }
//
//  public void setAddr(long addrBuffer) {
//    UNSAFE.copyMemory(addrBuffer, buffer + SIN_ADDR_OFFSET,4);
//  }

}
