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
import z.channel.GenericMPMCQueue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class GenericMPMCQueueTest {

  @Test
  public void testSendReceive1() {
    GenericMPMCQueue<String> queue = new GenericMPMCQueue(4);

    //1s-1r
    boolean suc = queue.offer("1234L");
    assertThat(suc, is(true));
    String v = queue.poll();
    assertThat(v, is("1234L"));

    //4s-4r
    suc = queue.offer("12L");
    assertThat(suc, is(true));
    suc = queue.offer("34L");
    assertThat(suc, is(true));
    suc = queue.offer("56L");
    assertThat(suc, is(true));
    suc = queue.offer("78L");
    assertThat(suc, is(true));
    suc = queue.offer("90L");
    assertThat(suc, is(false));

    v = queue.poll();
    assertThat(v, is("12L"));
    v = queue.poll();
    assertThat(v, is("34L"));
    v = queue.poll();
    assertThat(v, is("56L"));
    v = queue.poll();
    assertThat(v, is("78L"));
    v = queue.poll();
    assertThat(v, is((String)null));

    //1r-1s-2r-1s-3r-1s-1r
    suc = queue.offer("11L");
    assertThat(suc, is(true));
    v = queue.poll();
    assertThat(v, is("11L"));
    v = queue.poll();
    assertThat(v, is((String)null));

    suc = queue.offer("22L");
    assertThat(suc, is(true));
    suc = queue.offer("33L");
    assertThat(suc, is(true));
    v = queue.poll();
    assertThat(v, is("22L"));

    suc = queue.offer("44L");
    assertThat(suc, is(true));
    suc = queue.offer("55L");
    assertThat(suc, is(true));
    suc = queue.offer("66L");
    assertThat(suc, is(true));
    v = queue.poll();
    assertThat(v, is("33L"));

    suc = queue.offer("77L");
    assertThat(suc, is(true));
    suc = queue.offer("1024L");
    assertThat(suc, is(false));

    v = queue.poll();
    assertThat(v, is("44L"));
    v = queue.poll();
    assertThat(v, is("55L"));
    v = queue.poll();
    assertThat(v, is("66L"));
    v = queue.poll();
    assertThat(v, is("77L"));

    v = queue.poll();
    assertThat(v, is((String)null));

    queue = null;
  }

}
