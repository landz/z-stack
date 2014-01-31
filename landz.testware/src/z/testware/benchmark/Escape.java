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

/**
 * Entity escaping.
 */
public final class Escape
{
    public Escape()
    {
        // no instances.
    }

    /*
     * NOTE. I leave these unimplemented for now; there shouldn't be any problems with
     * escaping for method and class names because these are compiler-verified. If there
     * are problems in the future, it'll be easier to escape such strings in one place (or
     * redirect to Apache Commons Lang, for example).
     */

    /**
     * Escape special HTML entities.
     */
    public static String htmlEscape(String string)
    {
        // TODO: implement me.
        return string;
    }

    /**
     * Escape a JSON string.
     */
    public static String jsonEscape(String string)
    {
        // TODO: implement me.
        return string;
    }

    /**
     * Escape an SQL string.
     */
    public static Object sqlEscape(String string)
    {
        // TODO: implement me.
        return string;
    }

    /**
     * Escape XML attribute's value.
     */
    public static String xmlAttrEscape(String string)
    {
        // TODO: implement me.
        return string;
    }
}
