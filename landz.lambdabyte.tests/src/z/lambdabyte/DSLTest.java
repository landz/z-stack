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

package z.lambdabyte;

import org.junit.Test;
import z.function.Function0;
import z.function.Function1;
import z.function.Function2;

import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DSLTest {

    static class Foo {

        private String foo = "foo class";

        public Foo(String foo) {
            this.foo = foo;
        }

        long check(boolean isTrue) {
            return isTrue ? 1 : -1;
        }

        long check0() {
            return -1L;
        }

        void printWith(String s) {
            System.out.println(foo+" with "+s);
        }


        public String getFoo() {
            return foo;
        }

//        Long check2() {
//            return Long.valueOf(2);
//        }
    }

    static class Bar {

        private String bar = "bar class";
        private long id = 12345;
        private static Double percent = Double.valueOf(0.1D);
        private Foo foo = new Foo("foo in Bar");

        public String someWords(String s) {
            return bar+" say "+s;
        }

        private static String someWords2(String s) {
            return "someWords2";
        }

    }

//    @Test
//    public void basicFlowForGen() {
//        LambdaByteDSL.defineClass("Foo")
//               .defineMethod("bar")
//                   .ins(NOP);
//                   .code(LambdaByteDSL::aprintln);
//
//    }

//    @Test
    public void basicFlowForReplace() {
        Foo foo = new Foo("");
//        assertThat(foo.check(false), is(-1L));
        LambdaByteDSL.INSTANCE.<Long>replace(foo::check0)
                .by(() -> System.currentTimeMillis());
        //or
        LambdaByteDSL.INSTANCE.<Foo,Long>replace(Foo::check0)
           .by((_this) -> System.currentTimeMillis());

//        LambdaByteDSL.INSTANCE_FOR_CONSUMER.<Foo, String>replace(Foo::printWith)
//                .by((Foo _this, String s) -> System.out.println() );

//        assertThat( foo.check(false)-System.currentTimeMillis(), lessThan(new Long(10000L)) );
//        assertThat( foo.check(true) -System.currentTimeMillis(), lessThan(new Long(10000L)) );
//        idea: findWriteTo, findCallTo(::)
//                .findReadto()
//                .after()
//                .ins(NOP)
//                .code(()-> System.out.println("lambdabyte!"));
    }

//    @Test
//    public void replaceNamed() {
//        Bar b = new Bar();
//        LambdaByteDSL.INSTANCE.<Bar,String,String>replace( Bar::someWords);
//        LambdaByteDSL.INSTANCE.<String,String>replace(Bar::someWords2);
//
//    }


}
