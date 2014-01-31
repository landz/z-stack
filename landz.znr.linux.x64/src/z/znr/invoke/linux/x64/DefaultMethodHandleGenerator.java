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

import com.kenai.jffi.*;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import z.znr.InlineAssembler;
import z.znr.invoke.types.ParameterType;
import z.znr.invoke.types.ResultType;
import z.znr.invoke.types.Signature;

import java.io.OutputStreamWriter;
import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import static jdk.internal.org.objectweb.asm.Opcodes.*;
import static z.znr.invoke.linux.x64.AsmUtil.*;
import static z.znr.invoke.linux.x64.CodegenUtils.*;

/**
 *
 */
final class DefaultMethodHandleGenerator implements MethodHandleGenerator {
    @Override
    public MethodHandle createBoundHandle(Signature signature, InlineAssembler inlineAssembler) {
        AsmClassLoader classLoader = new AsmClassLoader(Native.class.getClassLoader());

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = Native.DEBUG ? AsmUtil.newCheckClassAdapter(cw) : cw;

        AsmBuilder builder = new AsmBuilder(p(Native.class) + "$jnr$ffi$" + Native.nextClassID.getAndIncrement(), cv, classLoader);

        cv.visit(V1_7, ACC_PUBLIC | ACC_FINAL, builder.getClassNamePath(), null, p(Object.class), new String[0]);
        ResultType resultType = signature.getResultType().asPrimitiveType();

        generate(builder, Native.STUB_NAME, signature.getNativeCallContext(), inlineAssembler, resultType, signature.parameterTypeArray());

        emitDefaultConstructor(cv);
        emitStaticFieldInitialization(builder, cv);

        cv.visitEnd();


        try {
            Class implClass = classLoader.defineClass(builder.getClassNamePath().replace("/", "."), cw.toByteArray(),
                    Native.DEBUG ? new OutputStreamWriter(System.err) : null);

            return Native.LOOKUP.findStatic(implClass, Native.STUB_NAME, signature.methodType());

        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean isSupported(ResultType resultType, Collection<ParameterType> parameterTypes) {
        for (ParameterType parameterType : parameterTypes) {
            if (!parameterType.javaType().isPrimitive() && !parameterType.isObject()) {
                return false;
            }
        }

        return resultType.javaType().isPrimitive();
    }

    private static void generate(AsmBuilder builder, String functionName, CallContext callContext, InlineAssembler inlineAssembler,
                         ResultType resultType, ParameterType[] parameterTypes) {

        SkinnyMethodAdapter mv = new SkinnyMethodAdapter(builder.getClassVisitor(), ACC_PUBLIC | ACC_FINAL | ACC_STATIC,
                functionName, sig(resultType.javaType(), Util.javaTypeArray(parameterTypes)), null, null);
        mv.start();

        // Retrieve the jffi Invoker instance
        mv.getstatic(builder.getClassNamePath(), builder.getObjectFieldName(Invoker.getInstance(), com.kenai.jffi.Invoker.class), ci(com.kenai.jffi.Invoker.class));

        // retrieve the call context and function address
        mv.getstatic(builder.getClassNamePath(), builder.getObjectFieldName(callContext), ci(CallContext.class));
        long nativeAddress = Util.inlineAssemblerToCodeAddress(inlineAssembler).address();
        mv.ldc(nativeAddress);
        // Stash a strong ref to the library, so it doesn't get garbage collected.
        builder.getObjectField(nativeAddress);

        LocalVariableAllocator localVariableAllocator = new LocalVariableAllocator(parameterTypes);

        // [ stack contains: Invoker, Function ]
        // Create a new InvocationBuffer
        mv.getstatic(builder.getClassNamePath(), builder.getObjectFieldName(callContext), ci(CallContext.class));
        mv.invokestatic(AsmRuntime.class, "newHeapInvocationBuffer", HeapInvocationBuffer.class, CallContext.class);
        // [ stack contains: Invoker, Function, HeapInvocationBuffer ]

        final LocalVariable[] parameters = AsmUtil.getParameterVariables(parameterTypes, true);

        for (int i = 0; i < parameterTypes.length; ++i) {
            MarshalOp marshalOp = getMarshalOp(parameterTypes[i].nativeType());

            mv.dup(); // HeapInvocationBuffer
            load(mv, parameterTypes[i].javaType(), parameters[i]);

            if (parameterTypes[i].getObjectStrategyHandle() != null) {

                mv.getstatic(builder.getClassNamePath(), builder.getObjectFieldName(parameterTypes[i].getObjectStrategyHandle()), ci(MethodHandle.class));
                load(mv, parameterTypes[i].javaType(), parameters[i]);
                mv.invokevirtual(MethodHandle.class, "invokeExact", ObjectParameterStrategy.class, parameterTypes[i].javaType());
                mv.getstatic(builder.getClassNamePath(),
                        builder.getObjectFieldName(ObjectParameterInfo.create(i, parameterTypes[i].getDataDirection().getArrayFlags())),
                        ci(ObjectParameterInfo.class));
                mv.invokevirtual(HeapInvocationBuffer.class, "putObject", void.class, Object.class, com.kenai.jffi.ObjectParameterStrategy.class, ObjectParameterInfo.class);

            } else {

                NumberUtil.convertPrimitive(mv, parameterTypes[i].javaType(), marshalOp.getPrimitiveClass(), parameterTypes[i].nativeType());
                mv.invokevirtual(HeapInvocationBuffer.class, marshalOp.getMethodName(), void.class, marshalOp.getPrimitiveClass());
            }
        }

        InvokeOp iop = getInvokeOp(resultType);

        mv.invokevirtual(Invoker.class, iop.getMethodName(), iop.getPrimitiveClass(), CallContext.class, long.class, HeapInvocationBuffer.class);

        // narrow/widen the return value if needed
        NumberUtil.convertPrimitive(mv, iop.getPrimitiveClass(), resultType.javaType(), resultType.nativeType());
        emitReturnOp(mv, resultType.javaType());

        mv.visitMaxs(100, localVariableAllocator.getSpaceUsed());
        mv.visitEnd();
    }

    private static InvokeOp getInvokeOp(ResultType resultType) {
        InvokeOp iop = invokeOps.get(resultType.nativeType());
        if (iop == null) {
            throw new IllegalArgumentException("unsupported return type " + resultType.javaType());
        }
        return iop;
    }

    private static MarshalOp getMarshalOp(z.znr.invoke.types.NativeType nativeType) {
        MarshalOp marshalOp = marshalOps.get(nativeType);
        if (marshalOp == null) {
            throw new IllegalArgumentException("unsupported parameter type " + nativeType);
        }

        return marshalOp;
    }

    private static abstract class Operation {
        private final String methodName;
        private final Class primitiveClass;

        private Operation(String methodName, Class primitiveClass) {
            this.methodName = methodName;
            this.primitiveClass = primitiveClass;
        }

        public String getMethodName() {
            return methodName;
        }

        public Class getPrimitiveClass() {
            return primitiveClass;
        }
    }

    private static final class MarshalOp extends Operation {
        private MarshalOp(String methodName, Class primitiveClass) {
            super("put" + methodName, primitiveClass);
        }
    }

    private static final class InvokeOp extends Operation {
        private InvokeOp(String methodName, Class primitiveClass) {
            super("invoke" + methodName, primitiveClass);
        }
    }

    private static final Map<z.znr.invoke.types.NativeType, MarshalOp> marshalOps;
    private static final Map<z.znr.invoke.types.NativeType, InvokeOp> invokeOps;
    static {
        Map<z.znr.invoke.types.NativeType, MarshalOp> mops = new EnumMap<z.znr.invoke.types.NativeType, MarshalOp>(z.znr.invoke.types.NativeType.class);
        Map<z.znr.invoke.types.NativeType, InvokeOp> iops = new EnumMap<z.znr.invoke.types.NativeType, InvokeOp>(z.znr.invoke.types.NativeType.class);
        mops.put(z.znr.invoke.types.NativeType.SCHAR, new MarshalOp("Byte", int.class));
        mops.put(z.znr.invoke.types.NativeType.UCHAR, new MarshalOp("Byte", int.class));
        mops.put(z.znr.invoke.types.NativeType.SSHORT, new MarshalOp("Short", int.class));
        mops.put(z.znr.invoke.types.NativeType.USHORT, new MarshalOp("Short", int.class));
        mops.put(z.znr.invoke.types.NativeType.SINT, new MarshalOp("Int", int.class));
        mops.put(z.znr.invoke.types.NativeType.UINT, new MarshalOp("Int", int.class));
        mops.put(z.znr.invoke.types.NativeType.SLONG_LONG, new MarshalOp("Long", long.class));
        mops.put(z.znr.invoke.types.NativeType.ULONG_LONG, new MarshalOp("Long", long.class));
        mops.put(z.znr.invoke.types.NativeType.FLOAT, new MarshalOp("Float", float.class));
        mops.put(z.znr.invoke.types.NativeType.DOUBLE, new MarshalOp("Double", double.class));
        mops.put(z.znr.invoke.types.NativeType.POINTER, new MarshalOp("Address", long.class));
        if (Util.sizeof(z.znr.invoke.types.NativeType.SLONG) == 4) {
            mops.put(z.znr.invoke.types.NativeType.SLONG, new MarshalOp("Int", int.class));
            mops.put(z.znr.invoke.types.NativeType.ULONG, new MarshalOp("Int", int.class));
        } else {
            mops.put(z.znr.invoke.types.NativeType.SLONG, new MarshalOp("Long", long.class));
            mops.put(z.znr.invoke.types.NativeType.ULONG, new MarshalOp("Long", long.class));
        }

        iops.put(z.znr.invoke.types.NativeType.SCHAR, new InvokeOp("Int", int.class));
        iops.put(z.znr.invoke.types.NativeType.UCHAR, new InvokeOp("Int", int.class));
        iops.put(z.znr.invoke.types.NativeType.SSHORT, new InvokeOp("Int", int.class));
        iops.put(z.znr.invoke.types.NativeType.USHORT, new InvokeOp("Int", int.class));
        iops.put(z.znr.invoke.types.NativeType.SINT, new InvokeOp("Int", int.class));
        iops.put(z.znr.invoke.types.NativeType.UINT, new InvokeOp("Int", int.class));
        iops.put(z.znr.invoke.types.NativeType.VOID, new InvokeOp("Int", int.class));
        iops.put(z.znr.invoke.types.NativeType.SLONG_LONG, new InvokeOp("Long", long.class));
        iops.put(z.znr.invoke.types.NativeType.ULONG_LONG, new InvokeOp("Long", long.class));
        iops.put(z.znr.invoke.types.NativeType.FLOAT, new InvokeOp("Float", float.class));
        iops.put(z.znr.invoke.types.NativeType.DOUBLE, new InvokeOp("Double", double.class));
        iops.put(z.znr.invoke.types.NativeType.POINTER, new InvokeOp("Address", long.class));
        if (Util.sizeof(z.znr.invoke.types.NativeType.SLONG) == 4) {
            iops.put(z.znr.invoke.types.NativeType.SLONG, new InvokeOp("Int", int.class));
            iops.put(z.znr.invoke.types.NativeType.ULONG, new InvokeOp("Int", int.class));
        } else {
            iops.put(z.znr.invoke.types.NativeType.SLONG, new InvokeOp("Long", long.class));
            iops.put(z.znr.invoke.types.NativeType.ULONG, new InvokeOp("Long", long.class));
        }
        marshalOps = Collections.unmodifiableMap(mops);
        invokeOps = Collections.unmodifiableMap(iops);
    }

}
