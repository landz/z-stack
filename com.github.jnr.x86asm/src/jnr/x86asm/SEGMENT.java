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
 * Segment override prefixes.
 */
public enum SEGMENT {
    SEGMENT_NONE(0), SEGMENT_CS(0x2E), SEGMENT_SS(0x36),
    SEGMENT_DS(0x3E), SEGMENT_ES(0x26), SEGMENT_FS(0x64), SEGMENT_GS(0x64);
    
    private final int prefix;

    SEGMENT(int prefix) {
        this.prefix = prefix;
    }

    public final int prefix() {
        return this.prefix;
    }
}
