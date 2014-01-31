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

import z.offheap.buffer.Buffer;
import z.offheap.buffer.ByteBuffer;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

interface ResponseBuilderHeadBody {
  ByteBuffer build();
}

interface ResponseBuilderHead {
  public ResponseBuilderHead head(String fieldname, String value);
  public ResponseBuilderHeadBody body(String body);
}

/**
 * Created by jin on 1/24/14.
 */
public class ResponseBuilder implements
    ResponseBuilderHead,
    ResponseBuilderHeadBody {

  private static final Charset ASCII = Charset.forName("US-ASCII");
  private static final Charset UTF_8 = Charset.forName("utf8");

  final int status;
  final Map<String, String> headers = new HashMap();
  String body;

  private ResponseBuilder(int status) {
    this.status = status;
  }

  public static ResponseBuilderHead status(int code) {
    return new ResponseBuilder(code);
  }

  public ResponseBuilder head(String fieldName, String fieldValue) {
    headers.put(fieldName,fieldValue);
    return this;
  }

  public ResponseBuilderHeadBody body(String body) {
    this.body = body;
    return this;
  }

  public ByteBuffer build() {
    //FIXME:
    ByteBuffer buffer = Buffer.create(128 * 1024);
    byte[] requestLine =
        ("HTTP/1.1 " + status + " " + getReason(status) + "\r\n").getBytes(ASCII);
    for (int i = 0; i < requestLine.length; i++) {
      buffer.write(requestLine[i]);
    }
    headers.forEach((k,v)->{
      byte[] bs = (k+": "+v+"\r\n").getBytes(ASCII);
      for (int i = 0; i < bs.length; i++) {
        buffer.write(bs[i]);
      }
    });

    if (body!=null) {
      byte[] b = body.getBytes(UTF_8);
      byte[] a = ("content-length: "+b.length+"\r\n\r\n").getBytes(ASCII);
      for (int i = 0; i < a.length; i++) {
        buffer.write(a[i]);
      }
      for (int i = 0; i < b.length; i++) {
        buffer.write(b[i]);
      }
    }

    buffer.write((byte)'\r');
    buffer.write((byte)'\n');
    return buffer;
  }

  String getReason(int status) {
    switch (status) {
      case 200:
        return "OK";
      default:
        return "Not Found";
    }
  }

  public static final String DEFAULT_FILED_NAME_CONTENT_TYPE   = "content-type";
  public static final String DEFAULT_FILED_NAME_SERVER         = "server";

  public static final String DEFAULT_FILED_VALUE_CONTENT_TYPE   = "text/html; charset=UTF-8";
  public static final String DEFAULT_FILED_VALUE_SERVER         = "Landz stack - HTTP";

  //not data...
  public static final ByteBuffer RESP_404 =
      ResponseBuilder
          .status(404)
          .head(DEFAULT_FILED_NAME_CONTENT_TYPE,
              DEFAULT_FILED_VALUE_CONTENT_TYPE)
          .head(DEFAULT_FILED_NAME_SERVER,
              DEFAULT_FILED_VALUE_SERVER)
          .body(null)
          .build();
}




