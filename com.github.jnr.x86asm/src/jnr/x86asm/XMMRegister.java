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

public final class XMMRegister extends BaseReg {
    /** Internal cache of xmm registers */
    static final XMMRegister[] cache = new XMMRegister[16];

    static {
        for (int i = 0; i < cache.length; ++i) {
            cache[i] = new XMMRegister(REG.REG_XMM | i, 16);
        }
    }

    private XMMRegister(int code, int size) {
        super(code, size);
    }

    public static final XMMRegister xmm(int idx) {
        if (idx >= 0 && idx < cache.length) {
            return cache[idx];
        }

        throw new IllegalArgumentException("invalid xmm register");
    }
}
