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

package z.channel.unit.mpmc;

import org.junit.Test;
import z.channel.MPMCQueue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MPMCQueueTest {

  @Test
  public void testSendReceive1() {
    MPMCQueue mploop = new MPMCQueue(4);

    //1s-1r
    boolean suc = mploop.offer(1234L);
    assertThat(suc, is(true));
    long v = mploop.poll();
    assertThat(v, is(1234L));

    //4s-4r
    suc = mploop.offer(12L);
    assertThat(suc, is(true));
    suc = mploop.offer(34L);
    assertThat(suc, is(true));
    suc = mploop.offer(56L);
    assertThat(suc, is(true));
    suc = mploop.offer(78L);
    assertThat(suc, is(true));
    suc = mploop.offer(90L);
    assertThat(suc, is(false));

    v = mploop.poll();
    assertThat(v, is(12L));
    v = mploop.poll();
    assertThat(v, is(34L));
    v = mploop.poll();
    assertThat(v, is(56L));
    v = mploop.poll();
    assertThat(v, is(78L));
    v = mploop.poll();
    assertThat(v, is(MPMCQueue.NULL));

    //1r-1s-2r-1s-3r-1s-1r
    suc = mploop.offer(11L);
    assertThat(suc, is(true));
    v = mploop.poll();
    assertThat(v, is(11L));
    v = mploop.poll();
    assertThat(v, is(MPMCQueue.NULL));

    suc = mploop.offer(22L);
    assertThat(suc, is(true));
    suc = mploop.offer(33L);
    assertThat(suc, is(true));
    v = mploop.poll();
    assertThat(v, is(22L));

    suc = mploop.offer(44L);
    assertThat(suc, is(true));
    suc = mploop.offer(55L);
    assertThat(suc, is(true));
    suc = mploop.offer(66L);
    assertThat(suc, is(true));
    v = mploop.poll();
    assertThat(v, is(33L));

    suc = mploop.offer(77L);
    assertThat(suc, is(true));
    suc = mploop.offer(1024L);
    assertThat(suc, is(false));

    v = mploop.poll();
    assertThat(v, is(44L));
    v = mploop.poll();
    assertThat(v, is(55L));
    v = mploop.poll();
    assertThat(v, is(66L));
    v = mploop.poll();
    assertThat(v, is(77L));

    v = mploop.poll();
    assertThat(v, is(MPMCQueue.NULL));

    mploop = null;
  }

}
