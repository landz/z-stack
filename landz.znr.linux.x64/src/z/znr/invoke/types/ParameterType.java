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

import z.znr.invoke.linux.x64.BufferParameterStrategy;
import z.znr.invoke.linux.x64.DataDirection;
import z.znr.invoke.linux.x64.PrimitiveArrayParameterStrategy;
import z.znr.invoke.linux.x64.Util;

import java.lang.invoke.MethodHandle;

public final class ParameterType extends SignatureType {
    private final DataDirection dataDirection;
    private final MethodHandle lookupObjectStrategy;
    private final MethodHandle directCheckHandle;
    private final MethodHandle directAddressHandle;

    private ParameterType(NativeType nativeType, Class javaType, DataDirection dataDirection) {
        this(nativeType, javaType, dataDirection, nativeType.jffiType());
    }

    private ParameterType(NativeType nativeType, Class javaType, DataDirection dataDirection, com.kenai.jffi.Type jffiType) {
        this(nativeType, javaType, dataDirection, jffiType, null, null, null);
    }

    private ParameterType(NativeType nativeType, Class javaType, DataDirection dataDirection, com.kenai.jffi.Type jffiType,
                          MethodHandle lookupObjectStrategy, MethodHandle directCheckHandle, MethodHandle directAddressHandle) {
        super(nativeType, javaType, jffiType);
        this.dataDirection = dataDirection;
        this.lookupObjectStrategy = lookupObjectStrategy;
        this.directCheckHandle = directCheckHandle;
        this.directAddressHandle = directAddressHandle;
    }


    public static ParameterType primitive(NativeType nativeType, Class javaType) {
        return new ParameterType(nativeType, javaType, DataDirection.INOUT);
    }

    public static ParameterType array(Class javaType, DataDirection dataDirection) {
        return object(javaType, dataDirection,
                PrimitiveArrayParameterStrategy.getStrategyLookupHandle(javaType),
                PrimitiveArrayParameterStrategy.getDirectCheckHandle(javaType),
                PrimitiveArrayParameterStrategy.getDirectAddressHandle(javaType));
    }

    public static ParameterType buffer(Class<? extends java.nio.Buffer> bufferClass, DataDirection dataDirection) {
        return object(bufferClass, dataDirection, BufferParameterStrategy.getStrategyHandle(bufferClass));
    }

    public static ParameterType object(Class javaType, DataDirection dataDirection, MethodHandle lookupObjectStrategy,
                                       MethodHandle directCheckHandle, MethodHandle directAddressHandle) {
        return new ParameterType(NativeType.POINTER, javaType, dataDirection, NativeType.POINTER.jffiType(),
                lookupObjectStrategy, directCheckHandle, directAddressHandle);
    }

    public static ParameterType object(Class javaType, DataDirection dataDirection, MethodHandle lookupObjectStrategy) {
        return object(javaType, dataDirection, lookupObjectStrategy, Util.getDirectCheckHandle(lookupObjectStrategy),
                Util.getDirectAddressHandle(lookupObjectStrategy));
    }

    public DataDirection getDataDirection() {
        return dataDirection;
    }

    public boolean isObject() {
        return lookupObjectStrategy != null;
    }

    public MethodHandle getObjectStrategyHandle() {
        return lookupObjectStrategy;
    }

    public MethodHandle getDirectCheckHandle() {
        return directCheckHandle;
    }

    public MethodHandle getDirectAddressHandle() {
        return directAddressHandle;
    }

    public ParameterType asPrimitiveType() {
        return lookupObjectStrategy != null ? ParameterType.primitive(NativeType.POINTER, long.class) : this;
    }
}
