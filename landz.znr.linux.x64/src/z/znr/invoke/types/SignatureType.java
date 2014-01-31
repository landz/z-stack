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

package z.znr.invoke.types;

public abstract class SignatureType {
    private final NativeType nativeType;
    private final Class javaType;
    protected final com.kenai.jffi.Type jffiType;


    SignatureType(NativeType nativeType, Class javaType, com.kenai.jffi.Type jffiType) {
        this.nativeType = nativeType;
        this.javaType = javaType;
        this.jffiType = jffiType;
    }

    public int size() {
        return jffiType().size();
    }

    public int alignment() {
        return jffiType().alignment();
    }

    public NativeType nativeType() {
        return nativeType;
    }

    public Class javaType() {
        return javaType;
    }

    com.kenai.jffi.Type jffiType() {
        return jffiType;
    }
}
