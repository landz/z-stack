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

import org.junit.Test;
import z.function.Pipeline;
import z.offheap.buffer.Buffer;
import z.offheap.buffer.Buffers;
import z.offheap.buffer.ByteBuffer;

import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import static z.offheap.zmalloc.Allocator.*;
import static z.util.Throwables.uncheck;
import static z.znr.socket.Sockets.*;
import static z.znr.socket.SocketAddressInet.*;

/**
 * Created by jin on 1/9/14.
 */
public class NetModuleTest {

  @Test
  public void basicTest() {
    int port = 12345;

    on(LOOPBACK_ADDRESS, port,
        Pipeline
            .create((PipelineContext ctx) -> {
              ByteBuffer in  = ctx.inBuffer;
              ByteBuffer out = ctx.outBuffer;
              StringBuffer sb = new StringBuffer();
              while (in.readableBytes()!=0) {
                byte v = in.read();
                out.write(v);
                sb.append((char)v);
              }
              //append a random long to the return msg
              long rlong = ThreadLocalRandom.current().nextLong();
              out.networkOrder().writeLongN(rlong);
              System.out.println("@@@server will return back:"+sb+rlong);
              return ctx;
            })
            .end());
    
    System.out.println("net server has started on "+ LOOPBACK_ADDRESS + " with port " + port);

    uncheck(() -> Thread.sleep(3_000L));

    int fd = socketTcp(); System.out.println("current client socket: "+fd);

    connectTo(fd, LOOPBACK_ADDRESS, port);

    sendmsg(fd, "hello, world!");
    recvmsg(fd);

    sendmsg(fd, "hello, java8!");
    recvmsg(fd);

    sendmsg(fd, "hello, landz!");
    recvmsg(fd);

    sendmsg(fd, "hello, hello!");
    recvmsg(fd);

    shutdown(fd);

  }

  public static final void on(
      String ipAddress, int port,
      Pipeline<PipelineContext, PipelineContext> pipeline) {
    AsyncIOThreadPool asyncIOPool =
        new AsyncIOThreadPool(1, pipeline, fd->{});
    NetModule net = new NetModule(asyncIOPool);
    net.startServer(ipAddress,port);
  }


  public void connectTo(int fd, String ipAddress, int port) {
    System.out.println("+++current thread now connects to server...");
    int suc = connect(fd, ipAddress, port);
    assertThat(suc, is(0));
    System.out.println("+++socket["+fd+"] connected.");
  }

  public void sendmsg(int fd, String msg) {
    System.out.println("--->start to send msg:");
    int msgLength = msg.length();

    long cBuffer = allocate(msgLength);

    for (int i = 0; i < msgLength; i++) {
      Buffers.put(cBuffer + i, (byte) msg.charAt(i));
    }

    System.out.println("--->current thread will block on writing...");
    long csize = write(fd, cBuffer, msgLength);
    assertThat((int)csize,  is(msgLength));
    free(cBuffer);
    System.out.println("--->msg has been sent.");
  }

  public void recvmsg(int fd) {
    System.out.println("<---start to receive msg:");
    int msgLength = 1024;
    try (ByteBuffer rb = Buffer.create(msgLength)) {
      System.out.println("<---current thread will block on writing...");
      long n = read(fd, rb.address(), msgLength);
      ((Buffer) rb).skipWriteTo(n);

      StringBuilder sb = new StringBuilder();
      while (rb.readableBytes() != 0) {
        if (rb.readableBytes() != 8) {
          sb.append((char) rb.read());
        } else {
          sb.append(rb.networkOrder().readLongN());
        }
      }
      assertThat((int) n, is(13 + 8));
      System.out.println("<---msg[" + sb + "] has been received.");
    }
  }

  public void shutdown(int fd) {
    System.out.println("===current thread will wait 3s to shutdown/close...");
    uncheck(() -> Thread.sleep(3_000L));
    int suc = shutdownWrite(fd);
    assertThat(suc, is(0));
    suc = close(fd);
    assertThat(suc, is(0));
    //
    System.out.println("===client thread done.");
  }


}
