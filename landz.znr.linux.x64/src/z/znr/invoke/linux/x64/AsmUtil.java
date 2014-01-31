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

import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import z.znr.invoke.types.ParameterType;
import z.znr.invoke.types.SignatureType;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.Map;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

final class AsmUtil {
    private AsmUtil() {}
    
    public static MethodVisitor newTraceMethodVisitor(MethodVisitor mv) {
        try {
            Class<? extends MethodVisitor> tmvClass = Class.forName("jdk.internal.org.objectweb.asm.util.TraceMethodVisitor").asSubclass(MethodVisitor.class);
            Constructor<? extends MethodVisitor> c = tmvClass.getDeclaredConstructor(MethodVisitor.class);
            return c.newInstance(mv);
        } catch (Throwable t) {
            return mv;
        }
    }

    public static ClassVisitor newTraceClassVisitor(ClassVisitor cv, OutputStream out) {
        return newTraceClassVisitor(cv, new PrintWriter(out, true));
    }

    public static ClassVisitor newTraceClassVisitor(ClassVisitor cv, PrintWriter out) {
        try {

            Class<? extends ClassVisitor> tmvClass = Class.forName("jdk.internal.org.objectweb.asm.util.TraceClassVisitor").asSubclass(ClassVisitor.class);
            Constructor<? extends ClassVisitor> c = tmvClass.getDeclaredConstructor(ClassVisitor.class, PrintWriter.class);
            return c.newInstance(cv, out);
        } catch (Throwable t) {
            return cv;
        }
    }

    public static ClassVisitor newTraceClassVisitor(PrintWriter out) {
        try {

            Class<? extends ClassVisitor> tmvClass = Class.forName("jdk.internal.org.objectweb.asm.util.TraceClassVisitor").asSubclass(ClassVisitor.class);
            Constructor<? extends ClassVisitor> c = tmvClass.getDeclaredConstructor(PrintWriter.class);
            return c.newInstance(out);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static ClassVisitor newCheckClassAdapter(ClassVisitor cv) {
        try {
            Class<? extends ClassVisitor> tmvClass = Class.forName("jdk.internal.org.objectweb.asm.util.CheckClassAdapter").asSubclass(ClassVisitor.class);
            Constructor<? extends ClassVisitor> c = tmvClass.getDeclaredConstructor(ClassVisitor.class);
            return c.newInstance(cv);
        } catch (Throwable t) {
            return cv;
        }
    }

    static void emitReturnOp(SkinnyMethodAdapter mv, Class returnType) {
        if (!returnType.isPrimitive()) {
            mv.areturn();
        } else if (long.class == returnType) {
            mv.lreturn();
        } else if (float.class == returnType) {
            mv.freturn();
        } else if (double.class == returnType) {
            mv.dreturn();
        } else if (void.class == returnType) {
            mv.voidreturn();
        } else {
            mv.ireturn();
        }
    }

    /**
     * Calculates the size of a local variable
     *
     * @param type The type of parameter
     * @return The size in parameter units
     */
    static int calculateLocalVariableSpace(Class type) {
        return long.class == type || double.class == type ? 2 : 1;
    }

    /**
     * Calculates the size of a local variable
     *
     * @param type The type of parameter
     * @return The size in parameter units
     */
    static int calculateLocalVariableSpace(SignatureType type) {
        return calculateLocalVariableSpace(type.javaType());
    }

    /**
     * Calculates the size of a list of types in the local variable area.
     *
     * @param types The type of parameter
     * @return The size in parameter units
     */
    static int calculateLocalVariableSpace(Class... types) {
        int size = 0;

        for (int i = 0; i < types.length; ++i) {
            size += calculateLocalVariableSpace(types[i]);
        }

        return size;
    }

    /**
     * Calculates the size of a list of types in the local variable area.
     *
     * @param types The type of parameter
     * @return The size in parameter units
     */
    static int calculateLocalVariableSpace(SignatureType... types) {
        int size = 0;

        for (SignatureType type : types) {
            size += calculateLocalVariableSpace(type);
        }

        return size;
    }

    static LocalVariable[] getParameterVariables(ParameterType[] parameterTypes, boolean isStatic) {
        LocalVariable[] lvars = new LocalVariable[parameterTypes.length];
        int lvar = isStatic ? 0 : 1;
        for (int i = 0; i < parameterTypes.length; i++) {
            lvars[i] = new LocalVariable(parameterTypes[i].javaType(), lvar);
            lvar += calculateLocalVariableSpace(parameterTypes[i]);
        }

        return lvars;
    }

    static void load(SkinnyMethodAdapter mv, Class parameterType, LocalVariable parameter) {
        if (!parameterType.isPrimitive()) {
            mv.aload(parameter);

        } else if (long.class == parameterType) {
            mv.lload(parameter);

        } else if (float.class == parameterType) {
            mv.fload(parameter);

        } else if (double.class == parameterType) {
            mv.dload(parameter);

        } else {
            mv.iload(parameter);
        }

    }


    static void store(SkinnyMethodAdapter mv, Class type, LocalVariable var) {
        if (!type.isPrimitive()) {
            mv.astore(var);

        } else if (long.class == type) {
            mv.lstore(var);

        } else if (double.class == type) {
            mv.dstore(var);

        } else if (float.class == type) {
            mv.fstore(var);

        } else {
            mv.istore(var);
        }
    }

    static void tryfinally(SkinnyMethodAdapter mv, Runnable codeBlock, Runnable finallyBlock) {
        Label before = new Label(), after = new Label(), ensure = new Label(), done = new Label();
        mv.trycatch(before, after, ensure, null);
        mv.label(before);
        codeBlock.run();
        mv.label(after);
        if (finallyBlock != null) finallyBlock.run();
        mv.go_to(done);
        if (finallyBlock != null) {
            mv.label(ensure);
            finallyBlock.run();
            mv.athrow();
        }
        mv.label(done);
    }

    static void emitDefaultConstructor(ClassVisitor cv) {
        SkinnyMethodAdapter init = new SkinnyMethodAdapter(cv, ACC_PUBLIC, "<init>", CodegenUtils.sig(void.class), null, null);
        init.start();
        init.aload(0);
        init.invokespecial(CodegenUtils.p(Object.class), "<init>", CodegenUtils.sig(void.class));
        init.voidreturn();
        init.visitMaxs(10, 10);
        init.visitEnd();
    }

    static void emitStaticFieldInitialization(AsmBuilder builder, ClassVisitor cv) {
        // Create the static class initializer to set the instance fields
        Map<String, Object> fields = builder.getObjectFieldMap();
        if (!fields.isEmpty()) {
            String classID = builder.getClassNamePath();
            AsmRuntime.setStaticClassData(classID, fields);

            SkinnyMethodAdapter clinit = new SkinnyMethodAdapter(cv, ACC_PUBLIC | ACC_STATIC, "<clinit>", CodegenUtils.sig(void.class), null, null);
            clinit.start();

            clinit.ldc(classID);
            clinit.invokestatic(AsmRuntime.class, "getStaticClassData", Map.class, String.class);
            clinit.astore(0);
            for (AsmBuilder.ObjectField f : builder.getObjectFieldArray()) {
                if (f.klass.isPrimitive()) {
                    cv.visitField(ACC_PRIVATE | ACC_FINAL | ACC_STATIC, f.name, CodegenUtils.ci(f.klass), null, f.value).visitEnd();
                } else {
                    cv.visitField(ACC_PRIVATE | ACC_FINAL | ACC_STATIC, f.name, CodegenUtils.ci(f.klass), null, null).visitEnd();
                    clinit.aload(0);
                    clinit.ldc(f.name);
                    clinit.invokeinterface(Map.class, "get", Object.class, Object.class);
                    clinit.checkcast(f.klass);
                    clinit.putstatic(builder.getClassNamePath(), f.name, CodegenUtils.ci(f.klass));
                }
            }

            clinit.voidreturn();
            clinit.visitMaxs(10, 10);
            clinit.visitEnd();
        }
    }
}
