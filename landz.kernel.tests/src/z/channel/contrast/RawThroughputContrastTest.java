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

package z.channel.contrast;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RawThroughputContrastTest {

  public enum DaemonThreadFactory implements ThreadFactory
  {
    INSTANCE;

    @Override
    public Thread newThread(final Runnable r)
    {
      Thread t = new Thread(r);
      t.setDaemon(true);
      return t;
    }
  }

  private static class MyRunnable implements Runnable
  {
    private long count = 0;
    private CountDownLatch latch;
    private RawThroughputConstrastHyperLoop.Out out;

    public MyRunnable(RawThroughputConstrastHyperLoop.Out out)
    {
      this.out = out;
    }


    public void reset(CountDownLatch latch)
    {
      this.latch = latch;
    }

    @Override
    public void run()
    {
      for (long i=0;i<RUNS;i++) {
        out.receiveAll();
      }
      latch.countDown();
    }
  }

  private static final int RUNS = 200_000_000;

  private RawThroughputConstrastHyperLoop loop = new RawThroughputConstrastHyperLoop(1024*64);
  private RawThroughputConstrastHyperLoop.Out out = loop.new Out();

  private final ExecutorService executor = Executors.newSingleThreadExecutor(DaemonThreadFactory.INSTANCE);
  private final MyRunnable myRunnable = new MyRunnable(out);

  @Test
  public void testHyperLoop() throws Exception {
    System.out.println("RawThroughputContrastTest start...");
    for (int i = 0; i < 20; i++) {
      System.gc();
      runHyperLoop();
    }
  }

  private void runHyperLoop() throws Exception {
    CountDownLatch endLatch = new CountDownLatch(1);
    myRunnable.reset(endLatch);
    executor.submit(myRunnable);

    long s = System.nanoTime();
    for (long i=0;i<RUNS;i++) {
      loop.sendTo(i);
    }
    endLatch.await();

    long opsPerSecond = (RUNS * 1000_000_000L) / (System.nanoTime()-s);
    System.out.printf("%,d ops/sec\n", opsPerSecond);

  }

  public static void main(String[] args) throws Exception  {
    System.out.println("start...");
    new RawThroughputContrastTest().testHyperLoop();
  }


}
