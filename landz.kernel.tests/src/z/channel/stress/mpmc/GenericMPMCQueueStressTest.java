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

package z.channel.stress.mpmc;

import org.junit.Test;
import z.channel.GenericMPMCQueue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class GenericMPMCQueueStressTest {

  private static final int RUNS = 10_000_000;

//  private MPMCQueue loop = new MPMCQueue(8);
  private GenericMPMCQueue<String> loop = new GenericMPMCQueue(1024);

  @Test
  public void testMPMCHyperLoop() {
    for (int i = 0; i < 5; i++) {
      System.gc();
      runStress("#" + i);
    }
  }

  private void runStress(String label) {
    System.out.println("=====in Round "+label);
    CountDownLatch endLatch = new CountDownLatch(1);
    String[] values = new String[RUNS];
    ThreadLocalRandom rnd = ThreadLocalRandom.current();
    IntStream.range(0, RUNS).forEach( i->
        values[i]= Long.toBinaryString(rnd.nextLong(0,Long.MAX_VALUE))
    );

    System.out.println("start GenericMPMCQueueStressTest...");
    new Thread(() -> {
      try{
      String v;
      for (int i=0;i<RUNS;i++) {
        while(null ==(v = loop.poll())){}
//        System.out.println("received values["+i+"]:"+values[i]);
        assertThat(v,is(values[i]));
      }
      }finally {
        endLatch.countDown();
      }
    }).start();

    try {
    long s = System.nanoTime();
    //XXX: producer is in main thread
    for (int i=0;i<RUNS;i++) {
//      System.out.println("to send values["+i+"]:"+values[i]);
      while (!loop.offer(values[i])){}
    }
    endLatch.await();
      long time = System.nanoTime()-s;
      System.out.printf("done with costed time: %,d\n",time);

//      System.out.println("current number of threads:"+numOfRunningThreads());
    }catch (Exception e){e.printStackTrace();}
  }

  private static final int numOfRunningThreads() {
    return (int)
        Thread.getAllStackTraces().keySet().stream()
            .filter(t -> t.getState() == Thread.State.RUNNABLE)
            .count();
  }

}
