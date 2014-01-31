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

import static jnr.x86asm.OP.*;

public class Operand {
    private final int op;
    private final int size;
    
    public Operand(int op, int size) {
        this.op = op;
        this.size = size;
    }

    
    /** Return type of operand, see @c OP. */
    public int op() {
        return op;
    }

    public int size() {
        return size;
    }

    /** Return @c true if operand is none (@c OP_NONE). */
    public boolean isNone() {
        return op() == OP_NONE;
    }

    /** Return @c true if operand is any (general purpose, mmx or sse) register (@c OP_REG). */
    public boolean isReg() {
        return op() == OP_REG;
    }

    /** Return @c true if operand is memory address (@c OP_MEM). */
    public boolean isMem() {
        return op() == OP_MEM;
    }

    /** Return @c true if operand is immediate (@c OP_IMM). */
    public boolean isImm() {
        return op() == OP_IMM;
    }

    /** Return @c true if operand is label (@c OP_LABEL). */
    public boolean isLabel() {
        return op() == OP_LABEL;
    }

    /** Return @c true if operand is any register or memory. */
    public final boolean isRegMem() {
        return isMem() || isReg();
    }

    public final boolean isRegCode(int code) {
        return this instanceof BaseReg && ((BaseReg) this).code() == code;
    }

    public final boolean isRegType(int type) {
        return this instanceof BaseReg && ((BaseReg) this).type() == type;
    }

    public final boolean isRegIndex(int index) {
        return this instanceof BaseReg && ((BaseReg) this).index() == index;
    }

    /** @brief Return @c true if operand is register of @a regType type or memory. */
    public final boolean isRegMem(int regType) {
        return isMem() || isRegType(regType);
    }
}
