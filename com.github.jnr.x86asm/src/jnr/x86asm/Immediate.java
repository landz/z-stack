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

/**
 *
 */
public final class Immediate extends Operand {
    private final long value;
    private final boolean isUnsigned;
    private final RELOC_MODE relocMode;

    public Immediate(long value, boolean isUnsigned) {
        super(OP.OP_IMM, 0);
        this.value = value;
        this.isUnsigned = isUnsigned;
        this.relocMode = RELOC_MODE.RELOC_NONE;
    }

    public long value() {
        return value;
    }

    public final byte byteValue() {
        return (byte) value;
    }

    public final short shortValue() {
        return (short) value;
    }

    public final int intValue() {
        return (int) value;
    }

    public final long longValue() {
        return value;
    }

    /** Return true if immediate is unsigned value. */
    public final boolean isUnsigned() {
        return isUnsigned;
    }

    /** Return relocation mode. */
    RELOC_MODE relocMode() {
        return relocMode;
    }

    /**
     * Internal cache of common native long values
     */
    private static final class Cache {
        private Cache() {}

        static final Immediate[] cache = new Immediate[256];

        static {
            for (int i = 0; i < cache.length; ++i) {
                cache[i] = new Immediate(i - 128, false);
            }
        }

    }

    public static final Immediate imm(long value) {
        return value >= -128 && value <= 127
            ? Cache.cache[128 + (int) value] : new Immediate(value, false);
    }

    public static final Immediate uimm(long value) {
        return new Immediate(value, true);
    }
}
