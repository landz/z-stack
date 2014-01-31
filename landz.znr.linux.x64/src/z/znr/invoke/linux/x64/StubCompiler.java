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

import com.kenai.jffi.Internals;
import com.kenai.jffi.PageManager;
import com.kenai.jffi.Platform;
import z.znr.InlineAssembler;
import z.znr.invoke.types.ParameterType;
import z.znr.invoke.types.ResultType;

/**
 * Compiles asm trampoline stubs for java class methods
 */
abstract class StubCompiler {
    // If the version of jffi exports the jffi_save_errno function address,
    // then it is recent enough to support PageManager and NativeMethods as well.
    static final long errnoFunctionAddress = getErrnoSaveFunction();
    static final boolean hasPageManager = hasPageManager();

    public static StubCompiler newCompiler() {
        if (errnoFunctionAddress != 0 && hasPageManager) {
            switch (Platform.getPlatform().getCPU()) {
                case X86_64:
                    if (Platform.getPlatform().getOS() != Platform.OS.WINDOWS) {
                        return new X64StubCompiler();
                    }
                    break;
            }
        }

        return new DummyStubCompiler();
    }

    abstract boolean canCompile(ResultType returnType, ParameterType[] parameterTypes);

    abstract void compile(InlineAssembler inlineAssembler, String name, ResultType returnType, ParameterType[] parameterTypes,
                          Class resultClass, Class[] parameterClasses, boolean saveErrno);

    abstract Object attach(Class clazz);

    static final class DummyStubCompiler extends StubCompiler {

        boolean canCompile(ResultType returnType, ParameterType[] parameterTypes) {
            return false;
        }

        @Override
        void compile(InlineAssembler inlineAssembler, String name, ResultType returnType, ParameterType[] parameterTypes, Class resultClass, Class[] parameterClasses, boolean saveErrno) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        Object attach(Class clazz) {
            return new Object();
        }

    }

    private static long getErrnoSaveFunction() {
        try {
            return Internals.getErrnoSaveFunction();
            
        } catch (Throwable t) {
            return 0;
        }
    }

    private static boolean hasPageManager() {
        try {
            // Just try and allocate/free a page to check the PageManager is working
            long page = PageManager.getInstance().allocatePages(1, PageManager.PROT_READ | PageManager.PROT_WRITE);
            PageManager.getInstance().freePages(page, 1);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

}
