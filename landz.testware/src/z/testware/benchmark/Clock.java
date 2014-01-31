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

package z.testware.benchmark;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Used to specify what time to measure in {@link BenchmarkOptions}.
 */
public enum Clock
{

    /**
     * Invokes {@link System#currentTimeMillis()}
     */
    REAL_TIME
            {
                @Override
                long time()
                {
                    return System.currentTimeMillis();
                }
            },

    /**
     * Invokes {@link System#nanoTime()}
     */
    NANO_TIME
            {
                @Override
                long time()
                {
                    return System.nanoTime() / FACTOR;
                }
            },

    /**
     * Invokes {@link java.lang.management.ThreadMXBean#getCurrentThreadCpuTime()}
     */
    CPU_TIME
            {
                @Override
                long time()
                {
                    if (mxBean.isThreadCpuTimeSupported())
                    {
                        return mxBean.getCurrentThreadCpuTime() / FACTOR;
                    } else
                    {
                        throw new RuntimeException("ThreadCpuTime is not supported. Impossible to use Clock.CPU_TIME");
                    }
                }
            },
    /**
     * Invokes {@link java.lang.management.ThreadMXBean#getCurrentThreadUserTime()}
     */
    USER_TIME
            {
                @Override
                long time()
                {
                    if (mxBean.isThreadCpuTimeSupported())
                    {
                        return mxBean.getCurrentThreadUserTime() / FACTOR;
                    } else
                    {
                        throw new RuntimeException("ThreadCpuTime is not supported. Impossible to use Clock.USER_TIME");
                    }
                }
            };

    private static final int FACTOR = 1000000;
    private static ThreadMXBean mxBean;

    static
    {
        mxBean = ManagementFactory.getThreadMXBean();
        if (mxBean.isThreadCpuTimeSupported()) mxBean.setThreadCpuTimeEnabled(true);
    }

    abstract long time();
}
