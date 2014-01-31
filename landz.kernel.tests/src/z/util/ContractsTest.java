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

import static z.util.Contracts.*;
import static z.util.Unsafes.*;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Field;


public class ContractsTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testContract() {
        String s = "something";
        contract(() -> s.contains("some"));
        contract(() -> s.startsWith("some"));

        exception.expect(IllegalStateException.class);
        contract(() -> s=="");
    }

    @Ignore("do we handle the exception")
    @Test
    public void testContractWithException() {
        exception.expect(IllegalStateException.class);
        contract(() -> {throw new RuntimeException();});
    }

    @Test
    public void testENABLEFlag() {
        contract(()->false);
    }

}
