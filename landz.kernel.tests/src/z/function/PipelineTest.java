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

package z.function;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class PipelineTest {

  @Test
  public void sanityCheck() {

    Pipeline<Integer, Boolean> testPipeline =
        Pipeline
            .create(PipelineTest::func1)
            .next(PipelineTest::func2)
            .end();

    assertThat(testPipeline.apply(12),is(false));
    assertThat(testPipeline.apply(123),is(true));

    //
    Pipeline<String, Double> pl2 =
        Pipeline
            .create(PipelineTest::func3)
            .end();

    //
    assertThat(pl2.apply("3.14159"),is(3.14159));

    //
    Pipeline<Boolean, Integer> pl3 =
        Pipeline
            .create(PipelineTest::find)
            .next(PipelineTest::stat)
            .end();

    //
    assertThat(pl3.apply(true),is(2));
    assertThat(pl3.apply(false),is(0));

  }

  static String func1(int x) {
    return String.valueOf(x);
  }

  static boolean func2(String s) {
    return s.startsWith("123")?true:false;
  }

  static Double func3(String s) {
    return Double.valueOf(s);
  }

  static List<String> find(boolean need) {
    return need ?
        Arrays.asList("s1","s2","1s","2s") : Collections.<String>emptyList();
  }

  static int stat(List<String> list) {
    return list.stream().filter(s->s.startsWith("s")).mapToInt(e->1).sum();
  }

  static void voidReturn(List<String> list) {
  }

}
