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

package z.channel.contrast;

import static z.util.Contracts.contract;
import static z.util.Unsafes.*;


/**
 *  {@link RawThroughputConstrastHyperLoop} is an Inter-Thread-Communication(ITC) mechanism.
 *  <P>
 *  Note:<p>
 *    For performance, the current implementations of LongHyperLoop use
 *    off-heap memory via s.m.Unsafe, so it is a little "heavy", take care
 *    when you rush with "new/discard" to LongHyperLoop.
 *    <p> see more
 *
 */
public class RawThroughputConstrastHyperLoop {
  //TODO: add a config option for SIZE_HYPERLOOP_BASE?
  private static final int SIZE_HYPERLOOP_BASE = 8*1024;//?
  private static final int SIZE_SHIFT_BUFFERSLOT = 3;

  private final long addressRaw;
  private final long addressHyperLoop;

  private final int nBufferSlots;
  private final int bufferSlotMask;

  private final long addrWriteCursor;
  private final long addrMinReadCursor;
  private final long addrBuffer;//buffer area grows up
  private final long addrReadCursorCount;//int type
  private long addrReadCursors;//readCursor area grows down


  /**
   * create a LongHyperLoop.
   * <p>
   * contract: <p>
   *  1. nBufferSlots > 0  <p>
   *  2. nBufferSlots is a power of 2   <p>
   *
   * @param nBufferSlots the size of internal buffer in slot unit
   */
  public RawThroughputConstrastHyperLoop(int nBufferSlots) {
    contract(()->nBufferSlots>0);
    contract(()->Integer.bitCount(nBufferSlots)==1);
    this.nBufferSlots = nBufferSlots;
    this.bufferSlotMask = nBufferSlots - 1;
    //========================================================
    int requestedSize = SIZE_HYPERLOOP_BASE
        + (nBufferSlots<< SIZE_SHIFT_BUFFERSLOT);
    addressRaw = systemAllocateMemory(requestedSize + SIZE_CACHE_LINE);
    addressHyperLoop = nextCacheLineAlignedAddress(addressRaw);
    contract(()-> isCacheLineAligned(addressHyperLoop));

    this.addrWriteCursor   = addressHyperLoop  + SIZE_CACHE_LINE_PADDING;
    this.addrMinReadCursor = addrWriteCursor   + SIZE_CACHE_LINE_PADDING;
    this.addrBuffer        = addrMinReadCursor + SIZE_CACHE_LINE_PADDING;

    this.addrReadCursorCount =
        addressHyperLoop + requestedSize - SIZE_CACHE_LINE_PADDING;

    this.addrReadCursors = addrReadCursorCount;// - SIZE_CACHE_LINE_PADDING;

    UNSAFE.putLong(addrWriteCursor, 0L);
    UNSAFE.putLong(addrMinReadCursor, 0L);
    UNSAFE.putInt(addrReadCursorCount,0);
  }


  public void sendTo(long value) {
    long minReadCursor = UNSAFE.getLongVolatile(null,addrMinReadCursor);
    long writeCursor = UNSAFE.getLong(addrWriteCursor);

    while (writeCursor == (minReadCursor+nBufferSlots)) {
      // assume readCursorCount>0
      minReadCursor = UNSAFE.getLongVolatile(null, addrReadCursors);
      for (int i = 1; i < UNSAFE.getInt(addrReadCursorCount); i++) {
        long readCursor =
            UNSAFE.getLongVolatile(null, addrReadCursors + i * SIZE_CACHE_LINE_PADDING);
        minReadCursor = minReadCursor > readCursor ? readCursor : minReadCursor;
      }
      UNSAFE.putLong(addrMinReadCursor,minReadCursor);
      Thread.yield();//harm latency but welcome to throughput
    }

//    UNSAFE.putLong(
//        addrBuffer + ((writeCursor & bufferSlotMask) << SIZE_SHIFT_BUFFERSLOT),
//        value);
    UNSAFE.putLong(addrWriteCursor, writeCursor + 1);
    UNSAFE.storeFence();
  }


  @Override
  public void finalize() {
    systemFreeMemory(addressRaw);
  }

  /**
   * Note:
   * it is always hoped the values sent into LongHyperLoop could be consumed ASAP.
   *
   * TODO: need to handle the removal of OutPort dynamically
   */
  public final class Out {
    private final long addrReadCursor;

    public Out() {
      synchronized(Out.class) {
        addrReadCursors -= SIZE_CACHE_LINE_PADDING;
        this.addrReadCursor = addrReadCursors;
        UNSAFE.putInt(addrReadCursorCount, UNSAFE.getInt(addrReadCursorCount) + 1);
      }
    }

    public void receiveAll() {
      long readCursor = UNSAFE.getLong(addrReadCursor);
      while (readCursor==UNSAFE.getLongVolatile(null, addrWriteCursor)) {
        Thread.yield();
//        long tries = 600;
//        while (0!=tries) {
//          tries--;
//        }
      }
//      long value = UNSAFE.getLong(
//          addrBuffer + ((readCursor & bufferSlotMask)<<SIZE_SHIFT_BUFFERSLOT));
      UNSAFE.putLong(addrReadCursor, readCursor+1);
      UNSAFE.storeFence();
//      return value;
    }


  }

  @FunctionalInterface
  public static interface WaitStrategy {
    public void waitToReceive();
  }
  
}
