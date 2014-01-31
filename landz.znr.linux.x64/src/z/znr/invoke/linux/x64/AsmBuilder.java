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

import jdk.internal.org.objectweb.asm.ClassVisitor;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 *
 */
class AsmBuilder {
    private final String classNamePath;
    private final ClassVisitor classVisitor;
    private final AsmClassLoader classLoader;

    private final ObjectNameGenerator genericObjectId = new InferringObjectNameGenerator();
    private final Map<Object, ObjectField> genericObjects = new IdentityHashMap<Object, ObjectField>();
    private final List<ObjectField> objectFields = new ArrayList<ObjectField>();

    AsmBuilder(String classNamePath, ClassVisitor classVisitor, AsmClassLoader classLoader) {
        this.classNamePath = classNamePath;
        this.classVisitor = classVisitor;
        this.classLoader = classLoader;
    }

    public String getClassNamePath() {
        return classNamePath;
    }

    ClassVisitor getClassVisitor() {
        return classVisitor;
    }

    public AsmClassLoader getClassLoader() {
        return classLoader;
    }

    private static interface ObjectNameGenerator {
        String generateName(Class cls);
    }

    private static final class SimpleObjectNameGenerator implements ObjectNameGenerator {
        private final String baseName;
        private int value;
        SimpleObjectNameGenerator(String baseName) {
            this.baseName = baseName;
            this.value = 0;
        }

        public String generateName(Class klass) {
            return baseName + "_" + ++value;
        }
    }

    private static final class InferringObjectNameGenerator implements ObjectNameGenerator {
        private final Map<Class, Long> classCount = new IdentityHashMap<>();
        public String generateName(Class klass) {
            Long count = classCount.get(klass);
            classCount.put(klass, count = count != null ? count + 1 : 1);
            return klass.getName().replace('.', '_') + '_' + count;
        }
    }

    <T> ObjectField addField(Map<T, ObjectField> map, T value, Class klass, ObjectNameGenerator objectNameGenerator) {
        ObjectField field = new ObjectField(objectNameGenerator.generateName(klass), value, klass);
        objectFields.add(field);
        map.put(value, field);
        return field;
    }

    <T> ObjectField getField(Map<T, ObjectField> map, T value, Class klass, ObjectNameGenerator objectNameGenerator) {
        ObjectField field = map.get(value);
        return field != null ? field : addField(map, value, klass, objectNameGenerator);
    }

    private static Class publicClass(Class klass) {
        for (Class c = klass; c != null; c = c.getSuperclass()) {
            if (Modifier.isPublic(c.getModifiers())) {
                return c;
            }
        }
        throw new RuntimeException("no public ancestor of " + klass);
    }

    String getObjectFieldName(Object obj, Class klass) {
        return getObjectField(obj, klass).name;
    }

    String getObjectFieldName(Object obj) {
        return getObjectField(obj).name;
    }

    ObjectField getObjectField(Object obj, Class klass) {
        return getField(genericObjects, obj, klass, genericObjectId);
    }

    ObjectField getObjectField(Object obj) {
        return getField(genericObjects, obj, publicClass(obj.getClass()), genericObjectId);
    }

    public static final class ObjectField {
        public final String name;
        public final Object value;
        public final Class klass;

        public ObjectField(String fieldName, Object fieldValue, Class fieldClass) {
            this.name = fieldName;
            this.value = fieldValue;
            this.klass = fieldClass;
        }
    }

    ObjectField[] getObjectFieldArray() {
        return objectFields.toArray(new ObjectField[objectFields.size()]);
    }


    Map<String, Object> getObjectFieldMap() {
        Map<String, Object> m = new HashMap<>();
        for (ObjectField f : objectFields) {
            m.put(f.name, f.value);
        }

        return m;
    }
}
