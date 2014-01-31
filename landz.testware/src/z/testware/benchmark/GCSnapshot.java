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

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * Snapshot of GC activity (cumulative for all GC types).
 */
public final class GCSnapshot
{
    private static List<GarbageCollectorMXBean> garbageBeans = ManagementFactory
        .getGarbageCollectorMXBeans();

    private long [] gcInvocations = new long [garbageBeans.size()];
    private long [] gcTimes = new long [garbageBeans.size()];

    GCSnapshot()
    {
        for (int i = 0; i < gcInvocations.length; i++)
        {
            gcInvocations[i] = garbageBeans.get(i).getCollectionCount();
            gcTimes[i] = garbageBeans.get(i).getCollectionTime();
        }
    }

    public long accumulatedInvocations()
    {
        long sum = 0;
        int i = 0;
        for (GarbageCollectorMXBean bean : garbageBeans)
        {
            sum += bean.getCollectionCount() - gcInvocations[i++];
        }
        return sum;
    }

    public long accumulatedTime()
    {
        long sum = 0;
        int i = 0;
        for (GarbageCollectorMXBean bean : garbageBeans)
        {
            sum += bean.getCollectionTime() - gcTimes[i++];
        }
        return sum;
    }
}