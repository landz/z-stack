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
import jnr.udis86.X86Disassembler;
import jnr.x86asm.Assembler;
import jnr.x86asm.REG;
import jnr.x86asm.Register;
import z.znr.InlineAssembler;
import z.znr.invoke.types.ParameterType;
import z.znr.invoke.types.ResultType;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static jnr.x86asm.Asm.*;
import static z.znr.invoke.linux.x64.CodegenUtils.sig;

/**
 * Compilers method trampoline stubs for x86_64 
 */
final class X64StubCompiler extends StubCompiler {

    public final static boolean DEBUG = Boolean.getBoolean("jnr.invoke.compile.dump");

    @Override
    // There is only one calling convention; SYSV, so abort if someone tries to use stdcall
    boolean canCompile(ResultType returnType, ParameterType[] parameterTypes) {
        switch (returnType.nativeType()) {
            case VOID:
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
                break;

            default:
                return false;
        }


        int fCount = 0;
        int iCount = 0;

        for (ParameterType t : parameterTypes) {
            switch (t.nativeType()) {
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
                    ++iCount;
                    break;

                case FLOAT:
                case DOUBLE:
                    ++fCount;
                    break;

                default:
                    // Fail on anything else
                    return false;
            }
        }

        // We can only safely compile methods with up to 6 integer and 8 floating point parameters
        return iCount <= 6 && fCount <= 8;
    }


    static final Register[] srcRegisters8 = { dl, cl, r8b, r9b };
    static final Register[] srcRegisters16 = { dx, cx, r8w, r9w };
    static final Register[] srcRegisters32 = { edx, ecx, Register.gpr(REG.REG_R8D), Register.gpr(REG.REG_R9D) };
    static final Register[] srcRegisters64 = { rdx, rcx, r8, r9 };
    static final Register[] dstRegisters32 = { edi, esi, edx, ecx, Register.gpr(REG.REG_R8D), Register.gpr(REG.REG_R9D) };
    static final Register[] dstRegisters64 = { rdi, rsi, rdx, rcx, r8, r9 };

    @Override
    final void compile(InlineAssembler inlineAssembler, String name, ResultType resultType, ParameterType[] parameterTypes,
                       Class resultClass, Class[] parameterClasses, boolean saveErrno) {

        Assembler a = new Assembler(X86_64);
        int iCount = iCount(parameterTypes);
        int fCount = fCount(parameterTypes);

        boolean canJumpToTarget = !saveErrno & iCount <= 6 & fCount <= 8;
        switch (resultType.nativeType()) {
            case SINT:
            case UINT:
                canJumpToTarget &= int.class == resultClass;
                break;

            case SLONG_LONG:
            case ULONG_LONG:
                canJumpToTarget &= long.class == resultClass;
                break;

            case FLOAT:
                canJumpToTarget &= float.class == resultClass;
                break;

            case DOUBLE:
                canJumpToTarget &= double.class == resultClass;
                break;

            case VOID:
                break;

            default:
                canJumpToTarget = false;
                break;
        }

        // JNI functions all look like:
        // foo(JNIEnv* env, jobject self, arg...)
        // on AMD64, those sit in %rdi, %rsi, %rdx, %rcx, %r8 and %r9
        // So we need to shuffle all the integer args up to over-write the
        // env and self arguments
        //
//        for (int i = 0; i < Math.min(iCount, 4); i++) {
//            switch (parameterTypes[i].nativeType()) {
//                case SCHAR:
//                    a.movsx(dstRegisters64[i], srcRegisters8[i]);
//                    break;
//
//                case UCHAR:
//                    a.movzx(dstRegisters64[i], srcRegisters8[i]);
//                    break;
//
//                case SSHORT:
//                    a.movsx(dstRegisters64[i], srcRegisters16[i]);
//                    break;
//
//                case USHORT:
//                    a.movzx(dstRegisters64[i], srcRegisters16[i]);
//                    break;
//
//                case SINT:
//                    a.movsxd(dstRegisters64[i], srcRegisters32[i]);
//                    break;
//
//                case UINT:
//                    // mov with a 32bit dst reg zero extends to 64bit
//                    a.mov(dstRegisters32[i], srcRegisters32[i]);
//                    break;
//
//                default:
//                    a.mov(dstRegisters64[i], srcRegisters64[i]);
//                    break;
//            }
//        }

        if (iCount > 6) {
            throw new IllegalArgumentException("integer argument count > 6");
        }

        // For args 5 & 6 of the function, they would have been pushed on the stack
//        for (int i = 4; i < iCount; i++) {
//            int disp = 8 + ((4 - i) * 8);
//            switch (parameterTypes[i].nativeType()) {
//                case SCHAR:
//                    a.movsx(dstRegisters64[i], byte_ptr(rsp, disp));
//                    break;
//
//                case UCHAR:
//                    a.movzx(dstRegisters64[i], byte_ptr(rsp, disp));
//                    break;
//
//                case SSHORT:
//                    a.movsx(dstRegisters64[i], word_ptr(rsp, disp));
//                    break;
//
//                case USHORT:
//                    a.movzx(dstRegisters64[i], word_ptr(rsp, disp));
//                    break;
//
//                case SINT:
//                    a.movsxd(dstRegisters64[i], dword_ptr(rsp, disp));
//                    break;
//
//                case UINT:
//                    // mov with a 32bit dst reg zero extends to 64bit
//                    a.mov(dstRegisters32[i], dword_ptr(rsp, disp));
//                    break;
//
//                default:
//                    a.mov(dstRegisters64[i], qword_ptr(rsp, disp));
//                    break;
//            }
//        }

        // All the integer registers are loaded; there nothing to do for the floating
        // registers, as the first 8 args are already in xmm0..xmm7, so just sanity check
        if (fCount > 8) {
            throw new IllegalArgumentException("float argument count > 8");
        }

        if (canJumpToTarget) {
            inlineAssembler.assemble(a);
            stubs.add(new Stub(name, sig(resultClass, parameterClasses), a));
            return;
        }

        // Need to align the stack to 16 bytes for function call.
        // It already has 8 bytes pushed (the return address), so making space
        // to save the return value from the function neatly aligns it to 16 bytes
        int space = resultClass == float.class || resultClass == double.class
                    ? 24 : 8;
        a.sub(rsp, imm(space));

        // Clear %rax, since it is used by varargs functions to determine the number of float registers to be saved
        a.xor_(eax, eax);

        // Call to the actual native function
        long function = Util.inlineAssemblerToCodeAddress(inlineAssembler).address();
        a.call(imm(function));

        if (saveErrno) {
            // Save the return on the stack
            switch (resultType.nativeType()) {
                case VOID:
                    // No need to save/reload return value registers
                    break;

                case FLOAT:
                    a.movss(dword_ptr(rsp, 0), xmm0);
                    break;

                case DOUBLE:
                    a.movsd(qword_ptr(rsp, 0), xmm0);
                    break;

                default:
                    a.mov(qword_ptr(rsp, 0), rax);
            }

            // Save the errno in a thread-local variable
            a.call(imm(errnoFunctionAddress));

            // Retrieve return value and put it back in the appropriate return register
            switch (resultType.nativeType()) {
                case VOID:
                    // No need to save/reload return value registers
                    break;

                case SCHAR:
                    a.movsx(rax, byte_ptr(rsp, 0));
                    break;

                case UCHAR:
                    a.movzx(rax, byte_ptr(rsp, 0));
                    break;

                case SSHORT:
                    a.movsx(rax, word_ptr(rsp, 0));
                    break;

                case USHORT:
                    a.movzx(rax, word_ptr(rsp, 0));
                    break;

                case SINT:
                    a.movsxd(rax, dword_ptr(rsp, 0));
                    break;

                case UINT:
                    // storing a value in eax zeroes out the upper 32 bits of rax
                    a.mov(eax, dword_ptr(rsp, 0));
                    break;

                case FLOAT:
                    a.movss(xmm0, dword_ptr(rsp, 0));
                    break;

                case DOUBLE:
                    a.movsd(xmm0, qword_ptr(rsp, 0));
                    break;

                default:
                    a.mov(rax, qword_ptr(rsp, 0));
                    break;
            }

        } else {
            // sign/zero extend the result
            switch (resultType.nativeType()) {
                case SCHAR:
                    a.movsx(rax, al);
                    break;

                case UCHAR:
                    a.movzx(rax, al);
                    break;

                case SSHORT:
                    a.movsx(rax, ax);
                    break;

                case USHORT:
                    a.movzx(rax, ax);
                    break;

                case SINT:
                    if (long.class == resultClass) a.movsxd(rax, eax);
                    break;

                case UINT:
                    if (long.class == resultClass) a.mov(eax, eax);
                    break;
            }
        }

        // Restore rsp to original position
        a.add(rsp, imm(space));
        a.ret();

        stubs.add(new Stub(name, sig(resultClass, parameterClasses), a));
    }

    static int fCount(ParameterType[] parameterTypes) {
        int fCount = 0;

        for (ParameterType t : parameterTypes) {
            switch (t.nativeType()) {
                case FLOAT:
                case DOUBLE:
                    ++fCount;
                    break;
            }
        }

        return fCount;
    }

    static int iCount(ParameterType[] parameterTypes) {
        int iCount = 0;

        for (ParameterType t : parameterTypes) {
            switch (t.nativeType()) {
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
                    ++iCount;
                    break;
            }
        }

        return iCount;
    }


    private static final class StaticDataHolder {
        // Keep a reference from the loaded class to the pages holding the code for that class.
        static final Map<Class, PageHolder> PAGES
                = Collections.synchronizedMap(new WeakHashMap<Class, PageHolder>());
    }
    final List<Stub> stubs = new LinkedList<Stub>();


    static final class Stub {
        final String name;
        final String signature;
        final Assembler assembler;

        public Stub(String name, String signature, Assembler assembler) {
            this.name = name;
            this.signature = signature;
            this.assembler = assembler;
        }
    }

    static final class PageHolder {
        final PageManager pm;
        final long memory;
        final long pageCount;

        public PageHolder(PageManager pm, long memory, long pageCount) {
            this.pm = pm;
            this.memory = memory;
            this.pageCount = pageCount;
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                pm.freePages(memory, (int) pageCount);
            } catch (Throwable t) {
                Logger.getLogger(getClass().getName()).log(Level.WARNING,
                        "Exception when freeing native pages: %s", t.getLocalizedMessage());
            } finally {
                super.finalize();
            }
        }

    }

    @Override
    Object attach(Class clazz) {

        if (stubs.isEmpty()) {
            return new Object();
        }

        long codeSize = 0;
        for (Stub stub : stubs) {
            // add 8 bytes for alignment
            codeSize += stub.assembler.codeSize() + 8;
        }

        PageManager pm = PageManager.getInstance();

        long npages = (codeSize + pm.pageSize() - 1) / pm.pageSize();
        // Allocate some native memory for it
        long code = pm.allocatePages((int) npages, PageManager.PROT_READ | PageManager.PROT_WRITE);
        if (code == 0) {
            throw new OutOfMemoryError("allocatePages failed for codeSize=" + codeSize);
        }
        PageHolder page = new PageHolder(pm, code, npages);

        // Now relocate/copy all the assembler stubs into the real code area
        List<NativeMethod> methods = new ArrayList<NativeMethod>(stubs.size());
        long fn = code;
        PrintStream dbg = System.err;
        System.out.flush(); System.err.flush();

        for (Stub stub : stubs) {
            Assembler asm = stub.assembler;
            // align the start of all functions on a 8 byte boundary
            fn = align(fn, 8);
            ByteBuffer buf = ByteBuffer.allocate(asm.codeSize()).order(ByteOrder.LITTLE_ENDIAN);
            stub.assembler.relocCode(buf, fn);
            buf.flip();
            MemoryIO.getInstance().putByteArray(fn, buf.array(), buf.arrayOffset(), buf.limit());

            if (DEBUG && X86Disassembler.isAvailable()) {

                dbg.println(clazz.getName() + "." + stub.name + " " + stub.signature);
                X86Disassembler disassembler = X86Disassembler.create();
                disassembler.setMode(Platform.getPlatform().getCPU() == Platform.CPU.I386
                        ? X86Disassembler.Mode.I386 : X86Disassembler.Mode.X86_64);
                disassembler.setInputBuffer(fn, asm.offset());
                while (disassembler.disassemble()) {
                    dbg.printf("%8x: %s\n", disassembler.offset(), disassembler.insn());
                }
                if (buf.remaining() > asm.offset()) {
                    // libudis86 for some reason cannot understand the code asmjit emits for the trampolines
                    dbg.printf("%8x: <indirect call trampolines>\n", asm.offset());
                }
                dbg.println();
            }
            methods.add(new NativeMethod(fn, stub.name, stub.signature));

            fn += asm.codeSize();
        }

        pm.protectPages(code, (int) npages, PageManager.PROT_READ | PageManager.PROT_EXEC);

        NativeMethods.register(clazz, methods);
        StaticDataHolder.PAGES.put(clazz, page);

        return page;
    }

    private static int align(int offset, int align) {
        return (offset + align - 1) & ~(align - 1);
    }

    private static long align(long offset, long align) {
        return (offset + align - 1) & ~(align - 1);
    }
}
