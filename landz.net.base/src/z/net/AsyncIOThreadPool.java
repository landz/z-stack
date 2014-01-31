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

import z.channel.*;
import z.function.Pipeline;
import z.offheap.buffer.Buffer;
import z.util.concurrent.ThreadLocalPool;

import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import static z.offheap.zmalloc.Allocator.*;
import static z.util.Contracts.contract;
import static z.util.Unsafes.*;
import static z.znr.Errno.*;
import static z.znr.socket.Sockets.*;
import static z.znr.event.EPollEvents.*;

/**
 * AsyncIOThreadPool, which holds a pool of {@link Thread}s, is used to schedule
 * socket IO's execution.
 *
 * design contract:
 * <p> 1. only concern related events when ready to concern those
 *
 * TODO: MEMCHK in comment may be replaced with annotations with check toolings
 *
 */
public class AsyncIOThreadPool {

  public static final int SIZE_NET_READ_BUFFER  = 4*1024;
  public static final int SIZE_NET_WRITE_BUFFER = 4*1024;

  public static final IntConsumer NULL_CONSUMER = (fd)->{};

  public final int poolSize;

  private final AsyncIOWorkerThread[] workers;

  private final EventPoll eventPoll;

  /** NOTE: pipeline is not thread-safe. */
  private final Pipeline<PipelineContext, PipelineContext> pipeline;

  private final ThreadLocalPool<PipelineContext> pipelineContextPool;

  private final IntConsumer cleaner;

  public AsyncIOThreadPool(
      int poolSize,
      Pipeline<PipelineContext, PipelineContext> pipeline,
      IntConsumer cleaner) {

    contract(()->poolSize>0);//TODO

    this.poolSize  = poolSize;
    this.pipeline  = pipeline;
    this.workers   = new AsyncIOWorkerThread[poolSize];
    this.eventPoll = new EventPoll();

    //TODO: MAX_SUPPORTED_SOCKS is OK?
    this.pipelineContextPool =
        new ThreadLocalPool(NetModule.MAX_SUPPORTED_SOCKS,PipelineContext::new);

    IntStream.range(0,poolSize)
        .forEach(i -> workers[i] = new AsyncIOWorkerThread(i));

    this.cleaner = cleaner;
  }


  /**
   * TODO
   * note: the back queue is bounded, so this async may be blocked
   * when the work queue can not accept the asyncors
   *
   */
  void accept(int sockfd, int workerIndex) {
    workers[workerIndex].clientSocks.send(sockfd);
  }

  public final void start() {
    for (Thread t : workers) {
      t.start();
    }
  }

  public IntConsumer cleaner() {
    return cleaner;
  }

  //======================================================================
  //contracts trick
  protected static final boolean ENABLE_CONTRACTS = true;

  /**
   * AsyncIOWorkerThread, provide the worker thread implementation for AsyncPool.
   *
   */
  class AsyncIOWorkerThread extends Thread {
    private final int id;

    final IntHyperLoop clientSocks;
    final IntHyperLoop.OutPort clientSocksPort;

    final MPMCQueue activeSocks;

    AsyncIOWorkerThread(int id) {
      super("AsyncIOWorkerThread#"+id);
      this.id = id;
      this.clientSocks = new IntHyperLoop(1024*16);//TODO
      this.clientSocksPort = clientSocks.createOutPort();
      this.activeSocks = new MPMCQueue(1024*16);
    }

    /**
     * main run loop
     */
    public void run() {
      int suc = 0;//FIXME debug

      try {
        for(;;) {
          //accept
          while (clientSocksPort.isReceivable()) {
            int sockfd = clientSocksPort.receive();

            //FIXME
            if (ENABLE_CONTRACTS) {
              if (sockfd>=NetModule.MAX_SUPPORTED_SOCKS) {
                throw new IllegalArgumentException(String.format(
                    "the socket file descriptor(=%,d) is larger than " +
                        "MAX_SUPPORTED_SOCKS %,d",
                    sockfd, NetModule.MAX_SUPPORTED_SOCKS));
              }
            }

            //FIXME: investigate performance for non-removal
            //MEMCHK#1: release after fd closed
            long pointer = allocate(POINTERED_AREA_SIZE);
            setFileDescriptorForPointer(pointer, sockfd);
            setBufferAddressForPointer(pointer, 0L);
            setBufferSizeForPointer(pointer, 0);
            setWriteOffsetForPointer(pointer, 0L);
            //XXX: ignore the failing when non-removal
            suc = eventPoll.addForRead(sockfd, pointer);//FIXME: log
            //FIXME: so, suc = -EEXIST is allowed
          }
          //poll
          EventArray events = eventPoll.pollNonBlock();
          if (events!=null) {
            int numEvents = events.availableNumEvents;
            for (int i = 0; i < numEvents; i++) {
              //note: here just reuse the  partial of events
              long event = events.getEventAddress(i);
              activeSocks.offer(event);//FIXME log
            }

            long event;
            //event queue has provided mem barrier
            while ( MPMCQueue.NULL!=(event=activeSocks.poll()) ) {
              int mask = getEventMask(event);

              if (mask == EPOLLIN) {
                doRead(event);
              } else if (mask == EPOLLOUT) {
                doWrite(event);
              } else {//FIXME
                long pointer = getPointer(event);
                int fd       = getFileDescriptorForPointer(pointer);
                long buffer  = getBufferAddressForPointer(pointer);
                suc = close(fd);//FIXME for debug
                free(pointer);//match MEMCHK#1
              }
            }

            events.close();
          } else {
            //FIXME
            UNSAFE.park(false, 100L);
          }

        }
      } catch (Throwable ex) {
        ex.printStackTrace();//FIXME
      }
    }

    private void doRead(long event) {
      try (ThreadLocalPool.Item<PipelineContext> contextItem
               = pipelineContextPool.item()) {
        PipelineContext pContext = contextItem.get();

        long pointer = getPointer(event);
        int fd = getFileDescriptorForPointer(pointer);

        Buffer inBuffer = (Buffer) pContext.inBuffer;
        if (inBuffer.address() == 0L) {
          //MEMCHK#2: free after business logic done, but this may be changed
          //          after the read side design changed
          inBuffer.address(allocate(SIZE_NET_READ_BUFFER));
          inBuffer.capacity(SIZE_NET_READ_BUFFER);
        }

        long n = read(fd, inBuffer.address(), SIZE_NET_READ_BUFFER);

        if (n == SIZE_NET_READ_BUFFER) {
          //read again
          //FIXME: doesn't work now
          activeSocks.offer(event);//FIXME log - should not be false
        } else if (n > 0) {
          //FIXME
          inBuffer.skipWriteTo(n);
        } else if (n == -EAGAIN || n == 0) {
          //FIXME do noting? n==0?
          eventPoll.concernRead(fd, pointer);
          return;
        } else if (n < 0) {
          //FIXME: more treatment for different errnos
          //FIXME: do clean, outBuffer aslo need be freed
          //       after the read side design changed
          int suc = close(fd);//FIXME for debug
          free(pointer);//MEMCHK#1
          free(inBuffer.address());//MEMCHK#2
          inBuffer.address(0L);
          //FIXME log
          return;
        }

        //
        Buffer outBuffer = (Buffer) pContext.outBuffer;
        if (outBuffer.address() == 0L) {
          //MEMCHK#3: free after write
          outBuffer.address(allocate(SIZE_NET_WRITE_BUFFER));
          outBuffer.capacity(SIZE_NET_WRITE_BUFFER);
        }

        //FIXME business logic
        try {
         pipeline.apply(pContext);
        }catch (Throwable t) {
          //FIXME log
          //lastly do clean
          free(inBuffer.address());//MEMCHK#2
          inBuffer.address(0L);
          inBuffer.reset();
          free(outBuffer.address());//MEMCHK#3
          outBuffer.address(0L);
          outBuffer.reset();
        }

        setBufferAddressForPointer(pointer, pContext.outBuffer.address());
        setBufferSizeForPointer(pointer, (int) pContext.outBuffer.writeCursor());
        setWriteOffsetForPointer(pointer, 0L);

        doWrite(event);

        //lastly do clean
        //FIXME  should be protected against potential exceptions
        free(inBuffer.address());//MEMCHK#2
        inBuffer.address(0L);
        inBuffer.reset();
        //NOTE: outBuffer is not freed here
        outBuffer.address(0L);
        outBuffer.reset();
      }
    }


    private void doWrite(long event) {
      long pointer = getPointer(event);
      int fd       = getFileDescriptorForPointer(pointer);
      long buffer  = getBufferAddressForPointer(pointer);
      int size     = getBufferSizeForPointer(pointer);
      long offset  = getWriteOffsetForPointer(pointer);

      //FIXME: this is an unusual case. how about a special dealing?
      if (size==0) {
        eventPoll.concernRead(fd, pointer);
        return;
      }

      long count   = size-offset;
      long n = write(fd, buffer+offset, count);//FIXME: suc for debug, log

      if (n==count) {
        free(buffer);//MEMCHK#3
        eventPoll.concernRead(fd, pointer);
      } else if (n>0) {
        setWriteOffsetForPointer(pointer, n);
        eventPoll.concernWrite(fd, pointer);
      } else if (n==-EAGAIN || n==0) {
        eventPoll.concernWrite(fd, pointer);
      } else {
        //do clean
        int suc = close(fd);
        free(pointer);//MEMCHK#1
        free(buffer);//MEMCHK#3
      }
    }


  }


  //=====================================================
  //helpers for EventPoll
  private static final boolean isErrorEvent(int mask) {
    return (mask & ( EPOLLERR | EPOLLHUP ) )!=0;
  }

  private static final boolean isCloseEvent(int mask) {
    return (mask & EPOLLRDHUP )!=0;
  }

  //=====================================================
  //helper for the pointer (to event data "struct")
  private static final int POINTERED_AREA_SIZE = 24;

  private static final int getFileDescriptorForPointer(long pointer) {
    return UNSAFE.getInt(pointer);
  }

  private static final void setFileDescriptorForPointer(long pointer, int fd) {
    UNSAFE.putInt(pointer, fd);
  }

  private static final long getBufferAddressForPointer(long pointer) {
    return UNSAFE.getLong(pointer+4);
  }

  private static final void setBufferAddressForPointer(long pointer, long bufferAddress) {
    UNSAFE.putLong(pointer+4, bufferAddress);
  }

  private static final int getBufferSizeForPointer(long pointer) {
    return UNSAFE.getInt(pointer+12);
  }

  private static final void setBufferSizeForPointer(long pointer, int size) {
    UNSAFE.putInt(pointer+12, size);
  }

  private static final long getWriteOffsetForPointer(long pointer) {
    return UNSAFE.getLong(pointer + 16);
  }

  private static final void setWriteOffsetForPointer(long pointer, long offset) {
    UNSAFE.putLong(pointer + 16, offset);
  }

}
