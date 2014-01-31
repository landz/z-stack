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

public final class MMRegister extends BaseReg {
    /** Internal cache of mm registers */
    static final MMRegister[] cache = new MMRegister[8];

    static {
        for (int i = 0; i < cache.length; ++i) {
            cache[i] = new MMRegister(REG.REG_MM | i, 8);
        }
    }

    private MMRegister(int code, int size) {
        super(code, size);
    }

    public static final MMRegister mm(int code) {
        if (code >= 0 && code < cache.length) {
            return cache[code];
        }

        throw new IllegalArgumentException("invalid mm register");
    }
}
