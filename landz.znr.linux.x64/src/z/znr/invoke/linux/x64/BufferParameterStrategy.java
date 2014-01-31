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
import com.kenai.jffi.ObjectParameterType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.*;
import java.util.EnumSet;

/**
 *
 */
public final class BufferParameterStrategy extends ObjectParameterStrategy {
    private final int shift;

    private BufferParameterStrategy(ObjectParameterStrategy.StrategyType type, ObjectParameterType.ComponentType componentType) {
        super(type, ObjectParameterType.create(ObjectParameterType.ObjectType.ARRAY, componentType));
        this.shift = calculateShift(componentType);
    }

    public long address(Buffer buffer) {
        return buffer.isDirect() ? MemoryIO.getInstance().getDirectBufferAddress(buffer) + (buffer.position() << shift) : 0L;
    }

    @Override
    public long address(Object o) {
        return address((Buffer) o);
    }

    @Override
    public Object object(Object o) {
        return ((Buffer) o).array();
    }

    @Override
    public int offset(Object o) {
        Buffer buffer = (Buffer) o;
        return buffer.arrayOffset() + buffer.position();
    }

    @Override
    public int length(Object o) {
        return ((Buffer) o).remaining();
    }

    static int calculateShift(ObjectParameterType.ComponentType componentType) {
        switch (componentType) {
            case BYTE:
                return 0;

            case SHORT:
            case CHAR:
                return 1;

            case INT:
            case BOOLEAN:
            case FLOAT:
                return 2;

            case LONG:
            case DOUBLE:
                return 3;
            default:
                throw new IllegalArgumentException("unsupported component type: " + componentType);
        }
    }


    private static final BufferParameterStrategy[] DIRECT_BUFFER_PARAMETER_STRATEGIES;
    private static final BufferParameterStrategy[] HEAP_BUFFER_PARAMETER_STRATEGIES;
    static {
        EnumSet<ObjectParameterType.ComponentType> componentTypes = EnumSet.allOf(ObjectParameterType.ComponentType.class);
        DIRECT_BUFFER_PARAMETER_STRATEGIES = new BufferParameterStrategy[componentTypes.size()];
        HEAP_BUFFER_PARAMETER_STRATEGIES = new BufferParameterStrategy[componentTypes.size()];
        for (ObjectParameterType.ComponentType componentType : componentTypes) {
            DIRECT_BUFFER_PARAMETER_STRATEGIES[componentType.ordinal()] = new BufferParameterStrategy(DIRECT, componentType);
            HEAP_BUFFER_PARAMETER_STRATEGIES[componentType.ordinal()] = new BufferParameterStrategy(HEAP, componentType);
        }
    }

    static ObjectParameterStrategy direct(ObjectParameterType.ComponentType componentType) {
        return DIRECT_BUFFER_PARAMETER_STRATEGIES[componentType.ordinal()];
    }

    static ObjectParameterStrategy heap(ObjectParameterType.ComponentType componentType) {
        return HEAP_BUFFER_PARAMETER_STRATEGIES[componentType.ordinal()];
    }

    public static MethodHandle getStrategyHandle(Class<? extends Buffer> bufferClass) {
        MethodHandle bufferStrategyHandle = MethodHandles.guardWithTest(getBufferIsDirectHandle().asType(MethodType.methodType(boolean.class, bufferClass)),
                MethodHandles.dropArguments(MethodHandles.constant(ObjectParameterStrategy.class, direct(componentType(bufferClass))), 0, bufferClass),
                MethodHandles.dropArguments(MethodHandles.constant(ObjectParameterStrategy.class, heap(componentType(bufferClass))), 0, bufferClass));


        return MethodHandles.guardWithTest(Util.getNotNullHandle().asType(MethodType.methodType(boolean.class, bufferClass)),
                bufferStrategyHandle,
                MethodHandles.dropArguments(MethodHandles.constant(ObjectParameterStrategy.class, NullObjectParameterStrategy.NULL), 0, bufferClass));
    }

    public static MethodHandle getBufferIsDirectHandle() {
        try {
            return MethodHandles.publicLookup().findVirtual(Buffer.class, "isDirect", MethodType.methodType(boolean.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static ObjectParameterType.ComponentType componentType(Class<? extends Buffer> bufferClass) {
        if (ByteBuffer.class == bufferClass) {
            return ObjectParameterType.ComponentType.BYTE;

        } else if (CharBuffer.class == bufferClass) {
            return ObjectParameterType.ComponentType.CHAR;

        } else if (ShortBuffer.class == bufferClass) {
            return ObjectParameterType.ComponentType.SHORT;

        } else if (IntBuffer.class == bufferClass) {
            return ObjectParameterType.ComponentType.INT;

        } else if (LongBuffer.class == bufferClass) {
            return ObjectParameterType.ComponentType.LONG;

        } else if (FloatBuffer.class == bufferClass) {
            return ObjectParameterType.ComponentType.FLOAT;

        } else if (DoubleBuffer.class == bufferClass) {
            return ObjectParameterType.ComponentType.DOUBLE;
        }
        throw new IllegalArgumentException("cannot determine component type of " + bufferClass);
    }
}
