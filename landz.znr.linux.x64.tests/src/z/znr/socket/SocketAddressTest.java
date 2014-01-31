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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import z.util.primitives.Bytes;

import java.net.InetAddress;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static z.znr.socket.Sockets.DomainFamily.*;
/**
 */
public class SocketAddressTest {
  @Test
  public void testSockAddr() throws Exception {
    SocketAddressInet addr = new SocketAddressInet("127.0.0.1",12345);
    System.out.println("addr:"+ Bytes.asList(addr.getAddr()));
    System.out.println("port:"+ addr.getPort());

    assertThat(addr.getFamily(), is (INET));
    assertThat(addr.getAddr(), is(new byte[]{127,0,0,1}));
    assertThat(addr.getPort(), is (12345));

    //
    addr = new SocketAddressInet("0.0.0.0",80);
    System.out.println("addr:"+ Bytes.asList(addr.getAddr()));
    System.out.println("port:"+ addr.getPort());

    assertThat(addr.getFamily(), is (INET));
    assertThat(addr.getAddr(), is(new byte[]{0,0,0,0}));
    assertThat(addr.getPort(), is (80));

    //
    addr = new SocketAddressInet("254.255.254.255",60000);
    System.out.println("addr:"+ Bytes.asList(addr.getAddr()));
    System.out.println("port:"+ addr.getPort());

    assertThat(addr.getFamily(), is (INET));
    assertThat(addr.getAddr(), is(new byte[]{(byte)254,(byte)255,(byte)254,(byte)255}));
    assertThat(addr.getPort(), is (60000));

    //
    addr = new SocketAddressInet(InetAddress.getByName("254.255.254.255"),33333);
    System.out.println("addr:"+ Bytes.asList(addr.getAddr()));
    System.out.println("port:"+ addr.getPort());

    assertThat(addr.getFamily(), is (INET));
    assertThat(addr.getAddr(), is(new byte[]{(byte)254,(byte)255,(byte)254,(byte)255}));
    assertThat(addr.getPort(), is (33333));

    //
    addr = new SocketAddressInet(InetAddress.getByName("localhost"),33333);
    System.out.println("addr:"+ Bytes.asList(addr.getAddr()));
    System.out.println("port:"+ addr.getPort());

    assertThat(addr.getFamily(), is (INET));
    assertThat(addr.getAddr(), is(new byte[]{127,0,0,1}));
    assertThat(addr.getPort(), is (33333));

    //
    addr = new SocketAddressInet(InetAddress.getLoopbackAddress(),33333);
    System.out.println("addr:"+ Bytes.asList(addr.getAddr()));
    System.out.println("port:"+ addr.getPort());

    assertThat(addr.getFamily(), is (INET));
    assertThat(addr.getAddr(), is(new byte[]{127,0,0,1}));
    assertThat(addr.getPort(), is (33333));

    //
    addr = new SocketAddressInet(SocketAddressInet.ANY_ADDRESS,33333);
    System.out.println("addr:"+ Bytes.asList(addr.getAddr()));
    System.out.println("port:"+ addr.getPort());

    assertThat(addr.getFamily(), is (INET));
    assertThat(addr.getAddr(), is(new byte[]{0,0,0,0}));
    assertThat(addr.getPort(), is (33333));

    //====================================================================
    SocketAddressInet6 addr6 = new SocketAddressInet6("::1",55555);
    System.out.println("addr:"+ Bytes.asList(addr6.getAddr()));
    System.out.println("port:"+ addr6.getPort());

    assertThat(addr6.getFamily(), is (INET6));
    assertThat(addr6.getAddr(), is(new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1}));
    assertThat(addr6.getPort(), is (55555));

    addr6 = new SocketAddressInet6(SocketAddressInet6.LOOPBACK_ADDRESS,55555);
    System.out.println("addr:"+ Bytes.asList(addr6.getAddr()));
    System.out.println("port:"+ addr6.getPort());

    assertThat(addr6.getFamily(), is (INET6));
    assertThat(addr6.getAddr(), is(new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1}));
    assertThat(addr6.getPort(), is (55555));

    addr6 = new SocketAddressInet6(SocketAddressInet6.ANY_ADDRESS,1024);
    System.out.println("addr:"+ Bytes.asList(addr6.getAddr()));
    System.out.println("port:"+ addr6.getPort());

    assertThat(addr6.getFamily(), is (INET6));
    assertThat(addr6.getAddr(), is(new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}));
    assertThat(addr6.getPort(), is (1024));


    //
    addr6 = new SocketAddressInet6("abcd::1",666);
    System.out.println("addr:"+ Bytes.asList(addr6.getAddr()));
    System.out.println("port:"+ addr6.getPort());

    assertThat(addr6.getFamily(), is (INET6));
    assertThat(addr6.getAddr(), is(new byte[]{(byte)0xab,(byte)0xcd,0,0,0,0,0,0,0,0,0,0,0,0,0,1}));
    assertThat(addr6.getPort(), is (666));

  }

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testSockAddrException1() {
    //
    exception.expect(IllegalArgumentException.class);
    SocketAddressInet addr = new SocketAddressInet("localhost",1024);
  }

  @Test
  public void testSockAddrException2() {
    //
    exception.expect(IllegalArgumentException.class);
    SocketAddressInet addr = new SocketAddressInet("google.com",1024);
  }


  @Test
  public void testSockAddrException3() {
    //
    exception.expect(IllegalArgumentException.class);
    SocketAddressInet6 addr = new SocketAddressInet6("google.com",1024);
  }

}
