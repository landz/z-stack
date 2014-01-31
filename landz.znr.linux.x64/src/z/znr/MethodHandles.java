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

package z.znr;

import z.znr.invoke.types.NativeType;
import z.znr.invoke.types.ParameterType;
import z.znr.invoke.types.ResultType;
import z.znr.invoke.types.Signature;
import z.znr.invoke.linux.x64.DataDirection;
import z.znr.invoke.linux.x64.Native;

import java.lang.invoke.MethodHandle;

public class MethodHandles {

  public static final MethodHandle asm(Class returnType, InlineAssembler inlineAssembler) {
    return asm(inlineAssembler, returnType);
  }

  public static final MethodHandle asm(Class returnType, Class parameter1, InlineAssembler inlineAssembler) {
    return asm(inlineAssembler, returnType, parameter1);
  }

  public static final MethodHandle asm(Class returnType, Class parameter1, Class parameter2, InlineAssembler inlineAssembler) {
    return asm(inlineAssembler, returnType, parameter1, parameter2);
  }

  public static final MethodHandle asm(Class returnType, Class parameter1, Class parameter2, Class parameter3, InlineAssembler inlineAssembler) {
    return asm(inlineAssembler, returnType, parameter1, parameter2, parameter3);
  }

  public static final MethodHandle asm(Class returnType, Class parameter1, Class parameter2, Class parameter3, Class parameter4, InlineAssembler inlineAssembler) {
    return asm(inlineAssembler, returnType, parameter1, parameter2, parameter3, parameter4);
  }

  public static final MethodHandle asm(Class returnType, Class parameter1, Class parameter2, Class parameter3, Class parameter4, Class parameter5, InlineAssembler inlineAssembler) {
    return asm(inlineAssembler, returnType, parameter1, parameter2, parameter3, parameter4, parameter5);
  }

  public static final MethodHandle asm(InlineAssembler inlineAssembler, Class returnType, Class... parameterTypes) {
    ParameterType[] jnrParameterTypes = new ParameterType[parameterTypes.length];
    for (int i = 0; i < parameterTypes.length; i++) {
      jnrParameterTypes[i] = parameterType(parameterTypes[i]);
    }

    return Native.getMethodHandle(Signature.getSignature(resultType(returnType), jnrParameterTypes), inlineAssembler);
  }

  static ResultType resultType(Class javaType) {
    if (javaType.isPrimitive()) {
      return ResultType.primitive(nativeType(javaType), javaType);
    }

    throw new IllegalArgumentException("unsupported return type: " + javaType);
  }

  static ParameterType parameterType(Class javaType) {
    if (javaType.isPrimitive()) {
      return ParameterType.primitive(nativeType(javaType), javaType);

    } else if (javaType.isArray() && javaType.getComponentType().isPrimitive()) {
      return ParameterType.array(javaType, DataDirection.INOUT);
    }

    throw new IllegalArgumentException("unsupported parameter type: " + javaType);
  }

  private static NativeType nativeType(Class klass) {
    if (byte.class == klass) {
      return NativeType.SCHAR;

    } else if (short.class == klass) {
      return NativeType.SSHORT;

    } else if (int.class == klass) {
      return NativeType.SINT;

    } else if (long.class == klass) {
      return NativeType.SLONG_LONG;

    } else if (float.class == klass) {
      return NativeType.FLOAT;

    } else if (double.class == klass) {
      return NativeType.DOUBLE;

    } else if (void.class == klass) {
      return NativeType.VOID;
    }
    throw new IllegalArgumentException("unsupported signature type: " + klass);
  }


}
