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

import z.znr.InlineAssembler;
import z.znr.invoke.types.Signature;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.atomic.AtomicLong;

public final class Native {

    static final boolean DEBUG = Boolean.getBoolean("jnr.invoke.compile.dump");
    static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    static final AtomicLong nextClassID = new AtomicLong(0);
    static final String STUB_NAME = "invokeNative";

    private Native() {
    }

    public static MethodHandle getMethodHandle(Signature signature, InlineAssembler inlineAssembler) {

        MethodHandle mh = getPrimitiveMethodHandle(signature, inlineAssembler);
        if (mh == null) {
            throw new UnsupportedOperationException("cannot generate handle for " + signature);
        }

        return mh;
    }

    private static MethodHandle getPrimitiveMethodHandle(Signature signature, InlineAssembler inlineAssembler) {
        MethodHandleGenerator[] generators = {
                new PrimitiveX86MethodHandleGenerator(),
                new PrimitiveNumericMethodHandleGenerator(),
                new DirectCheckMethodHandleGenerator(),
                new DefaultMethodHandleGenerator()
        };

        for (MethodHandleGenerator g : generators) {
            if (g.isSupported(signature.getResultType(), signature.parameterTypeList())) {
                return g.createBoundHandle(signature, inlineAssembler);
            }
        }

        return null;
    }
}
