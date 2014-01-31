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

package z.channel.perf;

import org.junit.Test;
import z.channel.GenericHyperLoop;
import z.channel.ReceivePort;

import java.util.concurrent.CountDownLatch;

public class EndToEndThroughputTestForGenericHyperLoop {

  private static class MyRunnable implements Runnable
  {
    private CountDownLatch latch;
    private ReceivePort<Object> out;

    public MyRunnable(ReceivePort<Object> out, CountDownLatch latch)
    {
      this.out = out;
      this.latch = latch;
    }

    @Override
    public void run()
    {
      Object v;
      for (int i=0;i<RUNS;i++) {
        v = out.receive();
      }
//      latch.countDown();
    }
  }

  private static final int RUNS = 500_000_000;

  private GenericHyperLoop<Object> loop = new GenericHyperLoop(1024);
  private ReceivePort<Object> out = loop.createReceivePort();
  public static final Object OBJECT = new Object();

  @Test
  public void testHyperLoop() throws Exception {
    runHyperLoop();
  }

  private void runHyperLoop() throws Exception {
//    int numRunnings0 = numberOfRunningThreads();
    CountDownLatch endLatch = new CountDownLatch(1);

    new Thread(new MyRunnable(out,endLatch)).start();

    System.out.println("start...");

    long s = System.nanoTime();
    //XXX: producer is in main thread
    for (int i=0;i<RUNS;i++) {
      while (!loop.trySend(OBJECT)){}
    }
//    endLatch.await();

    long opsPerSecond = RUNS * 1000_000_000L / (System.nanoTime()-s);
    System.out.printf("%,d ops/sec\n", opsPerSecond);

//    assertThat(numberOfRunningThreads(), is(numRunnings0));

  }

  private static final int numberOfRunningThreads() {
    return (int)
        Thread.getAllStackTraces().keySet().stream()
            .filter(t -> t.getState() == Thread.State.RUNNABLE)
            .count();
  }

  public static void main(String[] args) throws Exception {
//    Affinity.bindTo(Topology.socket(0).physicalCore(2).virtualCore(0));
//    System.out.println("start...");
    new EndToEndThroughputTestForGenericHyperLoop().testHyperLoop();
  }


}
