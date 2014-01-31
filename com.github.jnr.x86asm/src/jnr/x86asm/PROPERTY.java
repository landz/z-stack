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
 * Assembler/Compiler properties.
 */
public final class PROPERTY {
  /**
   * Optimize align for current processor.
   *
   * Default: @c true.
   */
  public static final int PROPERTY_OPTIMIZE_ALIGN = 0;

  /**
   * Force rex prefix emitting.
   *
   * Default: @c false.
   *
   * @note This is X86/X86 property only.
   */
  public static final int PROPERTY_X86_FORCE_REX = 1;

  /**
   * Emit hints added to jcc() instructions.
   *
   * Default: @c true.
   *
   * @note This is X86/X86 property only.
   */
  public static final int PROPERTY_X86_JCC_HINTS = 2;
}
