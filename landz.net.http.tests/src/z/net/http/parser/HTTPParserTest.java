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

package z.net.http.parser;

import org.junit.Test;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by jin on 1/24/14.
 */
public class HTTPParserTest {

  private boolean suc = false;

  @Test
  public void basicRequest() {
    String request = "GET /index.html HTTP/1.1\r\n" +
        "Host: www.example.com\r\n\nasdsadas";

    ByteBuffer buffer = ByteBuffer.wrap(request.getBytes());

    ParserSettings settings = new ParserSettings();
    settings.on_path = (parser, b, pos, len)-> {
      System.out.println(str(b,pos,len));
      return 0;
    };

    settings.on_message_complete = parser->{
      suc = true;
      return 0;
    };

    HTTPParser parser = new HTTPParser(ParserType.HTTP_REQUEST);
    parser.execute(settings, buffer);

    assertThat(suc, is(true));

    System.out.println(request.getBytes().length);
    System.out.println(buffer.position());
  }


  @Test
  public void basicRequest2() {
    String request1 = "GET /index.html HTTP/1.1\r\n" +
        "Host: www.example.com\r\n\n";

    String request2 = "GET /css/a.css HTTP/1.1\r\n" +
        "Host: www.example.com\r\n\n";

    String request = request1+request2;

    ByteBuffer buffer = ByteBuffer.wrap(request.getBytes());

    ParserSettings settings = new ParserSettings();
    settings.on_path = (parser, b, pos, len)-> {
      System.out.println(str(b,pos,len));
      return 0;
    };

    settings.on_message_complete = parser->{
      suc = true;
      return 0;
    };

    HTTPParser parser = new HTTPParser(ParserType.HTTP_REQUEST);
    parser.execute(settings, buffer);

    System.out.println(suc);
  }

  @Test
  public void basicResponse() {

//    ByteBuffer buffer = ByteBuffer.wrap(request.getBytes());
//
//    ParserSettings settings = new ParserSettings();
//    settings.on_path = (parser, b, pos, len)-> {
//      System.out.println(str(b,pos,len));
//      return 0;
//    };
//
//    settings.on_message_complete = parser->{
//      suc = true;
//      return 0;
//    };
//
//    HTTPParser parser = new HTTPParser(ParserType.HTTP_REQUEST);
//    parser.execute(settings, buffer);
//
//    System.out.println(suc);
  }


  static String str (ByteBuffer b, int pos, int len) {
    byte [] by = new byte[len];
    int saved = b.position();
    b.position(pos);
    b.get(by);
    b.position(saved);
    return new String(by);
  }

  static String str (ByteBuffer b) {
    int len = b.limit() - b.position();
    byte [] by = new byte[len];
    int saved = b.position();
    b.get(by);
    b.position(saved);
    return new String(by);
  }

}
