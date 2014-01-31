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

import com.kenai.jffi.ObjectParameterType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 *
 */
public abstract class PrimitiveArrayParameterStrategy extends ObjectParameterStrategy {
    static final PrimitiveArrayParameterStrategy BYTE = new PrimitiveArrayParameterStrategy(ObjectParameterType.BYTE) {
        public int length(Object o) {
            return ((byte[]) o).length;
        }
    };

    static final PrimitiveArrayParameterStrategy SHORT = new PrimitiveArrayParameterStrategy(ObjectParameterType.SHORT) {
        public int length(Object o) {
            return ((short[]) o).length;
        }
    };

    static final PrimitiveArrayParameterStrategy CHAR = new PrimitiveArrayParameterStrategy(ObjectParameterType.CHAR) {
        public int length(Object o) {
            return ((char[]) o).length;
        }
    };

    static final PrimitiveArrayParameterStrategy INT = new PrimitiveArrayParameterStrategy(ObjectParameterType.INT) {
        public int length(Object o) {
            return ((int[]) o).length;
        }
    };

    static final PrimitiveArrayParameterStrategy LONG = new PrimitiveArrayParameterStrategy(ObjectParameterType.LONG) {
        public int length(Object o) {
            return ((long[]) o).length;
        }
    };

    static final PrimitiveArrayParameterStrategy FLOAT = new PrimitiveArrayParameterStrategy(ObjectParameterType.FLOAT) {
        public int length(Object o) {
            return ((float[]) o).length;
        }
    };

    static final PrimitiveArrayParameterStrategy DOUBLE = new PrimitiveArrayParameterStrategy(ObjectParameterType.DOUBLE) {
        public int length(Object o) {
            return ((double[]) o).length;
        }
    };

    static final PrimitiveArrayParameterStrategy BOOLEAN = new PrimitiveArrayParameterStrategy(ObjectParameterType.BOOLEAN) {
        public int length(Object o) {
            return ((boolean[]) o).length;
        }
    };

    PrimitiveArrayParameterStrategy(ObjectParameterType.ComponentType componentType) {
        super(HEAP, ObjectParameterType.create(ObjectParameterType.ObjectType.ARRAY, componentType));
    }

    @Override
    public final long address(Object o) {
        return 0;
    }

    @Override
    public final Object object(Object o) {
        return o;
    }

    @Override
    public final int offset(Object o) {
        return 0;
    }

    public static MethodHandle getStrategyLookupHandle(Class arrayType) {
        return MethodHandles.guardWithTest(Util.getNotNullHandle().asType(MethodType.methodType(boolean.class, arrayType)),
                MethodHandles.dropArguments(MethodHandles.constant(ObjectParameterStrategy.class, strategyForComponentType(arrayType.getComponentType())), 0, arrayType),
                MethodHandles.dropArguments(MethodHandles.constant(ObjectParameterStrategy.class, NullObjectParameterStrategy.NULL), 0, arrayType));
    }

    public static MethodHandle getDirectCheckHandle(Class arrayType) {
        return Util.getIsNullHandle().asType(MethodType.methodType(boolean.class, arrayType));
    }

    public static MethodHandle getDirectAddressHandle(Class arrayType) {
        return MethodHandles.dropArguments(MethodHandles.constant(long.class, (long) 0), 0, arrayType);
    }

    private static ObjectParameterStrategy strategyForComponentType(Class componentType) {
        if (byte.class == componentType) {
            return PrimitiveArrayParameterStrategy.BYTE;

        } else if (short.class == componentType) {
            return PrimitiveArrayParameterStrategy.SHORT;

        } else if (char.class == componentType) {
            return PrimitiveArrayParameterStrategy.CHAR;

        } else if (int.class == componentType) {
            return PrimitiveArrayParameterStrategy.INT;

        } else if (long.class == componentType) {
            return PrimitiveArrayParameterStrategy.LONG;

        } else if (float.class == componentType) {
            return PrimitiveArrayParameterStrategy.FLOAT;

        } else if (double.class == componentType) {
            return PrimitiveArrayParameterStrategy.DOUBLE;

        } else if (boolean.class == componentType) {
            return PrimitiveArrayParameterStrategy.BOOLEAN;
        }
        throw new IllegalArgumentException("no object strategy for arrays of " + componentType);
    }

}
