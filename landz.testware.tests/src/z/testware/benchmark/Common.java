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

import static org.junit.Assert.assertTrue;

/**
 * Common test utilities.
 */
public final class Common
{
    private Common()
    {
        // no instances.
    }

    /*
     * 
     */
    public static void existsAndDelete(String... fileNames)
    {
        for (String f : fileNames)
        {
            existsAndDelete(new File(f));
        }
    }

    /*
     * 
     */
    public static void existsAndDelete(File f)
    {
        assertTrue(f.exists());
        assertTrue(f.delete());
    }

    /*
     * 
     */
    public static String getAndDelete(File file) throws IOException 
    {
        assertTrue(file.exists());
        assertTrue(file.isFile() && file.canRead());

        InputStream is = new FileInputStream(file);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final byte [] buffer = new byte [1024];
        int cnt;
        while ((cnt = is.read(buffer)) > 0) {
            baos.write(buffer, 0, cnt);
        }
        is.close();
        baos.close();

        assertTrue(file.delete());
        
        return new String(baos.toByteArray(), "UTF-8");
    }

    /**
     * Delete recursively.
     */
    public static boolean deleteDir(File dir)
    {
        if (!dir.exists()) return true;

        File [] subfiles = dir.listFiles();
        if (subfiles.length > 0)
        {
            for (File f : subfiles)
            {
                if (f.isFile())
                    f.delete();
                else
                    deleteDir(f);
            }
        }
        return dir.delete();
    }
}
