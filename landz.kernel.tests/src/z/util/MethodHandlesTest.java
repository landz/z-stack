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

package z.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static z.util.MethodHandles.*;
import static z.util.Throwables.*;
import static z.util.Unsafes.*;

import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;

public class MethodHandlesTest {

    @Test
    public void testLOOKUP() {
        assertThat(LOOKUP.toString().endsWith("trusted"), is(true));
        //XXX: we try bootstrapped Unsafe by MethodHandle way
        MethodHandle HANDLE_UNSAFE = uncheckTo(() ->
                LOOKUP.findStaticGetter(Unsafe.class, "theUnsafe", Unsafe.class));
        Unsafe unsafe = (Unsafe)uncheckTo(() -> HANDLE_UNSAFE.invoke());
        System.out.printf("UNSAFE is %s and unsafe is %s",UNSAFE,unsafe);
        assertThat(UNSAFE==unsafe, is(true));
    }
}
