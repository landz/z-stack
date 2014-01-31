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
import static jnr.x86asm.SIZE.*;

public final class Register extends BaseReg {
    private static final Register[] gpb = new Register[16];
    private static final Register[] gpw = new Register[16];
    private static final Register[] gpd = new Register[16];
    private static final Register[] gpq = new Register[16];

    static {
        for (int i = 0; i < 16; ++i) {
            gpb[i] = new Register(REG_GPB | i, SIZE_BYTE);
            gpw[i] = new Register(REG_GPW | i, SIZE_WORD);
            gpd[i] = new Register(REG_GPD | i, SIZE_DWORD);
            gpq[i] = new Register(REG_GPQ | i, SIZE_QWORD);
        }
    }

    Register(int code, int size) {
        super(code, size);
    }


    public static final Register gpr(int reg) {

        switch (reg & REGTYPE_MASK) {
            case REG_GPB:
                return gpb[reg & REGCODE_MASK];

            case REG_GPW:
                return gpw[reg & REGCODE_MASK];

            case REG_GPD:
                return gpd[reg & REGCODE_MASK];

            case REG_GPQ:
                return gpq[reg & REGCODE_MASK];
        }

        throw new IllegalArgumentException("invalid register 0x" + Integer.toHexString(reg));
    }

    private static final Register gpr(Register[] cache, int idx) {
        if (idx >= 0 && idx < 16) {
            return cache[idx];
        }

        throw new IllegalArgumentException("invalid register index " + idx);
    }

    public static final Register gpb(int idx) {
        return gpr(gpb, idx);
    }

    public static final Register gpw(int idx) {
        return gpr(gpw, idx);
    }

    public static final Register gpd(int idx) {
        return gpr(gpd, idx);
    }

    public static final Register gpq(int idx) {
        return gpr(gpq, idx);
    }
}
