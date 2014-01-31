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

import java.io.*;
import java.util.Locale;

/**
 * {@link IResultsConsumer} printing benchmark results to a given writer. 
 */
public final class WriterConsumer implements IResultsConsumer
{
    private final Writer w;

    public WriterConsumer()
    {
        this(getDefaultWriter());
    }

    public WriterConsumer(Writer w)
    {
        this.w = w;
    }

    public void accept(Result result) throws IOException
    {
        w.write(String.format(Locale.ENGLISH,
            "%s:\n" +
            "[measured %d out of %d rounds, %s, time unit(second)]\n" +
            "  average time of single round:                    %s\n" +
            "  average time of blocked status in single round:  %s\n" +
            "  average time of GC in single round:              %s\n" +
            "  total calls of GC:                               %d\n" +
            "  total time of GC:                                %.3f\n" +
            "  total warmup time:                               %.3f\n" +
            "  total bench time:                                %.3f\n" +
            "  whole time spent for whole(warmup+bench):        %.3f\n\n",
            result.getShortTestClassName() + "." + result.getTestMethodName(),
            result.benchmarkRounds, result.benchmarkRounds + result.warmupRounds, concurrencyToText(result),
            result.roundAverage.toString(),
            result.blockedAverage.toString(),
            result.gcAverage.toString(), 
            result.gcInfo.accumulatedInvocations(), 
            result.gcInfo.accumulatedTime() / 1000.0,
            result.warmupTime * 0.001,
            result.benchmarkTime * 0.001,
            (result.warmupTime + result.benchmarkTime) * 0.001
        ));
        w.flush();
    }

    private String concurrencyToText(Result result)
    {
        int threads = result.getThreadCount();
        if (threads == Runtime.getRuntime().availableProcessors())
        {
            return "threads: " + threads +
                " (all cores)";
        }

        if (threads == 1)
        {
            return "threads: 1 (sequential)";
        }

        return "threads: " + result.concurrency
            + " (physical processors: " + Runtime.getRuntime().availableProcessors() + ")";
    }

    /**
     * Return the default writer (zee).
     */
    private static Writer getDefaultWriter()
    {
        return new OutputStreamWriter(System.out)
        {
            public void close() throws IOException
            {
                // Don't close the superstream.
            }
        };
    }
}
