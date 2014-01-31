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

package z.offheap.buffer;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import z.exception.ContractViolatedException;

/**
 * Created by jin on 12/26/13.
 */
public class BufferTest2 {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testContracts1() {
    try (ByteBuffer buffer = Buffer.create(8)) {
      exception.expect(ContractViolatedException.class);
      buffer.skipRead(100);
    }
  }

  @Test
  public void testContracts2() {
    try (ByteBuffer buffer = Buffer.create(8)) {
      exception.expect(ContractViolatedException.class);
      buffer.skipReadTo(100);
    }
  }


  @Test
  public void testContracts3() {
    try (ByteBuffer buffer = Buffer.create(8)) {
      buffer.nativeOrder().writeLong(1L);
      exception.expect(ContractViolatedException.class);
      buffer.write((byte)1);
    }
  }

  @Test
  public void testContracts4() {
    try (ByteBuffer buffer = Buffer.create(8)) {
      exception.expect(ContractViolatedException.class);
      byte r = buffer.read();
    }
  }


}
