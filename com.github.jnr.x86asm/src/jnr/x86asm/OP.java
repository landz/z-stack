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

package jnr.x86asm;

/** Operand types that can be encoded in <tt>Op</tt> operand */
public final class OP {
    /** Invalid operand */
    public static final int OP_NONE = 0;
    
    /** Operand is register. */
    public static final int OP_REG = 1;

    /** Operand is memory. */
    public static final int OP_MEM = 2;

    /** Operand is immediate. */
    public static final int OP_IMM = 3;

    /** Operand is label. */
    public static final int OP_LABEL = 4;

    /** Operand is variable. */
    public static final int OP_VAR = 5;

    private OP() { }
}
