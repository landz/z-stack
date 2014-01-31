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

package z.function;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.function.BooleanSupplier;

import static org.hamcrest.Matchers.lessThan;
import static z.function.Functions.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FunctionTypeParameterArityTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void convertWithRTFunctions() {
        ToBooleanFunction0 tbf = () -> true;
        assertThat(checkLambda(()-> tbf.apply()), is(true));
        assertThat(checkLambda(toBooleanSupplier(tbf)), is(true));

        exception.expect(ClassCastException.class);
        boolean b = checkLambda((BooleanSupplier) tbf);
    }

    static boolean checkLambda(BooleanSupplier bs) {
       return bs.getAsBoolean();
    }

    @Test
    public void primitiveToObjectParameter() {
       ToFloatFunction0 f = ()-> 3.14159f;
       assertThat(f.apply()-3.14159f, lessThan(Float.MIN_VALUE));
    }


}
