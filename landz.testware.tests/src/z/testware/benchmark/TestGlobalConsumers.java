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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Test global consumers and properties.
 */
public class TestGlobalConsumers
{
    private static final File resultsFile = new File("results.xml");

    @BeforeClass
    public static void checkFile() throws IOException
    {
        System.setProperty(BenchmarkOptionsSystemProperties.CONSUMERS_PROPERTY, 
              ConsumerName.CONSOLE + ", "
            + ConsumerName.XML);
        
        System.setProperty(BenchmarkOptionsSystemProperties.XML_FILE_PROPERTY, resultsFile.getAbsolutePath());

        // Close any previous globals.
        closeGlobals();
    }

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    @Test
    public void testMethodA()
    {
        // empty.
    }

    @Test
    public void testMethodB()
    {
        // empty.
    }

    @AfterClass
    public static void verify() throws Exception
    {
        assertEquals(2, closeGlobals());
        Common.existsAndDelete(resultsFile.getAbsolutePath());

        System.clearProperty(BenchmarkOptionsSystemProperties.CONSUMERS_PROPERTY);
        System.clearProperty(BenchmarkOptionsSystemProperties.XML_FILE_PROPERTY);
    }

    /*
     * 
     */
    private static int closeGlobals() throws IOException
    {
        if (BenchmarkOptionsSystemProperties.consumers == null)
            return 0;

        for (IResultsConsumer c : BenchmarkOptionsSystemProperties.consumers)
        {
            if (c instanceof Closeable)
                ((Closeable) c).close();
        }
        final int count = BenchmarkOptionsSystemProperties.consumers.length;
        BenchmarkOptionsSystemProperties.consumers = null;
        return count;
    }
}
