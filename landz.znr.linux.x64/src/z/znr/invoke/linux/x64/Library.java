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

public final class Library {
    public static final int LAZY = com.kenai.jffi.Library.LAZY;
    public static final int NOW = com.kenai.jffi.Library.NOW;
    public static final int LOCAL = com.kenai.jffi.Library.LOCAL;
    public static final int GLOBAL = com.kenai.jffi.Library.GLOBAL;

    private final com.kenai.jffi.Library jffiLibrary;

    public static Library open(String name, int flags) {
        com.kenai.jffi.Library jffiLibrary = com.kenai.jffi.Library.getCachedInstance(name, flags);
        if (jffiLibrary != null) {
            return new Library(jffiLibrary);
        }

        throw new UnsatisfiedLinkError(com.kenai.jffi.Library.getLastError());
    }

    private Library(com.kenai.jffi.Library jffiLibrary) {
        this.jffiLibrary = jffiLibrary;
    }

    public final z.znr.invoke.linux.x64.CodeAddress getFunction(String name) {
        long address = jffiLibrary.getSymbolAddress(name);
        if (address != 0L) {
            return new CodeAddress(this, address);
        }

        throw new UnsatisfiedLinkError("no such function: " + name);
    }


    private static final class CodeAddress extends z.znr.invoke.linux.x64.CodeAddress {
        private final Library library;

        CodeAddress(Library library, long address) {
            super(address);
            this.library = library;
        }
    }
}
