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

import java.util.concurrent.atomic.AtomicLong;

public class GenericMPMCQueueStressTest4 {

  private static final int RUNS = 10_000_000;

//  private MPMCQueue loop = new MPMCQueue(8);
//  GenericMPMCQueue<String> loop = new GenericMPMCQueue(1024*128);
public static final AtomicLong ct = new AtomicLong();

  @Test
  public void testMPMCHyperLoop() {
    for (int i = 0; i < 5; i++) {
      ct.set(0);
      System.gc();
      runStress("#" + i);
    }
  }

  private void runStress(String label) {
    System.out.println("=====in Round "+label);
//    CountDownLatch endLatch = new CountDownLatch(1);
//    String[] values = new String[RUNS];
//    ThreadLocalRandom rnd = ThreadLocalRandom.current();
//    IntStream.range(0, RUNS).forEach( i->
//        values[i]= Long.toBinaryString(rnd.nextLong(0,Long.MAX_VALUE))
//    );

    System.out.println("start GenericMPMCQueueStressTest...");
    TestThread tt = new TestThread();//values,endLatch);
    tt.start();

    try {
    long s = System.nanoTime();
    //XXX: producer is in main thread
    for (int i=0;i<RUNS;i++) {
//      System.out.println("to send values["+i+"]:"+values[i]);
      while (!tt.queue.offer(new A(() -> ct.incrementAndGet()))){}
    }

      while (ct.get()!=RUNS) {
//      System.out.println(ct.get());
      }

//    endLatch.await();
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

  class TestThread extends Thread {
    final GenericMPMCQueue<A> queue = new GenericMPMCQueue(1024*128);
//    final String[] values;
//    final  CountDownLatch endLatch;

    public TestThread(){//String[] values, CountDownLatch endLatch) {
//      this.values = values;
//      this.endLatch = endLatch;
    }

    public void run() {
      for (;;) {
          A v = queue.poll();
        if (v!=null) {
          v.body.run();
        }
//          for (int i=0;i<RUNS;i++) {
//        System.out.println("received values["+i+"]:"+values[i]);
//            assertThat(v,is(values[i]));
//          }
//      endLatch.countDown();
      }
    }
  }

    public class A {
      final Runnable body;

      public A(Runnable body){
        this.body = body;
      }
    }

}
