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

package z.async.fj;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import static z.util.Throwables.uncheck;

public class FindMax extends RecursiveTask<Integer> {

  private final int[] data;
  private final int start;
  private final int end;

  public FindMax(int[] data, int start, int end) {
    this.data = data;
    this.start = start;
    this.end = end;
  }

  public FindMax(int[] data) {
    this(data, 0, data.length-1);
  }

  @Override
  protected Integer compute() {
    if (end == start) {
      return computeDirectly();
    }
    int mid = start + (end-start) / 2;
    FindMax left = new FindMax(data, start, mid);
    left.fork();
    FindMax right = new FindMax(data, mid+1, end);
    return Math.max(right.compute(), left.join());
  }

  private Integer computeDirectly() {
//    if (end==2)
//      uncheck(()->Thread.sleep(1000_000L));
    return data[end];
  }

  public static void main(String[] args) {
//    int COUNT = 10_000;
//    int[] data = new int[COUNT];
//    ThreadLocalRandom rnd = ThreadLocalRandom.current();
//    for (int i = 0; i < COUNT; i++) {
//      data[i] = rnd.nextInt(0,COUNT);
//    }
    int[] data = {3,2,1};

    final ForkJoinPool pool = new ForkJoinPool(4);
    final FindMax finder = new FindMax(data);

    System.out.println(pool.invoke(finder));
  }
}