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

package z.testware.benchmark.examples;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

// [[[start:lists-initial]]] 
public class Lists1
{
    private static Object singleton = new Object();
    private static int COUNT = 50000;
    private static int [] rnd;

    /** Prepare random numbers for tests. */
    @BeforeClass
    public static void prepare()
    {
        rnd = new int [COUNT];

        final Random random = new Random();
        for (int i = 0; i < COUNT; i++)
        {
            rnd[i] = Math.abs(random.nextInt());
        }
    }

    @Test
    public void arrayList() throws Exception
    {
        runTest(new ArrayList<Object>());
    }

    @Test
    public void linkedList() throws Exception
    {
        runTest(new LinkedList<Object>());
    }

    @Test
    public void vector() throws Exception
    {
        runTest(new Vector<Object>());
    }

    private void runTest(List<Object> list)
    {
        assert list.isEmpty();
        
        // First, add a number of objects to the list.
        for (int i = 0; i < COUNT; i++)
            list.add(singleton);
        
        // Randomly delete objects from the list.
        for (int i = 0; i < rnd.length; i++)
            list.remove(rnd[i] % list.size());
    }
}
// [[[end:lists-initial]]] 