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

import z.znr.invoke.linux.x64.Util;

import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Native function call context
 *
 * This class holds all the information that JFFI needs to correctly call a
 * native function, or to implement a callback from native code to java.
 */
public final class Signature {

    public static final int DEFAULT = 0x1;
    public static final int SAVE_ERRNO    = 0x2;
    public static final int FAULT_PROTECT = 0x4;
    private static final int VALID_FLAGS = (DEFAULT | SAVE_ERRNO | FAULT_PROTECT);

    /** The return type of this function */
    private final ResultType resultType;

    /** The parameter types of this function */
    private final ParameterType[] parameterTypes;

    private final int flags;

    private com.kenai.jffi.CallContext jffiContext;

    /**
     * Returns a {@link Signature} instance.  This may return a previously cached instance that matches
     * the signature requested, and should be used in preference to instantiating new instances.
     *
     * @param resultType The return type of the native function.
     * @param parameterTypes The parameter types the function accepts.
     * @return An instance of Signature
     */
    public static Signature getSignature(ResultType resultType, ParameterType[] parameterTypes) {
        return new Signature(resultType, parameterTypes, DEFAULT);
    }

    /**
     * Returns a {@link Signature} instance.  This may return a previously cached instance that matches
     * the signature requested, and should be used in preference to instantiating new instances.
     *
     * @param resultType The return type of the native function.
     * @param parameterTypes The parameter types the function accepts.
     * @param flags the flags for the call.
     * @return An instance of Signature
     */
    public static Signature getSignature(ResultType resultType, ParameterType[] parameterTypes, int flags) {
        return new Signature(resultType, parameterTypes, flags);
    }

    /**
     * Returns a {@link Signature} instance.  This may return a previously cached instance that matches
     * the signature requested, and should be used in preference to instantiating new instances.
     *
     * @param resultType The return type of the native function.
     * @param parameterTypes The parameter types the function accepts.
     * @param saveErrno Indicates that the errno should be saved
     * @return An instance of Signature
     */
    public static Signature getSignature(ResultType resultType, ParameterType[] parameterTypes, boolean saveErrno) {
        return new Signature(resultType, parameterTypes, (saveErrno ? SAVE_ERRNO : 0));
    }

    public static Signature getSignature(ResultType resultType, ParameterType[] parameterTypes, boolean saveErrno,
                                         boolean faultProtect) {
        return new Signature(resultType, parameterTypes, (saveErrno ? SAVE_ERRNO : 0) | (faultProtect ? FAULT_PROTECT : 0));
    }

    /**
     * Returns a {@link Signature} instance.  This may return a previously cached instance that matches
     * the signature requested, and should be used in preference to instantiating new instances.
     *
     * @param flags Flags.
     * @param resultType The return type of the native function.
     * @param parameterTypes The parameter types the function accepts.
     * @return An instance of Signature
     */
    public static Signature getSignature(int flags, ResultType resultType, ParameterType... parameterTypes) {
        return new Signature(resultType, parameterTypes, flags);
    }

    /**
     * Creates a new instance of <tt>Function</tt>.
     *
     * @param resultType The return type of the native function.
     * @param parameterTypes The parameter types the function accepts.
     */
    private Signature(ResultType resultType, ParameterType[] parameterTypes, int flags) {
        this.resultType = resultType;
        this.parameterTypes = parameterTypes.clone();
        this.flags = flags & VALID_FLAGS;
    }

    /**
     * Gets the number of parameters the native function accepts.
     *
     * @return The number of parameters the native function accepts.
     */
    public final int getParameterCount() {
        return parameterTypes.length;
    }

    /**
     * Gets the native return type of this function.
     *
     * @return The native return type of this function.
     */
    public final ResultType getResultType() {
        return resultType;
    }

    /**
     * Gets the type of a parameter.
     *
     * @param index The index of the parameter in the function signature
     * @return The <tt>Type</tt> of the parameter.
     */
    public final ParameterType getParameterType(int index) {
        return parameterTypes[index];
    }

    public com.kenai.jffi.CallContext getNativeCallContext() {
        return jffiContext != null ? jffiContext : createNativeCallContext();
    }

    public boolean saveErrno() {
        return (flags & SAVE_ERRNO) != 0;
    }

    private synchronized com.kenai.jffi.CallContext createNativeCallContext() {
        if (jffiContext != null) {
            return jffiContext;
        }
        com.kenai.jffi.Type[] nativeParamTypes = new com.kenai.jffi.Type[parameterTypes.length];

        for (int i = 0; i < nativeParamTypes.length; ++i) {
            nativeParamTypes[i] = parameterTypes[i].jffiType();
        }

        return jffiContext = com.kenai.jffi.CallContext.getCallContext(resultType.jffiType(),
                nativeParamTypes, jffiConvention(flags), saveErrno(), (flags & FAULT_PROTECT) != 0);
    }

    public MethodType methodType() {
        return MethodType.methodType(resultType.javaType(), Util.javaTypeArray(parameterTypes));
    }

    public ParameterType[] parameterTypeArray() {
        return parameterTypes.clone();
    }

    public List<ParameterType> parameterTypeList() {
        return Collections.unmodifiableList(Arrays.asList(parameterTypes));
    }

    public Signature asPrimitiveContext() {
        return getSignature(flags, resultType.asPrimitiveType(), Util.asPrimitiveTypes(parameterTypes));
    }

    static com.kenai.jffi.CallingConvention jffiConvention(int flags) {
        return com.kenai.jffi.CallingConvention.DEFAULT;
    }
}
