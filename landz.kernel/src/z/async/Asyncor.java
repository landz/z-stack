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

package z.async;

/**
 * Asyncor, is a message-passing based light weight execution unit.
 *
 * <p>
 * Asyncor, in fact, may have characteristics of Carl Hewitt's "Actor"[1].
 *
 * <p>
 * However, it is not equivalent to some coroutine/fiber/green thread concepts:
 * the async does not have pause/resume semantics, so it does not provide any
 * magic work for the execution context.
 *
 * <p>
 * One async terminates only in two termination status: actively completed
 * (includes exceptional terminations) or passively timeout.
 *
 * <p>
 * A suite of DSL for working with async has been provided in {@link Asyncors}
 * .
 *
 * <p>
 *
 */
public class Asyncor {
  long     id;
  String   name;
  int      tid = -1;
  Asyncor  next;
  final Runnable body;

  public Asyncor(Runnable body){
    this.body = body;
  }
}
