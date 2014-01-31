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

import z.function.*;
import z.module.License;
import z.module.Module;
import z.net.AsyncIOThreadPool;
import z.net.NetModule;
import z.net.PipelineContext;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import static z.znr.socket.SocketAddressInet.LOOPBACK_ADDRESS;

/**
 */
@Module(
    name        = "z.net.http",
    version     = "1.0",
    vendor      = "Landz",
    description = "Landz HTTP Module. Copyright 2014, " +
        "Landz and its contributors. All rights reserved. http://landz.org",
    license     = License.APLv2
)
public class HTTPServer implements Routable, Runnable {

  private static final int DEFAULT_ASYNC_IO_POOL_SIZE =
      Runtime.getRuntime().availableProcessors()-1;//FIXME

  private final String ipAddress;
  private final int port;

  private final HTTPConnectionManager connectionManager =
      new HTTPConnectionManager();
  private final HTTPCodec codec = new HTTPCodec(connectionManager);

  private final Pipeline<PipelineContext, PipelineContext> HTTP_PIPELINE =
      Pipeline
          .create(codec::process)
          .end();//FIXME

  private HTTPServer(String ipAddress, int port) {
    this.ipAddress = ipAddress;
    this.port      = port;
  }

  public static final Routable on(String ipAddress, int port) {
    return new HTTPServer(ipAddress, port);
  }

  public static final Routable on(String ipAddress) {
    return new HTTPServer(ipAddress, 80);
  }

  public static final Routable on(int port) {
    return new HTTPServer(LOOPBACK_ADDRESS, port);
  }


  @Override
  public Runnable GET(String s, Consumer<HTTPContext> func) {
    codec.router.put(s,func);
    return this;
  }

  @Override
  public void run() {
    AsyncIOThreadPool asyncIOPool =
        new AsyncIOThreadPool(DEFAULT_ASYNC_IO_POOL_SIZE,
            HTTP_PIPELINE,
            connectionManager::clean);
    NetModule net = new NetModule(asyncIOPool);
    net.startServer(ipAddress,port);
  }
}
