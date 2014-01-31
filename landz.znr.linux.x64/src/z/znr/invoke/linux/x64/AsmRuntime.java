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

package z.znr.invoke.linux.x64;

import com.kenai.jffi.CallContext;
import com.kenai.jffi.HeapInvocationBuffer;
import com.kenai.jffi.MemoryIO;

import java.nio.Buffer;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility methods that are used at runtime by generated code.
 */
public final class AsmRuntime {
    private AsmRuntime() {}

    private static final Map<String, Map<String, Object>> staticClassDataMap = new ConcurrentHashMap<String, Map<String, Object>>();

    public static Map<String, Object> getStaticClassData(String classID) {
        Map<String, Object> m = staticClassDataMap.get(classID);
        if (m != null) {
            staticClassDataMap.remove(classID);
            return m;
        }
        return Collections.emptyMap();
    }

    public static void removeStaticClassData(String classID) {
        staticClassDataMap.remove(classID);
    }

    static void setStaticClassData(String classID, Map<String, Object> staticClassData) {
        staticClassDataMap.put(classID, staticClassData);
    }

    public static UnsatisfiedLinkError newUnsatisifiedLinkError(String msg) {
        return new UnsatisfiedLinkError(msg);
    }

    public static HeapInvocationBuffer newHeapInvocationBuffer(com.kenai.jffi.Function function) {
        return new HeapInvocationBuffer(function);
    }

    public static HeapInvocationBuffer newHeapInvocationBuffer(CallContext callContext) {
        return new HeapInvocationBuffer(callContext);
    }

    public static HeapInvocationBuffer newHeapInvocationBuffer(CallContext callContext, int objCount) {
        return new HeapInvocationBuffer(callContext, objCount);
    }

    public static long longValue(Buffer ptr) {
        return ptr != null && ptr.isDirect() ? MemoryIO.getInstance().getDirectBufferAddress(ptr) : 0L;
    }

    public static int intValue(Buffer ptr) {
        return ptr != null && ptr.isDirect()  ? (int) MemoryIO.getInstance().getDirectBufferAddress(ptr) : 0;
    }

    public static int s8(int v) {
        return (byte) v;
    }

    public static long s8(long v) {
        return (byte) v;
    }

    public static int u8(int v) {
        return v & 0xFF;
    }

    public static long u8(long v) {
        return v & 0xFFL;
    }

    public static int s16(int v) {
        return (short) v;
    }

    public static long s16(long v) {
        return (short) v;
    }

    public static int u16(int v) {
        return v & 0xFFFF;
    }

    public static long u16(long v) {
        return v & 0xFFFFL;
    }

    public static int s32(int v) {
        return v;
    }

    public static long s32(long v) {
        return (int) v;
    }

    public static int u32(int v) {
        return v;
    }

    public static long u32(long v) {
        return v & 0xFFFFFFFFL;
    }

    public static boolean notNull(Object object) {
        return object != null;
    }

    public static boolean isNull(Object object) {
        return object == null;
    }

    public static boolean isDirect(ObjectParameterStrategy strategy) {
        return strategy.isDirect();
    }

    public static boolean isTrue(boolean p1) {
        return p1;
    }

    public static boolean isTrue(boolean p1, boolean p2) {
        return p1 & p2;
    }

    public static boolean isTrue(boolean p1, boolean p2, boolean p3) {
        return p1 & p2 & p3;
    }

    public static boolean isTrue(boolean p1, boolean p2, boolean p3, boolean p4) {
        return p1 & p2 & p3 & p4;
    }

    public static boolean isTrue(boolean p1, boolean p2, boolean p3, boolean p4, boolean p5) {
        return p1 & p2 & p3 & p4 & p5;
    }

    public static boolean isTrue(boolean p1, boolean p2, boolean p3, boolean p4, boolean p5, boolean p6) {
        return p1 & p2 & p3 & p4 & p5 & p5 & p6;
    }
}
