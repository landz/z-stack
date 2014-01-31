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
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import z.znr.InlineAssembler;
import z.znr.invoke.types.*;

import java.io.OutputStreamWriter;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Collection;

import static jdk.internal.org.objectweb.asm.Opcodes.*;
import static z.znr.invoke.linux.x64.AsmUtil.emitDefaultConstructor;
import static z.znr.invoke.linux.x64.AsmUtil.emitStaticFieldInitialization;
import static z.znr.invoke.linux.x64.CodegenUtils.*;
import static z.znr.invoke.linux.x64.Native.STUB_NAME;
import static z.znr.invoke.linux.x64.Native.nextClassID;

final class PrimitiveX86MethodHandleGenerator implements MethodHandleGenerator {
    private static final boolean ENABLED = Util.getBooleanProperty("jnr.invoke.x86asm.enabled", true);
    private static final String PAGE_HOLDER_FIELD = "pageHolder";

    private final StubCompiler compiler = StubCompiler.newCompiler();

    public boolean isSupported(ResultType resultType, Collection<ParameterType> parameterTypes) {
        if (!ENABLED) {
            return false;
        }

        final Platform platform = Platform.getPlatform();

        if (platform.getOS().equals(Platform.OS.WINDOWS)) {
            return false;
        }

        if (!platform.getCPU().equals(Platform.CPU.I386) && !platform.getCPU().equals(Platform.CPU.X86_64)) {
            return false;
        }

        for (ParameterType parameterType : parameterTypes) {
            if (!isSupportedParameter(parameterType)) {
                return false;
            }
        }

        return isSupportedResult(resultType)
                && compiler.canCompile(resultType, parameterTypes.toArray(new ParameterType[parameterTypes.size()]));
    }

    @Override
    public MethodHandle createBoundHandle(Signature signature,  InlineAssembler inlineAssembler) {
        AsmClassLoader classLoader = new AsmClassLoader(Native.class.getClassLoader());
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = Native.DEBUG ? AsmUtil.newCheckClassAdapter(cw) : cw;

        AsmBuilder builder = new AsmBuilder(p(Native.class) + "$x86asm$" + nextClassID.getAndIncrement(), cv, classLoader);
        ResultType resultType = signature.getResultType().asPrimitiveType();

        cv.visit(V1_7, ACC_PUBLIC | ACC_FINAL, builder.getClassNamePath(), null, p(Object.class), new String[0]);

        compile(signature, builder, inlineAssembler, STUB_NAME, resultType, signature.parameterTypeArray());

        // Stash a strong ref to the library, so it doesn't get garbage collected.
        builder.getObjectField(Util.inlineAssemblerToCodeAddress(inlineAssembler).address());

        emitDefaultConstructor(cv);
        emitStaticFieldInitialization(builder, cv);

        cv.visitField(ACC_PUBLIC | ACC_STATIC | ACC_VOLATILE, PAGE_HOLDER_FIELD, ci(Object.class), null, null);
        cv.visitEnd();


        try {
            Class implClass = classLoader.defineClass(builder.getClassNamePath().replace("/", "."), cw.toByteArray(),
                    Native.DEBUG ? new OutputStreamWriter(System.err) : null);

            // Attach any native method stubs, and store a strong ref to the compiled page in a class var
            implClass.getField("pageHolder").set(implClass, compiler.attach(implClass));

            return MethodHandles.lookup().findStatic(implClass, STUB_NAME, signature.methodType());

        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    private void compile(Signature signature, AsmBuilder builder, InlineAssembler inlineAssembler, String stubName, ResultType resultType, ParameterType[] parameterTypes) {
        Class[] nativeParameterTypes = Util.javaTypeArray(parameterTypes);
        Class nativeReturnType = resultType.javaType();

        builder.getClassVisitor().visitMethod(ACC_PUBLIC | ACC_FINAL | ACC_NATIVE | ACC_STATIC,
                stubName, sig(nativeReturnType, nativeParameterTypes), null, null).visitEnd();

        compiler.compile(inlineAssembler, stubName, resultType, parameterTypes, nativeReturnType, nativeParameterTypes,
                signature.saveErrno());
    }

    private static boolean isSupportedType(SignatureType type) {
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
            case POINTER:
            case FLOAT:
            case DOUBLE:
                return type.javaType().isPrimitive();

            default:
                return false;
        }
    }


    static boolean isSupportedResult(ResultType resultType) {
        return isSupportedType(resultType)
                || (resultType.nativeType() == NativeType.VOID && void.class == resultType.javaType())
                ;
    }

    static boolean isSupportedParameter(ParameterType parameterType) {
        return isSupportedType(parameterType)
                ;
    }

}
