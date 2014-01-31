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

package z.znr.invoke.linux.x64;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NativeLibrary {
    private static final List<String> libraryNames = new ArrayList<String>();
    private static final List<String> searchPaths = Collections.unmodifiableList(getPropertyPaths("java.library.path"));
    
    private static final List<com.kenai.jffi.Library> nativeLibraries = new ArrayList<com.kenai.jffi.Library>();

    private NativeLibrary() {}

    public static void loadLibrary(String libraryName) {
        com.kenai.jffi.Library lib;

        lib = openLibrary(libraryName);
        if (lib == null) {
            String path;
            if (libraryName != null && (path = locateLibrary(libraryName)) != null && !libraryName.equals(path)) {
                lib = openLibrary(path);
            }
        }

        if (lib == null) {
            throw new UnsatisfiedLinkError(com.kenai.jffi.Library.getLastError());
        }

        nativeLibraries.add(lib);
        libraryNames.add(libraryName);
    }

    private static final List<String> getPropertyPaths(String propName) {
        String value = System.getProperty(propName);
        if (value != null) {
            String[] paths = value.split(File.pathSeparator);
            return new ArrayList<String>(Arrays.asList(paths));
        }
        return Collections.emptyList();
    }


    private static final String locateLibrary(String libraryName) {
        if (new File(libraryName).isAbsolute()) {
            return libraryName;
        }

        return locateLibrary(libraryName, searchPaths);
    }

    private static final String locateLibrary(final String libName, List<String> libraryPath) {
        FilenameFilter filter = new FilenameFilter() {
            Pattern p = Pattern.compile("lib" + libName + "\\.so\\.[0-9]+$");
            String exact = "lib" + libName + ".so";
            public boolean accept(File dir, String name) {
                return p.matcher(name).matches() || exact.equals(name);
            }
        };

        List<File> matches = new LinkedList<File>();
        for (String path : libraryPath) {
            File[] files = new File(path).listFiles(filter);
            if (files != null && files.length > 0) {
                matches.addAll(Arrays.asList(files));
            }
        }

        //
        // Search through the results and return the highest numbered version
        // i.e. libc.so.6 is preferred over libc.so.5
        //
        int version = 0;
        String bestMatch = null;
        for (File file : matches) {
            String path = file.getAbsolutePath();
            if (bestMatch == null && path.endsWith(".so")) {
                bestMatch = path;
                version = 0;
            } else {
                String num = path.substring(path.lastIndexOf(".so.") + 4);
                try {
                    if (Integer.parseInt(num) >= version) {
                        bestMatch = path;
                    }
                } catch (NumberFormatException e) {
                } // Just skip if not a number
            }
        }
        return bestMatch != null ? bestMatch : mapLibraryName(libName);
    }

    private static final  String mapLibraryName(String libName) {
        // Older JDK on linux map 'c' to 'libc.so' which doesn't work
        return "c".equals(libName) || "libc.so".equals(libName)
                ? "libc.so.6" : System.mapLibraryName(libName);
    }

    public static final long getSymbolAddress(String name) {
        for (com.kenai.jffi.Library l : nativeLibraries) {
            long address = l.getSymbolAddress(name);
            if (address != 0) {
                return address;
            }
        }
        return 0;
    }

    public static final long findSymbolAddress(String name) {
        long address = getSymbolAddress(name);
        if (address == 0) {
            throw new UnsatisfiedLinkError(com.kenai.jffi.Library.getLastError());
        }
        return address;
    }

    private static final Pattern BAD_ELF = Pattern.compile("(.*): invalid ELF header");
    private static final Pattern ELF_GROUP = Pattern.compile("GROUP\\s*\\(\\s*(\\S*).*\\)");

    private static final com.kenai.jffi.Library openLibrary(String path) {
        com.kenai.jffi.Library lib;

        lib = com.kenai.jffi.Library.getCachedInstance(path, com.kenai.jffi.Library.LAZY | com.kenai.jffi.Library.GLOBAL);
        if (lib != null) {
            return lib;
        }
        
        // If dlopen() fails with 'invalid ELF header', then it is likely to be a ld script - parse it for the real library path
        Matcher badElf = BAD_ELF.matcher(com.kenai.jffi.Library.getLastError());
        if (badElf.lookingAt()) {
            File f = new File(badElf.group(1));
            if (f.isFile() && f.length() < (4 * 1024)) {
                Matcher sharedObject = ELF_GROUP.matcher(readAll(f));
                if (sharedObject.lookingAt()) {
                    return com.kenai.jffi.Library.getCachedInstance(sharedObject.group(1), com.kenai.jffi.Library.LAZY | com.kenai.jffi.Library.GLOBAL);
                }
            }
        }
        
        return null;
    }
    
    private static final String readAll(File f) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);

        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        
        } finally {
            if (br != null) try { br.close(); } catch (IOException e) { throw new RuntimeException(e); }
        }
    }
}
