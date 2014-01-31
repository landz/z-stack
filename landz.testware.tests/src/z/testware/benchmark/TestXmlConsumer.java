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

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Creates a benchmark with two measured methods. One of the test methods has an
 * overridden number of warmup and benchmark rounds.
 */
public class TestXmlConsumer
{
    private static final File resultsFile = new File("results.xml");
    private static XMLConsumer xmlConsumer;

    @BeforeClass
    public static void checkFile() throws IOException
    {
        if (resultsFile.exists())
            assertTrue(resultsFile.delete());

        xmlConsumer = new XMLConsumer(resultsFile);
    }

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule(xmlConsumer);

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
        xmlConsumer.close();

        assertTrue(resultsFile.exists());
        /*
        final Document d = new org.dom4j.io.SAXReader().read(resultsFile);
        assertEquals(2, d.selectNodes("//testname").size());
        assertTrue(resultsFile.delete());
        */
    }
}
