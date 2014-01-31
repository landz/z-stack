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

package z.znr.invoke.types;

/**
 * NativeType defines the primitive types supported internally.
 *
 * All other types are defined in terms of these primitive types.
 */
public enum NativeType {
    /** Void type.  Only used for function return types. */
    VOID,

    /** Signed char.  Equivalent to a C char or signed char type.  Usually 1 byte in size. */
    SCHAR,

    /** Unsigned char.  Equivalent to a C unsigned char type.  Usually 1 byte in size */
    UCHAR,

    /** Signed short integer.  Equivalent to a C short or signed short type.  Usually 2 bytes in size. */
    SSHORT,

    /** Unsigned short integer.  Equivalent to a C unsigned short type.  Usually 2 bytes in size. */
    USHORT,

    /** Signed integer.  Equivalent to a C int or signed int type.  Usually 4 bytes in size. */
    SINT,

    /** Unsigned integer.  Equivalent to a C unsigned int type.  Usually 4 bytes in size. */
    UINT,

    /** Signed long integer.  Equivalent to a C long or signed long type.  Can be either 4 or 8 bytes in size, depending on the platform. */
    SLONG,

    /** Unsigned long integer.  Equivalent to a C unsigned long type.  Can be either 4 or 8 bytes in size, depending on the platform. */
    ULONG,

    /** Signed long long integer.  Equivalent to a C long long or signed long long type.  Usually 8 bytes in size. */
    SLONG_LONG,

    /** Unsigned long long integer.  Equivalent to a C unsigned long long type.  Usually 8 bytes in size. */
    ULONG_LONG,

    /** Single precision floating point.  Equivalent to a C float type.  Usually 4 bytes in size. */
    FLOAT,

    /** Double precision floating point.  Equivalent to a C double type.  Usually 8 bytes in size. */
    DOUBLE,

    /** Native memory address.  Equivalent to a C void* or char* pointer type.  Can be either 4 or 8 bytes in size, depending on the platform. */
    POINTER;

    private com.kenai.jffi.Type jffiType;

    public int size() {
        return jffiType().size();
    }

    public int alignment() {
        return jffiType().alignment();
    }

    public boolean isUnsigned() {
        return name().charAt(0) == 'U' || this == POINTER;
    }

    public com.kenai.jffi.Type jffiType() {
        return jffiType != null ? jffiType : resolveType();
    }

    private synchronized com.kenai.jffi.Type resolveType() {
        return jffiType != null ? jffiType : (jffiType = jffiType(this));
    }

    private static com.kenai.jffi.Type jffiType(NativeType nativeType) {
        switch (nativeType) {
            case SCHAR:
                return com.kenai.jffi.Type.SCHAR;

            case UCHAR:
                return com.kenai.jffi.Type.UCHAR;

            case SSHORT:
                return com.kenai.jffi.Type.SSHORT;

            case USHORT:
                return com.kenai.jffi.Type.USHORT;

            case SINT:
                return com.kenai.jffi.Type.SINT;

            case UINT:
                return com.kenai.jffi.Type.UINT;

            case SLONG:
                return com.kenai.jffi.Type.SLONG;

            case ULONG:
                return com.kenai.jffi.Type.ULONG;

            case SLONG_LONG:
                return com.kenai.jffi.Type.SLONG_LONG;

            case ULONG_LONG:
                return com.kenai.jffi.Type.ULONG_LONG;

            case FLOAT:
                return com.kenai.jffi.Type.FLOAT;
            case DOUBLE:
                return com.kenai.jffi.Type.DOUBLE;

            case POINTER:
                return com.kenai.jffi.Type.POINTER;

            case VOID:
                return com.kenai.jffi.Type.VOID;

            default:
                throw new UnsupportedOperationException("Cannot resolve type " + nativeType);
        }
    }
}
