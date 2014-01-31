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

package z.net.stress;

import org.junit.Test;
import z.function.Pipeline;
import z.net.AsyncIOThreadPool;
import z.net.NetModule;
import z.net.PipelineContext;
import z.offheap.buffer.Buffer;
import z.offheap.buffer.Buffers;
import z.offheap.buffer.ByteBuffer;
import z.offheap.zmalloc.Allocator;
import z.util.Unsafes;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static z.offheap.zmalloc.Allocator.allocate;
import static z.offheap.zmalloc.Allocator.free;
import static z.util.Throwables.uncheck;
import static z.znr.socket.SocketAddressInet.LOOPBACK_ADDRESS;
import static z.znr.socket.Sockets.*;

/**
 * in this test: 7 client threads / 7 server threads
 */
public class NetModuleTestMT03 {

  public static final long RUNS = 10_000_000L;

  final Set<Long> tidSet = new ConcurrentSkipListSet();

  @Test
  public void basicTest() {
    int port = 12345;
    int NThreads = 7;
    CountDownLatch latch = new CountDownLatch(NThreads);

    on(LOOPBACK_ADDRESS, port,
        Pipeline
            .create((PipelineContext ctx) -> {
              ByteBuffer in  = ctx.inBuffer;
              ByteBuffer out = ctx.outBuffer;
              long length = in.readableBytes();
              in.readTo(out,length);
              out.write((byte)length);//append a byte for length
              tidSet.add(Unsafes.currentThreadId());
              return ctx;
            })
            .end());
    
    System.out.println("net server has started on "+ LOOPBACK_ADDRESS + " with port " + port);

    uncheck(() -> Thread.sleep(3_000L));

    for (int i = 0; i < NThreads; i++) {
      new Thread(()->{
        int fd = socketTcp(); System.out.println("current client socket: "+fd);
        connectTo(fd, LOOPBACK_ADDRESS, port);
        String msg;
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        long count = RUNS;
        while (count!=0) {
          if ( (count%(RUNS/2))==0 ) {
            System.gc();
            uncheck(() -> Thread.sleep(1000L));
            System.gc();
            printStats("count="+count);
          }
          int length = rnd.nextInt(1,Byte.MAX_VALUE);
          msg = randomString(length);
          sendmsg(fd, msg);
          recvmsg(fd, msg);
          count--;
        }
        shutdown(fd);

        latch.countDown();
      }).start();
    }

    long s = System.nanoTime();
    uncheck(() -> latch.await());
    System.out.printf("wait-to-be-done time(ns):%,d",(System.nanoTime()-s));

    System.gc();
    uncheck(() -> Thread.sleep(1000L));
    System.gc();
    printStats("end");
//    System.out.println(tidSet);
    assertThat(tidSet.size(),  is(NThreads));
  }

  private volatile long lastReportedMem = 0;
  private void printStats(String label) {
    System.out.println("********************"+label+"********************");
    System.out.println("+++++ heap usage stats @"+System.currentTimeMillis());
    long usedMemory = Runtime.getRuntime().totalMemory()
        - Runtime.getRuntime().freeMemory();
    System.out.println("current used memory(in bytes):" + usedMemory);
    System.out.println("delta to last reported(in bytes):" + (usedMemory-lastReportedMem));
    lastReportedMem = usedMemory;
    System.out.println("----- off-heap usage stats @" + System.currentTimeMillis()) ;
    dumpGlobalStats(label, Unsafes.currentThreadId());
    System.out.println("********************"+label+"********************");
  }

  public static final void on(
      String ipAddress, int port,
      Pipeline<PipelineContext, PipelineContext> pipeline) {
    AsyncIOThreadPool asyncIOPool =
        new AsyncIOThreadPool(7, pipeline,AsyncIOThreadPool.NULL_CONSUMER);
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
//    System.out.println("--->start to send msg["+msg+"]");
    int msgLength = msg.length();

    long cBuffer = allocate(msgLength);

    for (int i = 0; i < msgLength; i++) {
      Buffers.put(cBuffer + i, (byte) msg.charAt(i));
    }

//    System.out.println("--->current thread will block on writing...");
    long csize = write(fd, cBuffer, msgLength);
    assertThat((int)csize,  is(msgLength));
    free(cBuffer);
//    System.out.println("--->msg has been sent.");
  }

  public void recvmsg(int fd, String msg) {
//    System.out.println("<---start to receive msg:");
    int msgLength = 1024;
    try (ByteBuffer rb = Buffer.create(msgLength)) {
//      System.out.println("<---current thread will block on writing...");
      long n = read(fd, rb.address(), msgLength);
      ((Buffer) rb).skipWriteTo(n);

      StringBuilder sb = new StringBuilder();
      while (rb.readableBytes() != 1) {
        sb.append((char) rb.read());
      }
      int a = rb.read();
      assertThat(a, is(msg.length()));
      assertThat((int) n, is(a+1));
      assertThat(sb.toString(), is(msg));
//      System.out.println("<---msg[" + sb + "] has been received.");
    }
  }

  public void shutdown(int fd) {
    System.out.println("===current thread will wait 3s to shutdown/close...");
//    uncheck(() -> Thread.sleep(3_000L));
    int suc = shutdownWrite(fd);
    assertThat(suc, is(0));
    suc = close(fd);
    assertThat(suc, is(0));
    //
    System.out.println("===thread for fd="+fd+" done.");
  }

  private static String randomString(int length) {
    ThreadLocalRandom rnd = ThreadLocalRandom.current();
    StringBuilder sb = new StringBuilder(32);
    return rnd.ints(length)
        .mapToObj(i -> Character.valueOf((char) (i & 0x7F)).toString())
        .collect(Collectors.joining());
  }


  private static void dumpTLPStats(String label, long tid) {
    System.out.println("\n==================="+label+"===================");
    IntStream.range(0,34)
        .forEach(sizeClass->
            System.out.printf(
                "current Number Of TLP(%d) AvaiablePages for sizeClass(=%d): %d",
                tid,
                sizeClass,
                Allocator.ManagedPoolStats.currentNumOfTLPAvaiablePages(tid, sizeClass))
        );
  }

  private static void dumpGlobalStats(String label, long tid) {
    System.out.println("\n====================="+label+"=====================");
    System.out.println("current Number Of GP AvaiablePages:"+
        Allocator.ManagedPoolStats.currentNumOfGPAvaiablePages());
    System.out.println("current Number Of TLPs:"+
        Allocator.ManagedPoolStats.currentNumOfTLPs());
    System.out.printf("current Number Of TLP(=%d) FreePages:%d",
        tid,
        Allocator.ManagedPoolStats.currentNumOfTLPFreePages(tid));
  }


}
