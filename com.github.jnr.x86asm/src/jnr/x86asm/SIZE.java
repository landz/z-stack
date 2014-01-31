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

/** Size of registers and pointers */
public class SIZE {
    /** 1 byte size. */
    public static final int SIZE_BYTE = 1;
    /** 2 bytes size. */
    public static final int SIZE_WORD = 2;
    /** 4 bytes size. */
    public static final int SIZE_DWORD= 4;
    /** 8 bytes size. */
    public static final int SIZE_QWORD = 8;
    /** 10 bytes size. */
    public static final int SIZE_TWORD = 10;
    /** 16 bytes size. */
    public static final int SIZE_DQWORD = 16;

    private SIZE() {}
}
