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
import z.znr.Affinity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static z.util.Throwables.uncheck;
import static z.znr.LibC.*;

public class LibCTest {
  static {
    System.setProperty("jnr.invoke.compile.dump", "false");
  }

  @Test
  public void testGetpid() {
    int pid = getpid();
    System.out.println(pid);
    assertThat(pid, is(getpid()));
    assertThat(pid, greaterThan(0));
  }

  @Test
  public void testGettimeofday() {
    Timeval tv1 = new Timeval();
    Timeval tv2 = new Timeval();
    int suc1 = gettimeofday(tv1);
    int suc2 = gettimeofday(tv2);
    assertThat(suc1, is(0));
    assertThat(suc2, is(0));
    assertThat(tv1.tv_sec-tv2.tv_sec,lessThanOrEqualTo(1L));
    assertThat(tv1.tv_sec-System.currentTimeMillis()/1000,
        lessThanOrEqualTo(1L));
//    try {
//      tv1 = null;
//      tv2 = null;
//      System.gc();
//      Thread.sleep(10_000L);
//    } catch (Exception e){e.printStackTrace();}
  }

  @Test
  public void testGetaffinity() {
    Cpuset cpuset = new Cpuset();
    int suc1 = getAffinity(0, cpuset);
    assertThat(suc1, is(0));
    assertThat(cpuset.getMask(), greaterThanOrEqualTo(0L));
    // default the thread can run on all vcores
    assertThat(cpuset.getMask(),is((1L<< Affinity.getNumberOfVirtualCores())-1));
  }

  @Test
  public void testCpuset() {
    Cpuset cpuset = new Cpuset();
    cpuset = null;
    System.gc();
    uncheck(() -> Thread.sleep(6_000L));
    assertThat(true,is(true));//it is OK to reach here
  }

}
