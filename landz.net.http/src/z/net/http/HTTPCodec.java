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

import z.net.NetModule;
import z.net.PipelineContext;
import z.offheap.buffer.Buffer;
import z.offheap.buffer.ByteBuffer;
import z.util.concurrent.ThreadLocalPool;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Created by jin on 1/24/14.
 */
public class HTTPCodec {

  private final HTTPConnectionManager connectManager;
  private final ThreadLocalPool<HTTPContext> contextPool;
  final HashMap<String, Consumer<HTTPContext>> router = new HashMap();

  public HTTPCodec(HTTPConnectionManager connectManager) {
    this.connectManager = connectManager;
    this.contextPool    =
        new ThreadLocalPool(NetModule.MAX_SUPPORTED_SOCKS, HTTPContext::new);
  }

  public PipelineContext process(PipelineContext pContext) {
    try(ThreadLocalPool.Item<HTTPContext> contextItem = contextPool.item();) {
      HTTPContext httpContext = contextItem.get();
      Request req = httpContext.requst;
      Response resp = httpContext.response;

      decode(req, pContext.inBuffer);

      switch (req.method) {
        case RequestParser.HTTPMethod.GET:
          router.getOrDefault(req.path,ctx->{
            ctx.response.buffer = ResponseBuilder.RESP_404;
          }).accept(httpContext);
          break;
        default:
          resp.buffer = ResponseBuilder.RESP_404;
          break;
      }

      ByteBuffer b = resp.buffer;
      b.readTo(pContext.outBuffer,b.readableBytes());

      return pContext;
    }
  }

//  public static boolean splitInBuffer() {
//  }

  /**
   * NOTE: this method is not thread-safe
   */
  public void decode(Request req, ByteBuffer in) {
  }

//  /**
//   * NOTE: this method is not thread-safe
//   */
//  public void encode(Response resp, ByteBuffer out) {
//  }


}
