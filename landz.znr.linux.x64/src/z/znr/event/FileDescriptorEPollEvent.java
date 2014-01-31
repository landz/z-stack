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

import static z.offheap.buffer.Buffers.getInt;
import static z.offheap.buffer.Buffers.putInt;

/**
 * EPollEvent
 */
public final class FileDescriptorEPollEvent extends EPollEvent {

  public FileDescriptorEPollEvent() {}

  public FileDescriptorEPollEvent(long address) {
    super(address);
  }

  public int getFileDescriptor() {
    return getInt(address+OFFSET_EVENT_DATA);
  }
  public void setFileDescriptor(int fd) {
    putInt(address+OFFSET_EVENT_DATA,fd);
  }
}
