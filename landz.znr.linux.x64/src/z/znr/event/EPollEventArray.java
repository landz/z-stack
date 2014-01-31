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

/**
 * EPollEventArray.
 */
public class EPollEventArray extends Buffer {
  public static final int MAX_EVENTS = 2048;

  public EPollEventArray() {
    super(EPollEvent.SIZE * MAX_EVENTS);
  }

  public EPollEventArray(long addressEvents, long maxevents) {
    super(addressEvents, EPollEvent.SIZE * maxevents);
  }

  public <T> T getEvent(int index, Class<T> klass) {
    if (klass==FileDescriptorEPollEvent.class) {
      return (T)new FileDescriptorEPollEvent(address+index*EPollEvent.SIZE);
    }else if (klass==PointerEPollEvent.class) {
      return (T)new PointerEPollEvent(address+index*EPollEvent.SIZE);
    }else {
      throw new IllegalArgumentException("do not recognize the class "+klass);
    }
  }

  public long getEventAddress(int index) {
    return address+index*EPollEvent.SIZE;
  }

}
