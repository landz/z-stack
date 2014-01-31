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

package z.net;

import z.util.Unsafes;

import static z.znr.socket.SocketOptions.setOptReuseAddr;
import static z.znr.socket.SocketOptions.setOptTcpNoDelay;
import static z.znr.socket.Sockets.*;
import static z.znr.Errno.*;

/**
 */
public class NetModule {

  public static final int MAX_SUPPORTED_SOCKS =
      Integer.getInteger("z.net.maxSupportedSocks",128*1024);

//  private static final int INITIAL_SUPPORTED_SOCKS =
//      Integer.getInteger("z.net.initialSupportedSocks", 64*1024);

  private final AsyncIOThreadPool asyncIOPool;

  public NetModule(AsyncIOThreadPool asyncIOPool) {
    this.asyncIOPool = asyncIOPool;
  }

  public final void startServer(String ipAddress, int port) {
    asyncIOPool.start();

    int serverfd = socketTcp();
    contract(serverfd);

    //FIXME: works for inheritable options
    setOptReuseAddr(serverfd, true);
    setOptTcpNoDelay(serverfd, true);
    //TODO: TCP_KEEPALIVE
    //TODO: TCP_FASTOPEN and others...

    int suc = bind(serverfd, ipAddress, port);
    contract(suc);

    suc = listen(serverfd);
    contract(suc);

    Thread acceptor = new Acceptor(serverfd);
    acceptor.start();
  }

  private final class Acceptor extends Thread {
    private final int serverfd;

    private int robinIndex;

    public Acceptor(int serverfd) {
      setName("z.net Acceptor");
      setDaemon(true);
      this.serverfd = serverfd;
    }

    @Override
    public void run() {
      int cfd;
      for (;;) {
        cfd=acceptNonBlock(serverfd);
        if (cfd>0) {
          if (robinIndex == asyncIOPool.poolSize)
            robinIndex = 0;
          asyncIOPool.accept(cfd, robinIndex++);
        } else {
          if (cfd==(-EAGAIN)) {
            Unsafes.UNSAFE.park(false,500L);//TODO: accepting rate control
          }else {
            return;
          }
        }

      }
    }

  }


  //======================================================================
  //common contracts
  protected static boolean enable_contracts = true;
  private static final void contract(int num) {
    if (enable_contracts) {
      if (num >= 0) {
        return;
      } else {
        new RuntimeException("Meeting error:"+ getErrnoString(-num));
      }
    }
  }


}
