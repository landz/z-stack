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

package z.znr.event;

import z.offheap.buffer.Buffer;

import static z.offheap.buffer.Buffers.getInt;
import static z.offheap.buffer.Buffers.putInt;
/**
 * EPollEvent
 */
public abstract class EPollEvent extends Buffer {

  public static final int SIZE = 12;

  public static final int OFFSET_EVENT_DATA = 4;

  protected EPollEvent() {
    super(SIZE);
  }

  protected EPollEvent(long address) {
    super(address, SIZE);
  }

  public int getEventMask() {
    return getInt(address);
  }
  public void setEventMask(int eventMask) {
    putInt(address,eventMask);
  }
}
