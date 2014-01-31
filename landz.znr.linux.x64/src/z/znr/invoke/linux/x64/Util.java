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

import com.kenai.jffi.MemoryIO;
import com.kenai.jffi.PageManager;
import jnr.x86asm.Assembler;
import jnr.x86asm.CPU;
import z.znr.InlineAssembler;
import z.znr.invoke.types.NativeType;
import z.znr.invoke.types.ParameterType;
import z.znr.invoke.types.SignatureType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 */
public final class Util {
    public static boolean getBooleanProperty(String propertyName, boolean defaultValue) {
        try {
            return Boolean.valueOf(System.getProperty(propertyName, Boolean.valueOf(defaultValue).toString()));
        } catch (SecurityException se) {
            return defaultValue;
        }
    }

    public static int sizeof(NativeType nativeType) {
        return nativeType.size();
    }

    public static Class[] javaTypeArray(SignatureType[] types) {
        Class[] javaTypes = new Class[types.length];

        for (int i = 0; i < types.length; ++i) {
            javaTypes[i] = types[i].javaType();
        }

        return javaTypes;
    }

    public static MethodHandle getNotNullHandle() {
        return findStatic(AsmRuntime.class, "notNull", MethodType.methodType(boolean.class, Object.class));
    }

    public static MethodHandle getIsNullHandle() {
        return findStatic(AsmRuntime.class, "isNull", MethodType.methodType(boolean.class, Object.class));
    }

    public static MethodHandle findVirtual(Class klass, String methodName, MethodType methodType) {
        try {
            return Native.LOOKUP.findVirtual(klass, methodName, methodType);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle findStatic(Class klass, String methodName, MethodType methodType) {
        try {
            return Native.LOOKUP.findStatic(klass, methodName, methodType);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<ParameterType> asPrimitiveTypes(Collection<ParameterType> parameterTypes) {
        return Arrays.asList(convertParameterTypesToPrimitive(parameterTypes));
    }

    public static ParameterType[] asPrimitiveTypes(ParameterType[] parameterTypes) {
        return convertParameterTypesToPrimitive(Arrays.asList(parameterTypes));
    }

    private static ParameterType[] convertParameterTypesToPrimitive(Collection<ParameterType> parameterTypes) {
        ParameterType[] primitiveParameterTypes = new ParameterType[parameterTypes.size()];
        int i = 0;
        for (ParameterType p : parameterTypes) {
            primitiveParameterTypes[i++] = p.asPrimitiveType();
        }

        return primitiveParameterTypes;
    }


    public static MethodHandle getDirectCheckHandle(MethodHandle strategyLookup) {
        return MethodHandles.filterArguments(getStrategyIsDirectHandle(), 0, strategyLookup);
    }

    public static MethodHandle getDirectAddressHandle(MethodHandle strategyLookup) {
        MethodHandle addressHandle = getStrategyAddressHandle()
                .asType(MethodType.methodType(long.class, ObjectParameterStrategy.class, strategyLookup.type().parameterType(0)));
        return MethodHandles.foldArguments(addressHandle, strategyLookup);
    }

    private static MethodHandle getStrategyIsDirectHandle() {
        return findVirtual(com.kenai.jffi.ObjectParameterStrategy.class, "isDirect", MethodType.methodType(boolean.class))
                .asType(MethodType.methodType(boolean.class, ObjectParameterStrategy.class));
    }

    private static MethodHandle getStrategyAddressHandle() {
        return findVirtual(com.kenai.jffi.ObjectParameterStrategy.class, "address", MethodType.methodType(long.class, Object.class));
    }

    public static int countObjects(ParameterType... parameterTypes) {
        int objectCount = 0;
        for (ParameterType p : parameterTypes) {
            if (p.isObject()) {
                objectCount++;
            }
        }

        return objectCount;
    }

    public static CodeAddress inlineAssemblerToCodeAddress(InlineAssembler inlineAssembler) {
        Assembler asm = new Assembler(CPU.X86_64);
        inlineAssembler.assemble(asm);

        long page = PageManager.getInstance().allocatePages(1, PageManager.PROT_READ | PageManager.PROT_WRITE);
        ByteBuffer buf = ByteBuffer.allocate(asm.codeSize());
        asm.relocCode(buf, page);
        buf.flip();
        MemoryIO.getInstance().putByteArray(page, buf.array(), buf.arrayOffset(), buf.limit());

        buf.rewind();

//        if (X86Disassembler.isAvailable()) {
//            X86Disassembler dis = X86Disassembler.create();
//            dis.setInputBuffer(page, asm.codeSize());
//            dis.setMode(X86Disassembler.Mode.X86_64);
//
//            System.out.println("Dump of asm:");
//            while (dis.disassemble()) {
//                System.out.println("\t" + dis.insn());
//            }
//        }

        // Make the page executable (and non-writable, since some OS require that)
        PageManager.getInstance().protectPages(page, 1, PageManager.PROT_READ | PageManager.PROT_EXEC);
        return new CodeAddress(page);
    }
}
