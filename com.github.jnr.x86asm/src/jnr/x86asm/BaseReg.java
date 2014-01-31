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

import static jnr.x86asm.REG.*;

public abstract class BaseReg extends Operand {

    public final int code;

    public BaseReg(int code, int size) {
        super(OP.OP_REG, size);
        this.code = code;
    }

    //! @brief Return register type, see @c REG.
    public final int type() {
        return code() & REGTYPE_MASK;
    }

    //! @brief Return register code, see @c REG.
    public final int code() {
        return code;
    }

    //! @brief Return register index (value from 0 to 7/15).
    public final int index() {
        return code() & REGCODE_MASK;
    }


//    public final boolean isRegCode(int code) {
//        return code() == code;
//    }
//
//    public final boolean isRegType(int type) {
//        return type() == type;
//    }
//
//    public final boolean isRegIndex(int index) {
//        return index() == index;
//    }
}
