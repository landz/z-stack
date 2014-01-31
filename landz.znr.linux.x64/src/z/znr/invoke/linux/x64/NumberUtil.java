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


import z.znr.invoke.types.NativeType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static z.znr.invoke.linux.x64.Native.LOOKUP;

public final class NumberUtil {
    private NumberUtil() {}

    public static boolean isPrimitiveInt(Class c) {
        return byte.class == c || char.class == c || short.class == c || int.class == c || boolean.class == c;
    }


    public static void widen(SkinnyMethodAdapter mv, Class from, Class to) {
        if (long.class == to && long.class != from && isPrimitiveInt(from)) {
            mv.i2l();

        } else if (boolean.class == to && boolean.class != from && isPrimitiveInt(from)) {
            // Ensure only 0x0 and 0x1 values are used for boolean
            mv.iconst_1();
            mv.iand();
        }
    }

    public static void widen(SkinnyMethodAdapter mv, Class from, Class to, NativeType nativeType) {
        if (isPrimitiveInt(from)) {
            if (nativeType == NativeType.UCHAR) {
                mv.pushInt(0xff);
                mv.iand();

            } else if (nativeType == NativeType.USHORT) {
                mv.pushInt(0xffff);
                mv.iand();
            }

            if (long.class == to) {
                mv.i2l();
                switch (nativeType) {
                    case UINT:
                    case ULONG:
                    case POINTER:
                        if (Util.sizeof(nativeType) < 8) {
                            // strip off bits 32:63
                            mv.ldc(0xffffffffL);
                            mv.land();
                        }
                        break;
                }
            }
        }
    }


    public static void narrow(SkinnyMethodAdapter mv, Class from, Class to) {
        if (!from.equals(to)) {
            if (byte.class == to || short.class == to || char.class == to || int.class == to || boolean.class == to) {
                if (long.class == from) {
                    mv.l2i();
                }

                if (byte.class == to) {
                    mv.i2b();

                } else if (short.class == to) {
                    mv.i2s();

                } else if (char.class == to) {
                    mv.i2c();

                } else if (boolean.class == to) {
                    // Ensure only 0x0 and 0x1 values are used for boolean
                    mv.iconst_1();
                    mv.iand();
                }
            }
        }
    }


    public static void convertPrimitive(SkinnyMethodAdapter mv, final Class from, final Class to) {
        narrow(mv, from, to);
        widen(mv, from, to);
    }


    public static void convertPrimitive(SkinnyMethodAdapter mv, final Class from, final Class to, final NativeType nativeType) {
        if (boolean.class == to) {
            narrow(mv, from, to);
            return;
        }

        switch (nativeType) {
            case SCHAR:
                narrow(mv, from, byte.class);
                widen(mv, byte.class, to);
                break;

            case SSHORT:
                narrow(mv, from, short.class);
                widen(mv, short.class, to);
                break;

            case SINT:
                narrow(mv, from, int.class);
                widen(mv, int.class, to);
                break;

            case UCHAR:
                narrow(mv, from, int.class);
                mv.pushInt(0xff);
                mv.iand();
                widen(mv, int.class, to);
                break;

            case USHORT:
                narrow(mv, from, int.class);
                mv.pushInt(0xffff);
                mv.iand();
                widen(mv, int.class, to);
                break;

            case UINT:
            case ULONG:
            case POINTER:
                if (Util.sizeof(nativeType) <= 4) {
                    narrow(mv, from, int.class);
                    if (long.class == to) {
                        mv.i2l();
                        // strip off bits 32:63
                        mv.ldc(0xffffffffL);
                        mv.land();
                    }
                } else {
                    widen(mv, from, to);
                }
                break;


            case FLOAT:
            case DOUBLE:
                break;

            default:
                narrow(mv, from, to);
                widen(mv, from, to);
                break;
        }
    }

    static MethodHandle getParameterConversionHandle(NativeType nativeType, Class from, Class to) {
        try {
            switch (nativeType) {
                case FLOAT:
                    return LOOKUP.findStatic(Float.class, "floatToRawIntBits", MethodType.methodType(int.class, float.class))
                            .asType(MethodType.methodType(to, from));

                case DOUBLE:
                    return LOOKUP.findStatic(Double.class, "doubleToRawLongBits", MethodType.methodType(long.class, double.class))
                            .asType(MethodType.methodType(to, from));

                default:
                    return getIntegerConversionHandle(nativeType, from, to);
            }

        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    static MethodHandle getResultConversionHandle(NativeType nativeType, Class from, Class to) {
        try {
            switch (nativeType) {
                case FLOAT:
                    return MethodHandles.explicitCastArguments(LOOKUP.findStatic(Float.class, "intBitsToFloat", MethodType.methodType(float.class, int.class)),
                            MethodType.methodType(to, from));

                case DOUBLE:
                    return LOOKUP.findStatic(Double.class, "longBitsToDouble", MethodType.methodType(double.class, long.class))
                            .asType(MethodType.methodType(to, from));

                case VOID:
                    return null;

                default:
                    return getIntegerConversionHandle(nativeType, from, to);
            }

        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    static MethodHandle getIntegerConversionHandle(NativeType nativeType, Class from, Class to) throws NoSuchMethodException, IllegalAccessException {
        switch (nativeType) {
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
                if (nativeType.size() <= 4) {
                    Class nativeIntType = long.class == to ? long.class : int.class;
                    String conversionHelper = (nativeType.isUnsigned() ? "u" : "s") + Integer.toString(nativeType.size() * 8);
                    MethodHandle mh = LOOKUP.findStatic(AsmRuntime.class, conversionHelper, MethodType.methodType(nativeIntType, nativeIntType));
                    return MethodHandles.explicitCastArguments(mh, MethodType.methodType(to, from));
                }

                return null;

            default:
                return null;
        }
    }
}
