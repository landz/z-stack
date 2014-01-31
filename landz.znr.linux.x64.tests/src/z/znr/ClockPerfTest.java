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
import z.znr.Clock;

import static org.hamcrest.MatcherAssert.assertThat;
import static z.znr.Affinity.bindTo;

public class ClockPerfTest {

  @Test
  /**
   * 4% slower than that of JNI version
   * (https://github.com/peter-lawrey/Java-Thread-Affinity)
   */
  public void testRdtscPerf() {
    bindTo(Affinity.Topology.socket(0).physicalCore(3).virtualCore(1));
    long tsc = Clock.rdtsc();

    int RUNS = 100_000_000;
    long s = System.nanoTime();
    for (int i = 0; i < RUNS; i++) {
      tsc = Clock.rdtsc();
    }
    long t = System.nanoTime()-s;
    System.out.printf(
        "run %,d times of Clock.rdtsc() cost: %,d nanos\n", RUNS, t);
    System.out.println(
        "Or one shot cost of Clock.rdtsc()is: " + t * 1.0 / RUNS + " nanos");
  }

  @Test
  /**
   * 1.4x slower than rdtsc
   */
  public void testRdtscpPerf() {
    long tsc = Clock.rdtscp();

    int RUNS = 100_000_000;
    long s = System.nanoTime();
    for (int i = 0; i < RUNS; i++) {
      tsc = Clock.rdtscp();
    }
    long t = System.nanoTime()-s;
    System.out.printf(
        "run %,d times of Clock.rdtscp() cost: %,d nanos\n", RUNS, t);
    System.out.println(
        "Or one shot cost of Clock.rdtscp()is: "+ t*1.0/RUNS +" nanos");

    System.out.println(tsc);
  }
}
