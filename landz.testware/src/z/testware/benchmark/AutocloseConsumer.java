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

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Consumers that should be closed at shutdown (if not earlier).
 */
public abstract class AutocloseConsumer implements IResultsConsumer
{
    /**
     * A list of closeables to close at shutdown (if not closed earlier).
     */
    private static List<Closeable> autoclose = new ArrayList<Closeable>();

    /**
     * A shutdown agent closing {@link #autoclose}.
     */
    private static Thread shutdownAgent;

    protected AutocloseConsumer()
    {
        initShutdownAgent();
    }

    protected static synchronized void addAutoclose(Closeable c)
    {
        autoclose.add(c);
    }

    protected static synchronized void removeAutoclose(Closeable c)
    {
        try
        {
            while (autoclose.remove(c))
            {
                // repeat.
            }
            c.close();
        }
        catch (IOException e)
        {
            // Ignore.
        }
    }

    private static synchronized void initShutdownAgent()
    {
        if (shutdownAgent == null)
        {
            shutdownAgent = new Thread()
            {
                public void run()
                {
                    for (Closeable w : new ArrayList<Closeable>(autoclose))
                    {
                        try
                        {
                            w.close();
                        }
                        catch (IOException e)
                        {
                            // Ignore, not much to do.
                        }
                    }
                }
            };
            Runtime.getRuntime().addShutdownHook(shutdownAgent);
        }
    }
}