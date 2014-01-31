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

public class OperandFlags {
    public static final int // x86
        O_G8          = 0x01,
        O_G16         = 0x02,
        O_G32         = 0x04,
        O_G64         = 0x08,
        O_MEM         = 0x40,
        O_IMM         = 0x80,

        O_G8_16_32_64 = O_G64  | O_G32  | O_G16  | O_G8,
        O_G16_32_64   = O_G64  | O_G32  | O_G16,
        O_G32_64      = O_G64  | O_G32,

        // x87
        O_FM_1        = 0x01,
        O_FM_2        = 0x02,
        O_FM_4        = 0x04,
        O_FM_8        = 0x08,
        O_FM_10       = 0x10,

        O_FM_2_4      = O_FM_2 | O_FM_4,
        O_FM_2_4_8    = O_FM_2 | O_FM_4 | O_FM_8,
        O_FM_4_8      = O_FM_4 | O_FM_8,
        O_FM_4_8_10   = O_FM_4 | O_FM_8 | O_FM_10,

        // mm|xmm
        O_NOREX       = 0x01, // Used by MMX/SSE instructions, O_G8 is never used for them
        O_MM          = 0x10,
        O_XMM         = 0x20,

        O_MM_MEM      = O_MM   | O_MEM,
        O_XMM_MEM     = O_XMM  | O_MEM,
        O_MM_XMM      = O_MM   | O_XMM,
        O_MM_XMM_MEM  = O_MM   | O_XMM  | O_MEM;
}
