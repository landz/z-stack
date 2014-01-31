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

package z.testware.common;

import z.annotation.NotThreadSafe;

import java.util.concurrent.TimeUnit;

import static z.util.Throwables.uncheck;

@NotThreadSafe
public class Stopwatch {

  private String name;
  private long startTime = 0;
  private long stopTime = 0;
  private boolean running = false;

  public Stopwatch() {
  }

  public Stopwatch(String name) {
    this.name = name;
  }

  public static Stopwatch create() {
    return new Stopwatch();
  }

  public static Stopwatch create(String name) {
    return new Stopwatch(name);
  }

  public void start() {
    this.running = true;
    this.startTime = System.nanoTime();
  }

  /**
   * Note: this method also invoke System.gc
   */
  public void stop() {
    this.stopTime = System.nanoTime();
    this.running = false;

    System.gc();
    uncheck(()->Thread.sleep(2000));
  }

  /**
   * Note: this method also invoke System.gc
   */
  public void reset() {
    this.startTime = 0;
    this.stopTime = 0;
    this.running = false;

    System.gc();
    uncheck(()->Thread.sleep(2000));
  }

  public boolean isRunning() {
    return running;
  }

  /**
   *
   * @return elapsed time, in nanosecond
   */
  public long elapsed() {
    //TODO
    long elapsed0 = System.nanoTime() - startTime;
    if (running) {
      return elapsed0;
    } else {
      return stopTime - startTime;
    }
  }

  public long elapsedMicroseconds() {
    return TimeUnit.NANOSECONDS.toMicros(elapsed());
  }

  public long elapsedMilliseconds() {
    return TimeUnit.NANOSECONDS.toMillis(elapsed());
  }

  public long elapsedSeconds() {
    return TimeUnit.NANOSECONDS.toSeconds(elapsed());
  }

  @Override
  public String toString() {
    return "[Stopwatch(" + (isRunning() ? "Running" : "Stopped") + "): " + name + "]" +
        "{elapsed " + elapsed() + " nanoseconds}";
  }

  public void print() {
    System.out.println(toString());
  }

  public void printMicros() {
    System.out.println("[Stopwatch(" + (isRunning() ? "Running" : "Stopped") + "): " + name + "]" +
        "{elapsed " + elapsedMicroseconds() + " microseconds}");
  }

  public void printMillis() {
    System.out.println("[Stopwatch(" + (isRunning() ? "Running" : "Stopped") + "): " + name + "]" +
        "{elapsed " + elapsedMilliseconds() + " milliseconds}");
  }

  public void printSecs() {
    System.out.println("[Stopwatch(" + (isRunning() ? "Running" : "Stopped") + "): " + name + "]" +
        "{elapsed " + elapsedSeconds() + " seconds}");
  }


}
