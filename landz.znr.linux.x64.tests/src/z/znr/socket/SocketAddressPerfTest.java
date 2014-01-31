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

import org.junit.Test;
import org.xnio.nativeimpl.Native;
import sun.net.util.IPAddressUtil;
import z.util.primitives.Bytes;

import java.net.InetAddress;
import java.net.InetSocketAddress;


/**
 *
 * just for reference.
 * NOTE:
 * textToNumericFormatV4 is internally used by InetSocketAddress.
 *
 */
public class SocketAddressPerfTest {
  private static final long COUNT = 1000_000L;

  @Test
  public void benchAddrBytescalculation() throws Exception {
    InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName("1.2.3.4"),9);

    byte[] addr1=null, addr2=null, addr3 = null;

    addr1 = addr.getAddress().getAddress();
    addr2 = Native.encodeSocketAddress(addr);
    addr3 = IPAddressUtil.textToNumericFormatV4("1.2.3.4");//Sockets.encodeSockAddrBytes(Sockets.DomainFamily.INET, "1.2.3.4", 9);

    System.out.println("addr1: " + Bytes.asList(addr1));
    System.out.println("addr2: "+Bytes.asList(addr2));
    System.out.println("addr3: "+Bytes.asList(addr3));

    long s,t;

     s = System.nanoTime();
    for (long i = 0; i < COUNT; i++) {
      addr1 = addr.getAddress().getAddress();
    }
     t = System.nanoTime()-s;
    System.out.println("addr1 cost:" + t);

     s = System.nanoTime();
    for (long i = 0; i < COUNT; i++) {
      addr2 = Native.encodeSocketAddress(addr);
    }
    t = System.nanoTime()-s;
    System.out.println("addr2 cost:" + t);

     s = System.nanoTime();
    for (long i = 0; i < COUNT; i++) {
      addr3 = IPAddressUtil.textToNumericFormatV4("1.2.3.4");
    }
     t = System.nanoTime()-s;
    System.out.println("addr3 cost:" + t);

  }

}
