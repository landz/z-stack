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
import z.znr.invoke.types.ParameterType;
import z.znr.invoke.types.ResultType;
import z.znr.invoke.types.Signature;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collection;
import java.util.List;

import static z.znr.invoke.linux.x64.CodegenUtils.params;

public class DirectCheckMethodHandleGenerator implements MethodHandleGenerator {
    private final MethodHandleGenerator[] primitiveGenerators = {
            new PrimitiveX86MethodHandleGenerator(),
            new PrimitiveNumericMethodHandleGenerator(),
    };

    @Override
    public MethodHandle createBoundHandle(Signature signature, InlineAssembler inlineAssembler) {
        return MethodHandles.guardWithTest(createDirectCheckHandle(signature.parameterTypeArray()),
                getPrimitiveHandle(signature, inlineAssembler),
                new DefaultMethodHandleGenerator().createBoundHandle(signature, inlineAssembler));
    }


    @Override
    public boolean isSupported(ResultType resultType, Collection<ParameterType> parameterTypes) {
        boolean isSupported = false;
        List<ParameterType> primitiveParameterTypes = Util.asPrimitiveTypes(parameterTypes);
        for (MethodHandleGenerator g : primitiveGenerators) {
            isSupported |= g.isSupported(resultType, primitiveParameterTypes);
        }

        return isSupported && parameterTypes.size() <= 6;
    }

    private MethodHandle getPrimitiveHandle(Signature signature, InlineAssembler inlineAssembler) {
        Signature primitiveContext = signature.asPrimitiveContext();

        MethodHandle primitiveHandle = createPrimitiveMethodHandle(primitiveGenerators, primitiveContext, inlineAssembler);
        for (int i = 0; i < signature.getParameterCount(); i++) {
            if (signature.getParameterType(i).getDirectAddressHandle() != null) {
                primitiveHandle = MethodHandles.filterArguments(primitiveHandle, i, signature.getParameterType(i).getDirectAddressHandle());
            }
        }

        return primitiveHandle;
    }

    private static MethodHandle createPrimitiveMethodHandle(MethodHandleGenerator[] generators, Signature signature, InlineAssembler inlineAssembler) {
        for (MethodHandleGenerator g : generators) {
            if (g.isSupported(signature.getResultType(), signature.parameterTypeList())) {
                return g.createBoundHandle(signature, inlineAssembler);
            }
        }

        throw new RuntimeException("internal error");
    }


    private static MethodHandle createDirectCheckHandle(ParameterType[] parameterTypes) {
        MethodHandle isTrue = Util.findStatic(AsmRuntime.class, "isTrue", MethodType.methodType(boolean.class, params(boolean.class, Util.countObjects(parameterTypes))));
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i].getDirectCheckHandle() == null) {
                isTrue = MethodHandles.dropArguments(isTrue, i, parameterTypes[i].javaType());
            } else {
                isTrue = MethodHandles.filterArguments(isTrue, i, parameterTypes[i].getDirectCheckHandle());
            }
        }

        return isTrue;
    }
}
