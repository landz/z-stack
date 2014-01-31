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

package z.znr;

import org.junit.Test;
import z.znr.LibC;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static z.znr.LibC.getpid;
import static z.znr.LibC.gettimeofday;

public class LibCPerfTest {

  static {
    System.setProperty("jnr.invoke.compile.dump", "false");
  }

  @Test
  public void testGetpidPerf() {
    int pid = getpid();

    long s = System.nanoTime();
    for (int i = 0; i < 100_000_000; i++) {
      pid = getpid();
    }
    long t = System.nanoTime()-s;
    System.out.println("getpid cost: "+t+" nanos");

    System.out.println(pid);
    assertThat(pid, is(getpid()));
    assertThat(pid, greaterThan(0));
  }

  @Test
  public void testGettimeofdayPerf() {
    LibC.Timeval tv = new LibC.Timeval();
    int suc1 = gettimeofday(tv);

    int RUNS = 50_000_000;
    long s = System.nanoTime();
    for (int i = 0; i < RUNS; i++) {
      suc1 = gettimeofday(tv);
    }
    long t = System.nanoTime()-s;
    System.out.println(
        "run "+RUNS+" times of gettimeofday cost: "+t+" nanos");
    System.out.println(
        "Or one shot cost of gettimeofday is: "+ t*1.0/RUNS +" nanos");

    System.out.println(tv.tv_sec);
    assertThat(suc1, is(0));
  }

}
