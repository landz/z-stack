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
public final class Util {
    private Util() {
    }
    
    /** Returns @c true if a given integer @a x is signed 8 bit integer */
    static final boolean isInt8(long x) {
        return x >= -128 && x <= 127;
    }
    /** Returns @c true if a given integer @a x is unsigned 8 bit integer */
    static final boolean isUInt8(long x) {
        return x >= 0 && x <= 255;
    }

    /** Returns @c true if a given integer @a x is signed 16 bit integer */
    static final boolean isInt16(long x) {
        return x >= -32768 && x <= 32767;
    }

    /** Returns @c true if a given integer @a x is unsigned 16 bit integer */
    static final boolean isUInt16(long x) {
        return x >= 0 && x <= 65535;
    }

    /** Returns @c true if a given integer @a x is signed 32 bit integer */
    static final boolean isInt32(long x) {
        return x >= Integer.MIN_VALUE && x <= Integer.MAX_VALUE;
    }

    /** Returns @c true if a given integer @a x is unsigned 32 bit integer */
    static final boolean isUInt32(long x) {
        return x >= 0 && x <= 0xffffffffL;
    }
}
