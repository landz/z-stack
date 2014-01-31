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
import z.channel.GenericHyperLoop;
import z.channel.ReceivePort;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class GenericHyperLoopTest {

  @Test
  public void testSendReceive1() {
    GenericHyperLoop<String> loop = new GenericHyperLoop(8);
    boolean suc = loop.trySend("1234L");
    assertThat(suc, is(true));

    ReceivePort<String> out = loop.createReceivePort();
    assertThat(out.notReceivable(), is(false));

    String v = loop.createReceivePort().tryReceive();
    assertThat(v, is("1234L"));
    System.out.println("GenericHyperLoop.testPlainSendReceive1 done!");
    out = null;
    loop = null;
  }

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testSendReceive2() {
    GenericHyperLoop<String> loop = new GenericHyperLoop(8);
    ReceivePort<String> out = loop.createReceivePort();

    boolean suc;
    suc = loop.trySend("12L");
    assertThat(suc, is(true));
    suc = loop.trySend("34L");
    assertThat(suc, is(true));
    suc = loop.trySend("56L");
    assertThat(suc, is(true));
    suc = loop.trySend("78L");
    assertThat(suc, is(true));
    suc = loop.trySend("78L");
    assertThat(suc, is(true));
    suc = loop.trySend("56L");
    assertThat(suc, is(true));
    suc = loop.trySend("34L");
    assertThat(suc, is(true));
    suc = loop.trySend("12L");
    assertThat(suc, is(true));

    //fail for hitting buffer boundary
    suc = loop.trySend("90L");
    assertThat(suc, is(false));

    //consume first 2 values
    assertThat(out.notReceivable(), is(false));
    assertThat(out.isReceivable(), is(true));
    String v = out.tryReceive();
    assertThat(v, is("12L"));

    assertThat(out.notReceivable(), is(false));
    assertThat(out.isReceivable(), is(true));
    v = out.tryReceive();
    assertThat(v, is("34L"));

    //send 3 values again

    //fail for hitting buffer boundary
    //fail but update minReadCursor
    suc = loop.trySend("90L");
    assertThat(suc, is(false));

    //suc again
    suc = loop.trySend("90L");
    assertThat(suc, is(true));

    suc = loop.trySend("123L");
    assertThat(suc, is(true));

    // buffer full again
    suc = loop.trySend("456L");
    assertThat(suc, is(false));

    //consume the rest
    assertThat(out.notReceivable(), is(false));
    v = out.tryReceive();
    assertThat(v, is("56L"));

    assertThat(out.notReceivable(), is(false));
    v = out.tryReceive();
    assertThat(v, is("78L"));

    assertThat(out.notReceivable(), is(false));
    v = out.tryReceive();
    assertThat(v, is("78L"));

    assertThat(out.notReceivable(), is(false));
    v = out.tryReceive();
    assertThat(v, is("56L"));

    assertThat(out.notReceivable(), is(false));
    v = out.tryReceive();
    assertThat(v, is("34L"));

    assertThat(out.notReceivable(), is(false));
    v = out.tryReceive();
    assertThat(v, is("12L"));

    assertThat(out.notReceivable(), is(false));
    v = out.tryReceive();
    assertThat(v, is("90L"));

    assertThat(out.notReceivable(), is(false));
    v = out.tryReceive();
    assertThat(v, is("123L"));

    //now loop is empty
    assertThat(out.notReceivable(), is(true));
    assertThat(out.isReceivable(), is(false));
    //expect IllegalStateException
    try {
      exception.expect(IllegalStateException.class);
      v = out.tryReceive();
    } finally {
      System.out.println("GenericHyperLoop.testPlainSendReceive2 done!");
      out = null;
      loop = null;
    }
  }

  @Test
  public void testSendReceive3() {
    GenericHyperLoop<String> loop = new GenericHyperLoop(1024);
    ReceivePort<String> out = loop.createReceivePort();

    for (int i = 0; i < 1024; i++) {
      boolean suc = loop.trySend(String.valueOf(i));
      assertThat(suc, is(true));
    }

    boolean suc = loop.trySend("123L");
    assertThat(suc, is(false));

    for (long i = 0; i < 1024; i++) {
      assertThat(out.notReceivable(), is(false));
      String v = out.tryReceive();
      assertThat(v, is(String.valueOf(i)));
    }

    assertThat(out.notReceivable(), is(true));
    assertThat(out.isReceivable(), is(false));

    System.out.println("GenericHyperLoop.testSendReceive3 done!");
    out = null;
    loop = null;
  }

  @Test
  public void testSendReceive4() {
    GenericHyperLoop<String> loop = new GenericHyperLoop<String>(512);
    ReceivePort<String> out0 = loop.createReceivePort();
    ReceivePort<String> out1 = loop.createReceivePort();

    for (int i = 0; i < 512; i++) {
      boolean suc = loop.trySend(String.valueOf(i));
      assertThat(suc, is(true));
    }

    boolean suc = loop.trySend("123L");
    assertThat(suc, is(false));

    for (int i = 0; i < 512; i++) {
      assertThat(out0.notReceivable(), is(false));
      String v = out0.tryReceive();
      assertThat(v, is(String.valueOf(i)));
    }

    assertThat(out0.notReceivable(), is(true));
    assertThat(out0.isReceivable(), is(false));

    //but not for sender, still fails
    suc = loop.trySend("123L");
    assertThat(suc, is(false));

    //and not for out1
    for (int i = 0; i < 512; i++) {
      assertThat(out1.notReceivable(), is(false));
      assertThat(out1.isReceivable(), is(true));
      String v = out1.tryReceive();
      assertThat(v, is(String.valueOf(i)));
    }

    //fail to wait the minReadCursor update
    suc = loop.trySend("#$%^&*");
    assertThat(suc, is(false));

    //again, but large long
    for (int i = 0; i < 512; i++) {
      suc = loop.trySend(Integer.toBinaryString(i << 24));
      assertThat(suc, is(true));
    }

    //still fails
    suc = loop.trySend("#$%^&*");
    assertThat(suc, is(false));


    for (int i = 0; i < 512; i++) {
      assertThat(out0.notReceivable(), is(false));
      assertThat(out1.isReceivable(), is(true));
      String v0 = out0.tryReceive();
      String v1 = out1.tryReceive();
      assertThat(v0, is(v1));
      assertThat(v0, is(Integer.toBinaryString(i<<24)));
    }


    assertThat(out0.notReceivable(), is(true));
    assertThat(out0.isReceivable(), is(false));
    assertThat(out1.notReceivable(), is(true));
    assertThat(out1.isReceivable(), is(false));


    System.out.println("GenericHyperLoop.testSendReceive4 done!");
    out0 = null;
    loop = null;
  }

}
