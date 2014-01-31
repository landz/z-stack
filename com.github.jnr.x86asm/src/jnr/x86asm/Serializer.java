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
 * Assembler instruction serializer.
 *
 * Serializer functionality is implemented in {@link SerializerCore} class
 * and serializer intrinsics are implemented in {@link SerializerIntrinsics} class.
 *
 * Always use this class and never use {@link SerializerCore} or {@link SerializerIntrinsics}
 * classes directly.
 */
public abstract class Serializer extends SerializerIntrinsics {

}
