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

package z.net.http;

import org.junit.Test;
import z.offheap.buffer.ByteBuffer;

/**
 * Created by jin on 1/25/14.
 */
public class ResponseBuilderTest {
  @Test
  public void basicTest() {
    try (ByteBuffer buffer = ResponseBuilder
        .status(200)
        .head("x","1")
        .head("y","2")
        .head("a-b","c=d")
        .body(null)
        .build()) {
      byte[] bs= new byte[(int)buffer.readableBytes()];
      for (int i =0 ; buffer.readableBytes()!=0;i++) {
        bs[i]=buffer.read();
      }
      System.out.println(new String(bs));
    }

    try (ByteBuffer buffer = ResponseBuilder
        .status(200)
        .head("x","1")
        .head("y","2")
        .head("a-b","c=d")
        .body("abcdefg")
        .build()) {
      byte[] bs= new byte[(int)buffer.readableBytes()];
      for (int i =0 ; buffer.readableBytes()!=0;i++) {
        bs[i]=buffer.read();
      }
      System.out.println(new String(bs));
    }

    try (ByteBuffer buffer = ResponseBuilder
        .status(404)
        .head("x","1")
        .head("y","2")
        .head("a-b","c=d")
        .body("abcdefg")
        .build()) {
      byte[] bs= new byte[(int)buffer.readableBytes()];
      for (int i =0 ; buffer.readableBytes()!=0;i++) {
        bs[i]=buffer.read();
      }
      System.out.println(new String(bs));
    }

  }

}
