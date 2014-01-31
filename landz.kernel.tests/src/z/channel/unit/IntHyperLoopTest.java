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
import z.channel.IntHyperLoop;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class IntHyperLoopTest {

  @Test
  public void testSendReceive1() {
    IntHyperLoop loop = new IntHyperLoop(8);
    boolean suc = loop.trySend(1234);
    assertThat(suc, is(true));

    IntHyperLoop.OutPort out = loop.createOutPort();
    assertThat(out.notReceivable(), is(false));

    int v = out.receive();
    assertThat(v, is(1234));
    System.out.println("IntHyperLoopTest#testPlainSendReceive1 done!");
    out = null;
    loop = null;
  }

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testSendReceive2() {
    IntHyperLoop loop = new IntHyperLoop(8);
    IntHyperLoop.OutPort out = loop.createOutPort();

    boolean suc;
    suc = loop.trySend(12);
    assertThat(suc, is(true));
    suc = loop.trySend(34);
    assertThat(suc, is(true));
    suc = loop.trySend(56);
    assertThat(suc, is(true));
    suc = loop.trySend(78);
    assertThat(suc, is(true));
    suc = loop.trySend(78);
    assertThat(suc, is(true));
    suc = loop.trySend(56);
    assertThat(suc, is(true));
    suc = loop.trySend(34);
    assertThat(suc, is(true));
    suc = loop.trySend(12);
    assertThat(suc, is(true));

    //fail for hitting buffer boundary
    suc = loop.trySend(90);
    assertThat(suc, is(false));

    //consume first 2 values
    assertThat(out.notReceivable(), is(false));
    assertThat(out.isReceivable(), is(true));
    int v = out.receive();
    assertThat(v, is(12));

    assertThat(out.notReceivable(), is(false));
    assertThat(out.isReceivable(), is(true));
    v = out.receive();
    assertThat(v, is(34));

    //send 3 values again

    //fail for hitting buffer boundary
    //fail but update minReadCursor
    suc = loop.trySend(90);
    assertThat(suc, is(false));

    //suc again
    suc = loop.trySend(90);
    assertThat(suc, is(true));

    suc = loop.trySend(123);
    assertThat(suc, is(true));

    // buffer full again
    suc = loop.trySend(456);
    assertThat(suc, is(false));

    //consume the rest
    assertThat(out.notReceivable(), is(false));
    v = out.receive();
    assertThat(v, is(56));

    assertThat(out.notReceivable(), is(false));
    v = out.receive();
    assertThat(v, is(78));

    assertThat(out.notReceivable(), is(false));
    v = out.receive();
    assertThat(v, is(78));

    assertThat(out.notReceivable(), is(false));
    v = out.receive();
    assertThat(v, is(56));

    assertThat(out.notReceivable(), is(false));
    v = out.receive();
    assertThat(v, is(34));

    assertThat(out.notReceivable(), is(false));
    v = out.receive();
    assertThat(v, is(12));

    assertThat(out.notReceivable(), is(false));
    v = out.receive();
    assertThat(v, is(90));

    assertThat(out.notReceivable(), is(false));
    v = out.receive();
    assertThat(v, is(123));

    //now loop is empty
    assertThat(out.notReceivable(), is(true));
    assertThat(out.isReceivable(), is(false));
    //expect IllegalStateException
    try {
      exception.expect(IllegalStateException.class);
      v = out.receive();
    } finally {
      System.out.println("IntHyperLoopTest#testPlainSendReceive2 done!");
      out = null;
      loop = null;
    }
  }

  @Test
  public void testSendReceive3() {
    IntHyperLoop loop = new IntHyperLoop(1024);
    IntHyperLoop.OutPort out = loop.createOutPort();

    for (int i = 0; i < 1024; i++) {
      boolean suc = loop.trySend(i);
      assertThat(suc, is(true));
    }

    boolean suc = loop.trySend(123);
    assertThat(suc, is(false));

    for (int i = 0; i < 1024; i++) {
      assertThat(out.notReceivable(), is(false));
      int v = out.receive();
      assertThat(v, is(i));
    }

    assertThat(out.notReceivable(), is(true));
    assertThat(out.isReceivable(), is(false));

    System.out.println("IntHyperLoopTest#testSendReceive3 done!");
    out = null;
    loop = null;
  }

  @Test
  public void testSendReceive4() {
    IntHyperLoop loop = new IntHyperLoop(512);
    IntHyperLoop.OutPort out0 = loop.createOutPort();
    IntHyperLoop.OutPort out1 = loop.createOutPort();

    for (int i = 0; i < 512; i++) {
      boolean suc = loop.trySend(i);
      assertThat(suc, is(true));
    }

    boolean suc = loop.trySend(123);
    assertThat(suc, is(false));

    for (int i = 0; i < 512; i++) {
      assertThat(out0.notReceivable(), is(false));
      int v = out0.receive();
      assertThat(v, is(i));
    }

    assertThat(out0.notReceivable(), is(true));
    assertThat(out0.isReceivable(), is(false));

    //but not for sender, still fails
    suc = loop.trySend(123);
    assertThat(suc, is(false));

    //and not for out1
    for (int i = 0; i < 512; i++) {
      assertThat(out1.notReceivable(), is(false));
      assertThat(out1.isReceivable(), is(true));
      int v = out1.receive();
      assertThat(v, is(i));
    }

    //fail to wait the minReadCursor update
    suc = loop.trySend(Integer.MAX_VALUE);
    assertThat(suc, is(false));

    //again, but large int
    for (int i = 0; i < 512; i++) {
      suc = loop.trySend(i << 22);
      assertThat(suc, is(true));
    }

    //still fails
    suc = loop.trySend(Integer.MAX_VALUE);
    assertThat(suc, is(false));


    for (int i = 0; i < 512; i++) {
      assertThat(out0.notReceivable(), is(false));
      assertThat(out1.isReceivable(), is(true));
      int v0 = out0.receive();
      int v1 = out1.receive();
      assertThat(v0, is(v1));
      assertThat(v0, is(i<<22));
    }


    assertThat(out0.notReceivable(), is(true));
    assertThat(out0.isReceivable(), is(false));
    assertThat(out1.notReceivable(), is(true));
    assertThat(out1.isReceivable(), is(false));

    System.out.println("IntHyperLoopTest#testSendReceive4 done!");
    out0 = null;
    loop = null;
  }

}
