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

import com.kenai.jffi.ObjectParameterType;

abstract public class ObjectParameterStrategy extends com.kenai.jffi.ObjectParameterStrategy {
    protected static enum StrategyType { DIRECT, HEAP }
    protected static final StrategyType DIRECT = StrategyType.DIRECT;
    protected static final StrategyType HEAP = StrategyType.HEAP;

    /* objectCount is accessed directly from asm code - do not change */
    public final int objectCount;

    protected ObjectParameterStrategy(StrategyType strategyType) {
        super(jffiStrategyType(strategyType));
        objectCount = objectCount(strategyType);
    }

    ObjectParameterStrategy(StrategyType strategyType, ObjectParameterType parameterType) {
        super(jffiStrategyType(strategyType), parameterType);
        objectCount = objectCount(strategyType);
    }

    private static com.kenai.jffi.ObjectParameterStrategy.StrategyType jffiStrategyType(StrategyType strategyType) {
        return strategyType == DIRECT ? com.kenai.jffi.ObjectParameterStrategy.DIRECT : com.kenai.jffi.ObjectParameterStrategy.HEAP;
    }

    private static int objectCount(StrategyType strategyType) {
        return strategyType == HEAP ? 1 : 0;
    }

    abstract public long address(Object parameter);

    abstract public Object object(Object parameter);
    abstract public int offset(Object parameter);
    abstract public int length(Object parameter);
}
