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

public class RID {

    private RID() {
    }
    /** ID for AX/EAX/RAX registers. */
    public static final int RID_EAX = 0;
    /** ID for CX/ECX/RCX registers. */
    public static final int RID_ECX = 1;
    /** ID for DX/EDX/RDX registers. */
    public static final int RID_EDX = 2;
    /** ID for BX/EBX/RBX registers. */
    public static final int RID_EBX = 3;
    /** ID for SP/ESP/RSP registers. */
    public static final int RID_ESP = 4;
    /** ID for BP/EBP/RBP registers. */
    public static final int RID_EBP = 5;
    /** ID for SI/ESI/RSI registers. */
    public static final int RID_ESI = 6;
    /** ID for DI/EDI/RDI registers. */
    public static final int RID_EDI = 7;
}
