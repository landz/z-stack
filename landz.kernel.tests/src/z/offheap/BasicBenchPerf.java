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

package z.offheap;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import z.testware.benchmark.BenchmarkOptions;
import z.testware.benchmark.BenchmarkRule;

import static z.util.Unsafes.*;
/**
 * Created by jin on 8/28/13.
 */
public class BasicBenchPerf {

    public static final int COUNT = 10_000_000;
    public static final int SIZE_128k = 128 * 1024;
    public static final int SIZE_260k = 260 * 1024;
    public static final int SIZE_200k = 200 * 1024;

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    @Test
    @BenchmarkOptions(benchmarkRounds = 2, warmupRounds = 2)
    public void allocateAndFreeMemoryCost1024() {
        for (int i = 0; i < COUNT; i++) {
            long address = UNSAFE.allocateMemory(1024);
//            address++;
//            address--;
            UNSAFE.freeMemory(address);
        }
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 2, warmupRounds = 2)
    public void allocateAndFreeMemoryCost65536() {
        for (int i = 0; i < COUNT; i++) {
            long address = UNSAFE.allocateMemory(65536);
//            address++;
//            address--;
            UNSAFE.freeMemory(address);
        }
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 2, warmupRounds = 2)
    public void allocateAndFreeMemoryCost128k() {
        for (int i = 0; i < COUNT; i++) {
            long address = UNSAFE.allocateMemory(SIZE_128k);
//            address++;
//            address--;
            UNSAFE.freeMemory(address);
        }
    }

        @Test
    @BenchmarkOptions(benchmarkRounds = 2, warmupRounds = 2)
    public void allocateAndFreeMemoryCost200k() {

        for (int i = 0; i < 1000_000; i++) {
            long address = UNSAFE.allocateMemory(SIZE_200k);
//            address++;
//            address--;
            UNSAFE.freeMemory(address);
        }
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 2, warmupRounds = 2)
    /**
     * ?:
     * < 128K use glibc brk
     * > 128K use mmap
     * speed: 100x diff
     *
     * malloc/malloc.c:
     * (DEFAULT_MMAP_THRESHOLD_MAX): For 32-bit platforms define as 512KFor 64-bit platforms as 32MB.
     */
    public void allocateAndFreeMemoryCost260k() {
        //XX: COUNT -> COUNT/10
        for (int i = 0; i < 1000_000; i++) {
            long address = UNSAFE.allocateMemory(SIZE_260k);
//            address++;
//            address--;
            UNSAFE.freeMemory(address);
        }
    }


}
