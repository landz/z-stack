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

import java.util.Locale;

/**
 * Average with standard deviation.
 */
public final class Average
{
    /**
     * Average (in milliseconds).
     */
    public final double avg;

    /**
     * Standard deviation (in milliseconds).
     */
    public final double stddev;

    /**
     * 
     */
    Average(double avg, double stddev)
    {
        this.avg = avg;
        this.stddev = stddev;
    }

    public String toString()
    {
        return String.format(Locale.ENGLISH, "%.3f [+- %.3f]",
            avg, stddev);
    }

    static Average from(long [] values)
    {
        long sum = 0;
        long sumSquares = 0;

        for (long l : values)
        {
            sum += l;
            sumSquares += l * l;
        }

        double avg = sum / (double) values.length;
        return new Average(
            (sum / (double) values.length) / 1000.0, 
            Math.sqrt(sumSquares / (double) values.length - avg * avg) / 1000.0);
    }
}