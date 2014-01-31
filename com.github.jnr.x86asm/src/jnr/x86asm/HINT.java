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
 *  Condition hint, see @c AsmJit::Serializer::jz() and friends.
 */
public enum HINT {

    /** No hint. */
    HINT_NONE(0),

    /** Condition will be taken (likely). */
    HINT_TAKEN(0x3e),

    /** Condition will be not taken (unlikely). */
    HINT_NOT_TAKEN(0x2e);


    private final int value;
    HINT(int value) {
        this.value = value;
    }

    public final int value() {
        return value;
    }

    public static final HINT valueOf(int value) {
        switch (value) {
            case 0x3e:
                return HINT_TAKEN;
            case 0x2e:
                return HINT_NOT_TAKEN;
            default:
                return HINT_NONE;
        }
    }
}
