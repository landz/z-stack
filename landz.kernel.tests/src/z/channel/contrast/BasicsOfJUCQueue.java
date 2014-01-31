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
import z.testware.common.Stopwatch;

import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static z.util.Throwables.uncheck;

/**
 *
 [Stopwatch(Stopped): offer 50,000,000 times to an ArrayBlockingQueue
 ]{elapsed 1681 milliseconds}
 [Stopwatch(Stopped): offer 50,000,000 times to a LinkedBlockingQueue
 ]{elapsed 51266 milliseconds}
 [Stopwatch(Stopped): offer 50,000,000 times to a LinkedTransferQueue
 ]{elapsed 41966 milliseconds}
 [Stopwatch(Stopped): offer 50,000,000 times to a NavieBlockingQueue
 ]{elapsed 26193 milliseconds}
 [Stopwatch(Stopped): offer 50,000,000 times to a ConcurrentLinkedQueue
 ]{elapsed 13799 milliseconds}
 *
 * the main tested point here is the first step of offering objects.
 * It is so slow for queue. The most problem here is that the unbound queue
 * needs to new node object.
 *
 */
public class BasicsOfJUCQueue {
  private static final int RUNS = 50_000_000;
  public static final Object OBJECT = new Object();

  @Test
  public void testOfferOfCLQ() {
    ConcurrentLinkedQueue<Object> clq = new ConcurrentLinkedQueue();
    Stopwatch watch = Stopwatch.create(String.format("offer %,d times to a ConcurrentLinkedQueue\n", RUNS));
    watch.start();

    IntStream.range(0,RUNS).forEach((i)->
        clq.offer(OBJECT)
    );

    watch.stop();
    watch.printMillis();
    assertThat(clq.size(), is(RUNS));
  }

  @Test
  public void testPutOfABQ() {
    ArrayBlockingQueue<Object> abq = new ArrayBlockingQueue(RUNS);
    Stopwatch watch = Stopwatch.create(String.format("offer %,d times to an ArrayBlockingQueue\n", RUNS));
    watch.start();

    IntStream.range(0,RUNS).forEach((i)->
        uncheck(() -> abq.put(OBJECT))
    );

    watch.stop();
    watch.printMillis();
    assertThat(abq.size(), is(RUNS));
  }

  @Test
  public void testPutOfLTQ() {
    LinkedTransferQueue<Object> ltq = new LinkedTransferQueue();
    Stopwatch watch = Stopwatch.create(String.format("offer %,d times to a LinkedTransferQueue\n", RUNS));
    watch.start();

    IntStream.range(0,RUNS).forEach((i) ->
        ltq.put(OBJECT)
    );

    watch.stop();
    watch.printMillis();
    assertThat(ltq.size(), is(RUNS));
  }

  @Test
  public void testPutOfLBQ() {
    LinkedBlockingQueue<Object> lbq = new LinkedBlockingQueue();
    Stopwatch watch = Stopwatch.create(String.format("offer %,d times to a LinkedBlockingQueue\n", RUNS));
    watch.start();

    IntStream.range(0,RUNS).forEach((i) ->
        uncheck(() -> lbq.put(OBJECT))
    );

    watch.stop();
    watch.printMillis();
    assertThat(lbq.size(), is(RUNS));
  }


  @Test
  public void testPutOfNVQ() {
    NavieBlockingQueue<Object> nbq = new NavieBlockingQueue(RUNS);
    Stopwatch watch = Stopwatch.create(String.format("offer %,d times to a NavieBlockingQueue\n", RUNS));
    watch.start();

    IntStream.range(0,RUNS).forEach((i) ->
        uncheck(() -> nbq.put(OBJECT))
    );

    watch.stop();
    watch.printMillis();
    assertThat(nbq.size(), is(RUNS));
  }


  static class NavieBlockingQueue<T> {

    private LinkedList<T> queue = new LinkedList<>();
    private int limit = 16;

    public NavieBlockingQueue(int limit){
      this.limit = limit;
    }


    public synchronized void put(T item)
        throws InterruptedException  {
      while(this.queue.size() == this.limit) {
        wait();
      }
      if(this.queue.size() == 0) {
        notifyAll();
      }
      this.queue.add(item);
    }


    public synchronized T take()
        throws InterruptedException{
      while(this.queue.size() == 0){
        wait();
      }
      if(this.queue.size() == this.limit){
        notifyAll();
      }

      return this.queue.remove(0);
    }

    //XXX: NotThreadSafe
    public int size() {
      return queue.size();
    }

  }


}
