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
import z.znr.Clock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

public class ClockTest {

  @Test
  public void testRdtsc() {
    long tsc1 = Clock.rdtsc();
    long tsc2 = Clock.rdtsc();
    System.out.println(tsc1);
    System.out.println(tsc2);
    assertThat(tsc2-tsc1,greaterThan(0L));
    assertThat(tsc2-tsc1,lessThan(100_000L));//ok?
  }

  @Test
  public void testRdtscp() {
    long tsc1 = Clock.rdtscp();
    long tsc2 = Clock.rdtscp();
    System.out.println(tsc1);
    System.out.println(tsc2);
    assertThat(tsc2-tsc1,greaterThan(0L));
    assertThat(tsc2-tsc1,lessThan(100_000L));//ok?
  }

  @Test
  public void testEstimateTSCFrequency() {
    System.out.printf("Estimated the frequency of TSC is: %,d",
        Clock.estimateTSCFrequency());
  }

  @Test
  public void testNanoTSCTime() {
    int RUNS = 40_000_000;
    long a=0;
    long s = Clock.rdtsc();//warmup?

    for (int i = 0; i < RUNS; i++) {
      a = System.nanoTime();
    }

    s = Clock.rdtsc();
    for (int i = 0; i < RUNS; i++) {
      a = System.nanoTime();
    }
    long t2 = Clock.rdtsc()-s;

    s = System.nanoTime();
    for (int i = 0; i < RUNS; i++) {
      a = System.nanoTime();
    }
    long t1 = System.nanoTime()-s;

//    System.out.println(Clock.tscToNano(t2));System.out.println(t1);
    assertThat(Math.abs(Clock.tscToNano(t2) - t1)*1D/t1,lessThan(0.08D));//ok?
  }
}
