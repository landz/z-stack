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

import com.kenai.jffi.Platform;
import z.znr.InlineAssembler;
import z.znr.invoke.types.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.Collection;

final class PrimitiveNumericMethodHandleGenerator implements MethodHandleGenerator {
    private static final boolean ENABLED = Util.getBooleanProperty("jnr.invoke.fast-numeric.enabled", true);
    private static final int MAX_PARAMETERS = getMaximumParameters('N', long.class);

    PrimitiveNumericMethodHandleGenerator() {
    }

    public MethodHandle createBoundHandle(Signature signature, InlineAssembler inlineAssembler) {
        try {
            MethodHandle mh = MethodHandles.filterArguments(createUnBoundHandle(signature), 0,
                    Native.LOOKUP.findVirtual(CodeAddress.class, "address", MethodType.methodType(long.class)));
            mh = MethodHandles.insertArguments(mh, 0, Util.inlineAssemblerToCodeAddress(inlineAssembler).address());
            return MethodHandles.explicitCastArguments(mh, signature.methodType());

        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public MethodHandle createUnBoundHandle(Signature signature) {
        MethodHandle mh = MethodHandles.insertArguments(getFastNumericHandle(signature), 0, signature.getNativeCallContext());
        Class nativeIntType = long.class;
        for (int i = 0; i < signature.getParameterCount(); i++) {
            ParameterType parameterType = signature.getParameterType(i);
            MethodHandle conversion = NumberUtil.getParameterConversionHandle(parameterType.nativeType(), parameterType.javaType(), nativeIntType);
            if (conversion != null) {
                mh = MethodHandles.filterArguments(mh, i + 1, conversion);
            }
        }

        MethodHandle conversion = NumberUtil.getResultConversionHandle(signature.getResultType().nativeType(), nativeIntType, signature.getResultType().javaType());
        if (conversion != null) {
            mh = MethodHandles.filterReturnValue(mh, conversion);
        }

        return mh;
    }

    public boolean isSupported(ResultType resultType, Collection<ParameterType> parameterTypes) {

        if (!ENABLED) {
            return false;
        }

        final Platform platform = Platform.getPlatform();

        // Only supported on i386 and amd64 arches
        if (platform.getCPU() != Platform.CPU.X86_64) {
            return false;
        }

        if (platform.getOS().equals(Platform.OS.WINDOWS)) {
            return false;
        }

        for (ParameterType parameterType : parameterTypes) {
            if (!isFastNumericParameter(parameterType)) {
                return false;
            }
        }

        return isFastNumericResult(resultType);
    }

    private static MethodHandle getFastNumericHandle(Signature signature) {
        return getPrimitiveInvokerHandle(signature, long.class, 'N');
    }

    private static MethodHandle getPrimitiveInvokerHandle(Signature signature, Class nativeIntType, char suffix) {
        MethodType mt = MethodType.methodType(long.class, getInvokerParameterClasses(signature.getParameterCount(), nativeIntType));
        try {
            return Native.LOOKUP.findVirtual(com.kenai.jffi.Invoker.class, "invoke" + suffix + signature.getParameterCount(), mt)
                    .bindTo(com.kenai.jffi.Invoker.getInstance());
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isNumericType(SignatureType type) {
        switch (type.nativeType()) {
            case SCHAR:
            case UCHAR:
            case SSHORT:
            case USHORT:
            case SINT:
            case UINT:
            case SLONG:
            case ULONG:
            case SLONG_LONG:
            case ULONG_LONG:
            case FLOAT:
            case DOUBLE:
            case POINTER:
                return type.javaType().isPrimitive();

            default:
                return false;
        }
    }

    static boolean isFastNumericResult(ResultType type) {
        return isNumericType(type)
                || (type.nativeType() == NativeType.VOID && void.class == type.javaType())
                ;
    }

    static boolean isFastNumericParameter(ParameterType parameterType) {
        return isNumericType(parameterType);
    }

    private static Class[] getInvokerParameterClasses(int parameterCount, Class nativeIntType) {
        Class[] ptypes = new Class[parameterCount + 2];
        ptypes[0] = com.kenai.jffi.CallContext.class;
        ptypes[1] = long.class;
        Arrays.fill(ptypes, 2, ptypes.length, nativeIntType);

        return ptypes;
    }


    static int getMaximumParameters(char suffix, Class nativeIntType) {
        try {
            com.kenai.jffi.Invoker.class.getDeclaredMethod("invoke" + suffix + "6", getInvokerParameterClasses(6, nativeIntType));
            return 6;
        } catch (Throwable t) {
            return -1;
        }
    }
}
