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

import z.function.*;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * LambdaByte's DSL enabled by fluent interface style
 */
public class LambdaByteDSL {

    public static class ByOp0<R> {
        Function0<R> oldImpl;
        Function0<R> newImpl;

        public ByOp0(Function0<R> oldImpl) {
            this.oldImpl = oldImpl;
        }

        public void by(Function0<R> newImpl) {
            this.newImpl = newImpl;
        }
    }

    public static class ByOp1<T, R> {
        Function1<T, R> oldImpl;
        Function1<T, R> newImpl;

        public ByOp1(Function1<T, R> oldImpl) {
            this.oldImpl = oldImpl;
        }

        public void by(Function1<T, R> newImpl) {
            this.newImpl = newImpl;
        }
    }

    public static class ByOp2<T1, T2, R> {
        Function2<T1, T2, R> oldImpl;
        Function2<T1, T2, R> newImpl;

        public ByOp2(Function2<T1, T2, R> oldImpl) {
            this.oldImpl = oldImpl;
        }

        public void by(Function2<T1, T2, R> newImpl) {
            this.newImpl = newImpl;
        }
    }

    public interface ReplaceOp {

        public <R> ByOp0<R> replace(Function0<R> methodRef);

        public <T,R> ByOp1<T,R> replace(Function1<T, R> methodRef);

        public <T1, T2, R> ByOp2<T1, T2, R> replace(Function2<T1, T2, R> methodRef);

    }

    private static class DSL implements ReplaceOp {

        @Override
        public <R> ByOp0<R> replace(Function0<R> methodRef) {
            return new ByOp0<>(methodRef);
        }

        @Override
        public <T,R> ByOp1<T, R> replace(Function1<T, R> methodRef) {
            return new ByOp1<>(methodRef);
        }

        @Override
        public <T1, T2, R> ByOp2<T1, T2, R> replace(Function2<T1, T2, R> methodRef) {
            return new ByOp2<>(methodRef);
        }

    }


    public static class ByOpForConsumer1<T> {
        Consumer<T> oldImpl;
        Consumer<T> newImpl;

        public ByOpForConsumer1(Consumer<T> oldImpl) {
            this.oldImpl = oldImpl;
        }

        public void by(Consumer<T> newImpl) {
            this.newImpl = newImpl;
        }
    }

    public static class ByOpForConsumer2<T1, T2> {
        BiConsumer<T1, T2> oldImpl;
        BiConsumer<T1, T2> newImpl;

        public ByOpForConsumer2(BiConsumer<T1, T2> oldImpl) {
            this.oldImpl = oldImpl;
        }

        public void by(BiConsumer<T1, T2> newImpl) {
            this.newImpl = newImpl;
        }
    }

    public static interface ReplaceOpForConsumer {

        public <T> ByOpForConsumer1<T> replace(Consumer<T> methodRef);

        public <T1, T2> ByOpForConsumer2<T1, T2> replace(BiConsumer<T1, T2> methodRef);

    }

    private static class DSLForConsumer implements ReplaceOpForConsumer {

        @Override
        public <T> ByOpForConsumer1 replace(Consumer<T> methodRef) {
            return new ByOpForConsumer1(methodRef);
        }

        @Override
        public <T1, T2> ByOpForConsumer2<T1, T2> replace(BiConsumer<T1, T2> methodRef) {
            return new ByOpForConsumer2(methodRef);
        }

    }


    public static final ReplaceOp INSTANCE = new DSL();
    public static final ReplaceOpForConsumer INSTANCE_FOR_CONSUMER = new DSLForConsumer();

    public static final ReplaceOp LambdaByte() {
        return INSTANCE;
    }

    public static final ReplaceOpForConsumer LambdaByteForConsumer() {
        return INSTANCE_FOR_CONSUMER;
    }

//    @Override
//    public <T> ByOp replace(Consumer<T> methodRef) {
//        return this;
//    }
//
//    @Override
//    public <T> void by(Consumer<T> newImpl) {
//    }

//    public static void replace(Function3 old) {
//
//    }
//
//    public static void replace(Function4 old) {
//
//    }
//
//    public static void replace(Function5 old) {
//
//    }



}
