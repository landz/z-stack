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

package z.testware.benchmark.annotation;

import java.lang.annotation.*;


/**
 * Generate a graphical summary of the historical and current run of a given
 * set of methods. 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface BenchmarkHistoryChart
{
    /**
     * Chart file prefix override. If empty, class name is used. 
     */
    String filePrefix() default "";

    /**
     * Maximum number of historical runs to take into account.
     */
    int maxRuns() default Integer.MAX_VALUE;

    /**
     * Use custom keys for X-axis label. If <code>false</code>, run ID is used.
     */
    LabelType labelWith() default LabelType.RUN_ID;
}