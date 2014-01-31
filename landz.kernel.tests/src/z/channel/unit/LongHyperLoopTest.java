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

package z.channel.unit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import z.channel.LongHyperLoop;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LongHyperLoopTest {

  @Test
  public void testSendReceive1() {
    LongHyperLoop loop = new LongHyperLoop(8);
    boolean suc = loop.send(1234L);
    assertThat(suc, is(true));

    LongHyperLoop.OutPort out = loop.createOutPort();
    assertThat(out.notReceivable(), is(false));

    long v = out.receive();
    assertThat(v, is(1234L));
    System.out.println("testPlainSendReceive1 done!");
    out = null;
    loop = null;
  }

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testSendReceive2() {
    LongHyperLoop loop = new LongHyperLoop(8);
    LongHyperLoop.OutPort out = loop.createOutPort();

    boolean suc;
    suc = loop.send(12L);
    assertThat(suc, is(true));
    suc = loop.send(34L);
    assertThat(suc, is(true));
    suc = loop.send(56L);
    assertThat(suc, is(true));
    suc = loop.send(78L);
    assertThat(suc, is(true));
    suc = loop.send(78L);
    assertThat(suc, is(true));
    suc = loop.send(56L);
    assertThat(suc, is(true));
    suc = loop.send(34L);
    assertThat(suc, is(true));
    suc = loop.send(12L);
    assertThat(suc, is(true));

    //fail for hitting buffer boundary
    suc = loop.send(90L);
    assertThat(suc, is(false));

    //consume first 2 values
    assertThat(out.notReceivable(), is(false));
    assertThat(out.isReceivable(), is(true));
    long v = out.receive();
    assertThat(v, is(12L));

    assertThat(out.notReceivable(), is(false));
    assertThat(out.isReceivable(), is(true));
    v = out.receive();
    assertThat(v, is(34L));

    //send 3 values again

    //fail for hitting buffer boundary
    //fail but update minReadCursor
    suc = loop.send(90L);
    assertThat(suc, is(false));

    //suc again
    suc = loop.send(90L);
    assertThat(suc, is(true));

    suc = loop.send(123L);
    assertThat(suc, is(true));

    // buffer full again
    suc = loop.send(456L);
    assertThat(suc, is(false));

    //consume the rest
    assertThat(out.notReceivable(), is(false));
    v = out.receive();
    assertThat(v, is(56L));

    assertThat(out.notReceivable(), is(false));
    v = out.receive();
    assertThat(v, is(78L));

    assertThat(out.notReceivable(), is(false));
    v = out.receive();
    assertThat(v, is(78L));

    assertThat(out.notReceivable(), is(false));
    v = out.receive();
    assertThat(v, is(56L));

    assertThat(out.notReceivable(), is(false));
    v = out.receive();
    assertThat(v, is(34L));

    assertThat(out.notReceivable(), is(false));
    v = out.receive();
    assertThat(v, is(12L));

    assertThat(out.notReceivable(), is(false));
    v = out.receive();
    assertThat(v, is(90L));

    assertThat(out.notReceivable(), is(false));
    v = out.receive();
    assertThat(v, is(123L));

    //now loop is empty
    assertThat(out.notReceivable(), is(true));
    assertThat(out.isReceivable(), is(false));
    //expect IllegalStateException
    try {
      exception.expect(IllegalStateException.class);
      v = out.receive();
    } finally {
      System.out.println("testPlainSendReceive2 done!");
      out = null;
      loop = null;
    }
  }

  @Test
  public void testSendReceive3() {
    LongHyperLoop loop = new LongHyperLoop(1024);
    LongHyperLoop.OutPort out = loop.createOutPort();

    for (long i = 0; i < 1024; i++) {
      boolean suc = loop.send(i);
      assertThat(suc, is(true));
    }

    boolean suc = loop.send(123L);
    assertThat(suc, is(false));

    for (long i = 0; i < 1024; i++) {
      assertThat(out.notReceivable(), is(false));
      long v = out.receive();
      assertThat(v, is(i));
    }

    assertThat(out.notReceivable(), is(true));
    assertThat(out.isReceivable(), is(false));

    System.out.println("testSendReceive3 done!");
    out = null;
    loop = null;
  }

  @Test
  public void testSendReceive4() {
    LongHyperLoop loop = new LongHyperLoop(512);
    LongHyperLoop.OutPort out0 = loop.createOutPort();
    LongHyperLoop.OutPort out1 = loop.createOutPort();

    for (long i = 0; i < 512; i++) {
      boolean suc = loop.send(i);
      assertThat(suc, is(true));
    }

    boolean suc = loop.send(123L);
    assertThat(suc, is(false));

    for (long i = 0; i < 512; i++) {
      assertThat(out0.notReceivable(), is(false));
      long v = out0.receive();
      assertThat(v, is(i));
    }

    assertThat(out0.notReceivable(), is(true));
    assertThat(out0.isReceivable(), is(false));

    //but not for sender, still fails
    suc = loop.send(123L);
    assertThat(suc, is(false));

    //and not for out1
    for (long i = 0; i < 512; i++) {
      assertThat(out1.notReceivable(), is(false));
      assertThat(out1.isReceivable(), is(true));
      long v = out1.receive();
      assertThat(v, is(i));
    }

    //fail to wait the minReadCursor update
    suc = loop.send(Long.MAX_VALUE);
    assertThat(suc, is(false));

    //again, but large long
    for (long i = 0; i < 512; i++) {
      suc = loop.send(i<<44);
      assertThat(suc, is(true));
    }

    //still fails
    suc = loop.send(Long.MAX_VALUE);
    assertThat(suc, is(false));


    for (long i = 0; i < 512; i++) {
      assertThat(out0.notReceivable(), is(false));
      assertThat(out1.isReceivable(), is(true));
      long v0 = out0.receive();
      long v1 = out1.receive();
      assertThat(v0, is(v1));
      assertThat(v0, is(i<<44));
    }


    assertThat(out0.notReceivable(), is(true));
    assertThat(out0.isReceivable(), is(false));
    assertThat(out1.notReceivable(), is(true));
    assertThat(out1.isReceivable(), is(false));


    System.out.println("testSendReceive4 done!");
    out0 = null;
    loop = null;
  }

}
