/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.velocypack;

import com.arangodb.velocypack.VPackBuilder.BuilderOptions;
import com.arangodb.velocypack.annotations.Expose;
import com.arangodb.velocypack.annotations.SerializedName;
import com.arangodb.velocypack.annotations.VPackPOJOBuilder;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocypack.exception.VPackParserException;
import com.arangodb.velocypack.internal.*;
import com.arangodb.velocypack.internal.VPackBuilderUtils.BuilderInfo;
import com.arangodb.velocypack.internal.VPackCache.FieldInfo;
import com.arangodb.velocypack.internal.VPackCreatorMethodUtils.ParameterInfo;
import com.arangodb.velocypack.internal.VPackCreatorMethodUtils.VPackCreatorMethodInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author Mark Vollmary
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class VPack {

    private static final String ATTR_KEY = "key";
    private static final String ATTR_VALUE = "value";
    private static final String DEFAULT_TYPE_KEY = "_class";
    private static final boolean DEFAULT_USE_TYPE_HINT = true;

    private final Map<Type, VPackSerializer<?>> serializers;
    private final Map<Type, VPackSerializer<?>> enclosingSerializers;
    private final Map<Type, VPackDeserializer<?>> deserializers;
    private final Map<Type, VPackDeserializer<?>> deserializersWithSelfNullHandle;
    private final Map<String, Map<Type, VPackDeserializer<?>>> deserializersByName;
    private final Map<String, Map<Type, VPackDeserializer<?>>> deserializersByNameWithSelfNullHandle;
    private final Map<Type, VPackInstanceCreator<?>> instanceCreators;
    private final Map<Type, VPackKeyMapAdapter<?>> keyMapAdapters;

    private final BuilderOptions builderOptions;
    private final VPackCache cache;
    private final VPackSerializationContext serializationContext;
    private final VPackDeserializationContext deserializationContext;
    private final boolean serializeNullValues;
    private final String typeKey;
    private final boolean useTypeHint;
    private final VPackBuilderUtils builderUtils;
    private final VPackCreatorMethodUtils vPackCreatorMethodUtils;

    public static class Builder implements VPackSetupContext<Builder> {
        private final Map<Type, VPackSerializer<?>> serializers;
        private final Map<Type, VPackSerializer<?>> enclosingSerializers;
        private final Map<Type, VPackDeserializer<?>> deserializers;
        private final Map<Type, VPackDeserializer<?>> deserializersWithSelfNullHandle;
        private final Map<String, Map<Type, VPackDeserializer<?>>> deserializersByName;
        private final Map<String, Map<Type, VPackDeserializer<?>>> deserializersByNameWithSelfNullHandle;
        private final Map<Type, VPackInstanceCreator<?>> instanceCreators;
        private final BuilderOptions builderOptions;
        private boolean serializeNullValues;
        private VPackFieldNamingStrategy fieldNamingStrategy;
        private final Map<Class<? extends Annotation>, VPackAnnotationFieldFilter<? extends Annotation>> annotationFieldFilter;
        private final Map<Class<? extends Annotation>, VPackAnnotationFieldNaming<? extends Annotation>> annotationFieldNaming;
        private final Map<Type, VPackKeyMapAdapter<?>> keyMapAdapters;
        private String typeKey;
        private Boolean useTypeHint;

        public Builder() {
            super();
            serializers = new HashMap<>();
            enclosingSerializers = new HashMap<>();
            deserializers = new HashMap<>();
            deserializersWithSelfNullHandle = new HashMap<>();
            deserializersByName = new HashMap<>();
            deserializersByNameWithSelfNullHandle = new HashMap<>();
            instanceCreators = new HashMap<>();
            builderOptions = new DefaultVPackBuilderOptions();
            serializeNullValues = false;
            annotationFieldFilter = new HashMap<>();
            annotationFieldNaming = new HashMap<>();
            keyMapAdapters = new HashMap<>();
            typeKey = null;
            useTypeHint = null;

            instanceCreators.put(Iterable.class, VPackInstanceCreators.ITERABLE);
            instanceCreators.put(Collection.class, VPackInstanceCreators.COLLECTION);
            instanceCreators.put(List.class, VPackInstanceCreators.LIST);
            instanceCreators.put(Set.class, VPackInstanceCreators.SET);
            instanceCreators.put(Map.class, VPackInstanceCreators.MAP);

            serializers.put(String.class, VPackSerializers.STRING);
            serializers.put(Boolean.class, VPackSerializers.BOOLEAN);
            serializers.put(boolean.class, VPackSerializers.BOOLEAN);
            serializers.put(Integer.class, VPackSerializers.INTEGER);
            serializers.put(int.class, VPackSerializers.INTEGER);
            serializers.put(Long.class, VPackSerializers.LONG);
            serializers.put(long.class, VPackSerializers.LONG);
            serializers.put(Short.class, VPackSerializers.SHORT);
            serializers.put(short.class, VPackSerializers.SHORT);
            serializers.put(Double.class, VPackSerializers.DOUBLE);
            serializers.put(double.class, VPackSerializers.DOUBLE);
            serializers.put(Float.class, VPackSerializers.FLOAT);
            serializers.put(float.class, VPackSerializers.FLOAT);
            serializers.put(BigInteger.class, VPackSerializers.BIG_INTEGER);
            serializers.put(BigDecimal.class, VPackSerializers.BIG_DECIMAL);
            serializers.put(Number.class, VPackSerializers.NUMBER);
            serializers.put(Character.class, VPackSerializers.CHARACTER);
            serializers.put(char.class, VPackSerializers.CHARACTER);
            serializers.put(Date.class, VPackSerializers.DATE);
            serializers.put(java.sql.Date.class, VPackSerializers.SQL_DATE);
            serializers.put(java.sql.Timestamp.class, VPackSerializers.SQL_TIMESTAMP);
            serializers.put(VPackSlice.class, VPackSerializers.VPACK);
            serializers.put(UUID.class, VPackSerializers.UUID);
            serializers.put(byte[].class, VPackSerializers.BYTE_ARRAY);
            serializers.put(Byte.class, VPackSerializers.BYTE);
            serializers.put(byte.class, VPackSerializers.BYTE);

            deserializers.put(String.class, VPackDeserializers.STRING);
            deserializers.put(Boolean.class, VPackDeserializers.BOOLEAN);
            deserializers.put(boolean.class, VPackDeserializers.BOOLEAN);
            deserializers.put(Integer.class, VPackDeserializers.INTEGER);
            deserializers.put(int.class, VPackDeserializers.INTEGER);
            deserializers.put(Long.class, VPackDeserializers.LONG);
            deserializers.put(long.class, VPackDeserializers.LONG);
            deserializers.put(Short.class, VPackDeserializers.SHORT);
            deserializers.put(short.class, VPackDeserializers.SHORT);
            deserializers.put(Double.class, VPackDeserializers.DOUBLE);
            deserializers.put(double.class, VPackDeserializers.DOUBLE);
            deserializers.put(Float.class, VPackDeserializers.FLOAT);
            deserializers.put(float.class, VPackDeserializers.FLOAT);
            deserializers.put(BigInteger.class, VPackDeserializers.BIG_INTEGER);
            deserializers.put(BigDecimal.class, VPackDeserializers.BIG_DECIMAL);
            deserializers.put(Number.class, VPackDeserializers.NUMBER);
            deserializers.put(Character.class, VPackDeserializers.CHARACTER);
            deserializers.put(char.class, VPackDeserializers.CHARACTER);
            deserializers.put(Date.class, VPackDeserializers.DATE);
            deserializers.put(java.sql.Date.class, VPackDeserializers.SQL_DATE);
            deserializers.put(java.sql.Timestamp.class, VPackDeserializers.SQL_TIMESTAMP);
            deserializers.put(VPackSlice.class, VPackDeserializers.VPACK);
            deserializers.put(UUID.class, VPackDeserializers.UUID);
            deserializers.put(byte[].class, VPackDeserializers.BYTE_ARRAY);
            deserializers.put(Byte.class, VPackDeserializers.BYTE);
            deserializers.put(byte.class, VPackDeserializers.BYTE);

            annotationFieldFilter.put(Expose.class, new VPackAnnotationFieldFilter<Expose>() {
                @Override
                public boolean serialize(final Expose annotation) {
                    return annotation.serialize();
                }

                @Override
                public boolean deserialize(final Expose annotation) {
                    return annotation.deserialize();
                }
            });
            annotationFieldNaming.put(SerializedName.class, new VPackAnnotationFieldNaming<SerializedName>() {
                @Override
                public String name(final SerializedName annotation) {
                    return annotation.value();
                }
            });
        }

        @Override
        public <T> VPack.Builder registerSerializer(final Type type, final VPackSerializer<T> serializer) {
            serializers.put(type, serializer);
            return this;
        }

        @Override
        public <T> VPack.Builder registerEnclosingSerializer(final Type type, final VPackSerializer<T> serializer) {
            enclosingSerializers.put(type, serializer);
            return this;
        }

        @Override
        public <T> VPack.Builder registerDeserializer(final Type type, final VPackDeserializer<T> deserializer) {
            return registerDeserializer(type, deserializer, false);
        }

        @Override
        public <T> Builder registerDeserializer(
                final Type type,
                final VPackDeserializer<T> deserializer,
                final boolean includeNullValues) {
            if (includeNullValues) {
                deserializersWithSelfNullHandle.put(type, deserializer);
            }
            deserializers.put(type, deserializer);
            return this;
        }

        @Override
        public <T> VPack.Builder registerDeserializer(
                final String fieldName,
                final Type type,
                final VPackDeserializer<T> deserializer) {
            return registerDeserializer(fieldName, type, deserializer, false);
        }

        @Override
        public <T> Builder registerDeserializer(
                final String fieldName,
                final Type type,
                final VPackDeserializer<T> deserializer,
                final boolean includeNullValues) {
            if (includeNullValues) {
                Map<Type, VPackDeserializer<?>> byName = deserializersByNameWithSelfNullHandle.get(fieldName);
                if (byName == null) {
                    byName = new HashMap<>();
                    deserializersByNameWithSelfNullHandle.put(fieldName, byName);
                }
                byName.put(type, deserializer);
            }
            Map<Type, VPackDeserializer<?>> byName = deserializersByName.get(fieldName);
            if (byName == null) {
                byName = new HashMap<>();
                deserializersByName.put(fieldName, byName);
            }
            byName.put(type, deserializer);
            return this;
        }

        @Override
        public <T> VPack.Builder registerInstanceCreator(final Type type, final VPackInstanceCreator<T> creator) {
            instanceCreators.put(type, creator);
            return this;
        }

        @Override
        public VPack.Builder buildUnindexedArrays(final boolean buildUnindexedArrays) {
            builderOptions.setBuildUnindexedArrays(buildUnindexedArrays);
            return this;
        }

        @Override
        public VPack.Builder buildUnindexedObjects(final boolean buildUnindexedObjects) {
            builderOptions.setBuildUnindexedObjects(buildUnindexedObjects);
            return this;
        }

        @Override
        public VPack.Builder serializeNullValues(final boolean serializeNullValues) {
            this.serializeNullValues = serializeNullValues;
            return this;
        }

        @Override
        public VPack.Builder fieldNamingStrategy(final VPackFieldNamingStrategy fieldNamingStrategy) {
            this.fieldNamingStrategy = fieldNamingStrategy;
            return this;
        }

        @Override
        public <A extends Annotation> VPack.Builder annotationFieldFilter(
                final Class<A> type,
                final VPackAnnotationFieldFilter<A> fieldFilter) {
            annotationFieldFilter.put(type, fieldFilter);
            return this;
        }

        @Override
        public <A extends Annotation> VPack.Builder annotationFieldNaming(
                final Class<A> type,
                final VPackAnnotationFieldNaming<A> fieldNaming) {
            annotationFieldNaming.put(type, fieldNaming);
            return this;
        }

        @Override
        public Builder registerModule(final VPackModule module) {
            module.setup(VPack.Builder.this);
            return this;
        }

        @Override
        public Builder registerModules(final VPackModule... modules) {
            for (final VPackModule module : modules) {
                registerModule(module);
            }
            return this;
        }

        /**
         * Register an adapter to convert keys in {@link java.util.Map} which are not from type
         * {@link java.lang.String}.
         *
         * @param type    the type the adapter should used for
         * @param adapter the adapter
         * @return {@link VPack.Builder}
         */
        @Override
        public Builder registerKeyMapAdapter(final Type type, final VPackKeyMapAdapter<?> adapter) {
            keyMapAdapters.put(type, adapter);
            return this;
        }

        /**
         * Set the name of the serialized field with the type information
         *
         * @param typeKey Name of the field with type information
         * @return {@link VPack.Builder}
         */
        public Builder typeKey(final String typeKey) {
            this.typeKey = typeKey;
            return this;
        }

        /**
         * Enables storing type information of the serialized object
         *
         * @param useTypeHint (default: {@code true})
         * @return {@link VPack.Builder}
         */
        public Builder useTypeHint(final boolean useTypeHint) {
            this.useTypeHint = useTypeHint;
            return this;
        }

        public synchronized VPack build() {
            return new VPack(new HashMap<>(serializers), new HashMap<>(enclosingSerializers), new HashMap<>(deserializers),
                    new HashMap<>(deserializersWithSelfNullHandle),
                    new HashMap<>(instanceCreators), builderOptions, serializeNullValues,
                    fieldNamingStrategy, new HashMap<>(deserializersByName),
                    new HashMap<>(deserializersByNameWithSelfNullHandle),
                    new HashMap<>(annotationFieldFilter), new HashMap<>(annotationFieldNaming),
                    keyMapAdapters,
                    typeKey != null ? typeKey : DEFAULT_TYPE_KEY,
                    useTypeHint != null ? useTypeHint : DEFAULT_USE_TYPE_HINT
            );
        }

    }

    private VPack(final Map<Type, VPackSerializer<?>> serializers,
                  final Map<Type, VPackSerializer<?>> enclosingSerializers, final Map<Type, VPackDeserializer<?>> deserializers,
                  final Map<Type, VPackDeserializer<?>> deserializersWithSelfNullHandle,
                  final Map<Type, VPackInstanceCreator<?>> instanceCreators, final BuilderOptions builderOptions,
                  final boolean serializeNullValues, final VPackFieldNamingStrategy fieldNamingStrategy,
                  final Map<String, Map<Type, VPackDeserializer<?>>> deserializersByName,
                  final Map<String, Map<Type, VPackDeserializer<?>>> deserializersByNameWithSelfNullHandle,
                  final Map<Class<? extends Annotation>, VPackAnnotationFieldFilter<? extends Annotation>> annotationFieldFilter,
                  final Map<Class<? extends Annotation>, VPackAnnotationFieldNaming<? extends Annotation>> annotationFieldNaming,
                  final Map<Type, VPackKeyMapAdapter<?>> keyMapAdapters, final String typeKey, final boolean useTypeHint) {
        super();
        this.serializers = serializers;
        this.enclosingSerializers = enclosingSerializers;
        this.deserializers = deserializers;
        this.deserializersWithSelfNullHandle = deserializersWithSelfNullHandle;
        this.instanceCreators = instanceCreators;
        this.builderOptions = builderOptions;
        this.serializeNullValues = serializeNullValues;
        this.deserializersByName = deserializersByName;
        this.deserializersByNameWithSelfNullHandle = deserializersByNameWithSelfNullHandle;
        this.keyMapAdapters = keyMapAdapters;
        this.typeKey = typeKey;
        this.useTypeHint = useTypeHint;

        builderUtils = new VPackBuilderUtils();
        vPackCreatorMethodUtils = new VPackCreatorMethodUtils();
        cache = new VPackCache(fieldNamingStrategy, annotationFieldFilter, annotationFieldNaming);
        serializationContext = new VPackSerializationContext() {
            @Override
            public void serialize(final VPackBuilder builder, final String attribute, final Object entity)
                    throws VPackParserException {
                VPack.this.serialize(attribute, entity, entity != null ? entity.getClass() : null, builder,
                        Collections.<String, Object>emptyMap());
            }
        };
        deserializationContext = new VPackDeserializationContext() {
            @Override
            public <T> T deserialize(final VPackSlice vpack, final Type type) throws VPackParserException {
                return VPack.this.deserialize(vpack, type);
            }
        };
        keyMapAdapters.put(String.class, VPackKeyMapAdapters.STRING);
        keyMapAdapters.put(Boolean.class, VPackKeyMapAdapters.BOOLEAN);
        keyMapAdapters.put(Integer.class, VPackKeyMapAdapters.INTEGER);
        keyMapAdapters.put(Long.class, VPackKeyMapAdapters.LONG);
        keyMapAdapters.put(Short.class, VPackKeyMapAdapters.SHORT);
        keyMapAdapters.put(Double.class, VPackKeyMapAdapters.DOUBLE);
        keyMapAdapters.put(Float.class, VPackKeyMapAdapters.FLOAT);
        keyMapAdapters.put(BigInteger.class, VPackKeyMapAdapters.BIG_INTEGER);
        keyMapAdapters.put(BigDecimal.class, VPackKeyMapAdapters.BIG_DECIMAL);
        keyMapAdapters.put(Number.class, VPackKeyMapAdapters.NUMBER);
        keyMapAdapters.put(Character.class, VPackKeyMapAdapters.CHARACTER);
    }

    public <T> T deserialize(final VPackSlice vpack, final Type type) throws VPackParserException {
        if (type == VPackSlice.class) {
            return (T) vpack;
        }
        final T entity;
        try {
            entity = (T) getValue(null, vpack, type, null);
        } catch (final Exception e) {
            throw new VPackParserException(e);
        }
        return entity;
    }

    private VPackDeserializer<?> getDeserializer(final String fieldName, final Type type) {
        return getDeserializer(fieldName, type, deserializers, deserializersByName);
    }

    private VPackDeserializer<?> getDeserializerWithSelfNullHandle(final String fieldName, final Type type) {
        return getDeserializer(fieldName, type, deserializersWithSelfNullHandle, deserializersByNameWithSelfNullHandle);
    }

    private VPackDeserializer<?> getDeserializer(
            final String fieldName,
            final Type type,
            final Map<Type, VPackDeserializer<?>> deserializers,
            final Map<String, Map<Type, VPackDeserializer<?>>> deserializersByName) {
        VPackDeserializer<?> deserializer = null;
        final Map<Type, VPackDeserializer<?>> byName = deserializersByName.get(fieldName);
        if (byName != null) {
            deserializer = byName.get(type);
        }
        if (deserializer == null) {
            deserializer = deserializers.get(type);
        }
        if (deserializer == null && ParameterizedType.class.isAssignableFrom(type.getClass())) {
            deserializer = getDeserializer(fieldName, ((ParameterizedType) type).getRawType(), deserializers,
                    deserializersByName);
        }
        return deserializer;
    }

    private Method getBuildMethod(Class<?> clazz, String name) throws NoSuchMethodException {
        Method build = clazz.getDeclaredMethod(name);
        if (Modifier.isStatic(build.getModifiers()))
            throw new NoSuchMethodException("Cannot find build method: " + clazz.getName() + "." + name + "() ");
        return build;
    }

    private <T> T deserializeObject(
            final VPackSlice parent, final VPackSlice vpack, final Type type, final String fieldName)
            throws ReflectiveOperationException, VPackException {
        return deserializeObject(parent, vpack, type, fieldName, null);
    }

    private <T> T deserializeObject(
            final VPackSlice parent,
            final VPackSlice vpack,
            final Type type,
            final String fieldName,
            final AnnotatedElement referencingElement) throws ReflectiveOperationException, VPackException {
        final T entity;

        final VPackDeserializer<?> deserializer = getDeserializer(fieldName, type);
        final BuilderInfo builderInfo = builderUtils.getBuilderInfo(type, referencingElement);
        final VPackCreatorMethodInfo factoryMethodInfo = vPackCreatorMethodUtils.getCreatorMethodInfo(type);

        if (builderInfo != null) {
            Object builder = builderInfo.createBuilder();
            deserializeBuilder(builder, vpack, builderInfo.annotation);
            Method build = getBuildMethod(builder.getClass(), builderInfo.annotation.buildMethodName);
            entity = (T) build.invoke(builder);
        } else if (factoryMethodInfo != null) {
            entity = (T) deserializeFactoryMethod(factoryMethodInfo, vpack);
        } else if (deserializer != null) {
            if (VPackDeserializerParameterizedType.class.isAssignableFrom(deserializer.getClass())
                    && ParameterizedType.class.isAssignableFrom(type.getClass())) {
                entity = ((VPackDeserializerParameterizedType<T>) deserializer)
                        .deserialize(parent, vpack, deserializationContext, (ParameterizedType) type);
            } else {
                entity = ((VPackDeserializer<T>) deserializer).deserialize(parent, vpack, deserializationContext);
            }
        } else if (type == Object.class) {
            entity = (T) getValue(parent, vpack, getType(vpack), fieldName);
        } else {
            entity = createInstance(type);
            deserializeFields(entity, vpack);
        }
        return entity;
    }

    private Type determineType(final VPackSlice vpack, final Type type) {
        if (!useTypeHint || !vpack.isObject()) {
            return type;
        }
        final VPackSlice clazz = vpack.get(typeKey);
        try {
            return clazz.isString() ? Class.forName(clazz.getAsString()) : type;
        } catch (final ClassNotFoundException e) {
            throw new VPackParserException(e);
        }
    }

    private Type getType(final VPackSlice vpack) {
        final Type type;
        if (vpack.isObject()) {
            type = Map.class;
        } else if (vpack.isString()) {
            type = String.class;
        } else if (vpack.isBoolean()) {
            type = Boolean.class;
        } else if (vpack.isArray()) {
            type = Iterable.class;
        } else if (vpack.isDate()) {
            type = Date.class;
        } else if (vpack.isDouble()) {
            type = Double.class;
        } else if (vpack.isNumber()) {
            type = Number.class;
        } else if (vpack.isCustom()) {
            type = String.class;
        } else {
            type = null;
        }
        return type;
    }

    private <T> T createInstance(final Type type) throws ReflectiveOperationException {
        final T entity;
        final VPackInstanceCreator<?> creator = instanceCreators.get(type);
        if (creator != null) {
            entity = (T) creator.createInstance();
        } else if (type instanceof ParameterizedType) {
            entity = createInstance(((ParameterizedType) type).getRawType());
        } else {
            entity = ((Class<T>) type).newInstance();
        }
        return entity;
    }

    private void deserializeFields(final Object entity, final VPackSlice vpack)
            throws ReflectiveOperationException, VPackException {
        final Map<String, FieldInfo> fields = cache.getFields(entity.getClass());
        for (final Iterator<Entry<String, VPackSlice>> iterator = vpack.objectIterator(); iterator.hasNext(); ) {
            final Entry<String, VPackSlice> next = iterator.next();
            final FieldInfo fieldInfo = fields.get(next.getKey());
            if (fieldInfo != null && fieldInfo.isDeserialize()) {
                deserializeField(vpack, next.getValue(), entity, fieldInfo);
            }
        }
    }

    private Object deserializeFactoryMethod(final VPackCreatorMethodInfo factoryMethodInfo, final VPackSlice vpack)
            throws ReflectiveOperationException, VPackException {
        LinkedHashMap<String, ParameterInfo> parameters = cache.getParameters(factoryMethodInfo.getExecutable());
        Map<String, Object> parameterValuesMap = new HashMap<>();

        for (final Iterator<Entry<String, VPackSlice>> iterator = vpack.objectIterator(); iterator.hasNext(); ) {
            final Entry<String, VPackSlice> next = iterator.next();
            final ParameterInfo parameterInfo = parameters.get(next.getKey());
            parameterValuesMap.put(parameterInfo.name,
                    getValue(vpack, next.getValue(), parameterInfo.type, parameterInfo.name,
                            parameterInfo.referencingElement));
        }

        Object[] parameterValues = new Object[parameters.size()];
        int i = 0;
        for (String key : parameters.keySet()) {
            parameterValues[i++] = parameterValuesMap.get(key);
        }

        return factoryMethodInfo.create(parameterValues);
    }

    private void deserializeBuilder(
            final Object builder, final VPackSlice vpack, VPackPOJOBuilder.Value annotation)
            throws ReflectiveOperationException, VPackException {
        final Map<String, FieldInfo> setters = cache.getBuiderSetters(builder.getClass(), annotation.withSetterPrefix);
        for (final Iterator<Entry<String, VPackSlice>> iterator = vpack.objectIterator(); iterator.hasNext(); ) {
            final Entry<String, VPackSlice> next = iterator.next();
            final FieldInfo fieldInfo = setters.get(next.getKey());
            if (fieldInfo != null && fieldInfo.isDeserialize()) {
                deserializeField(vpack, next.getValue(), builder, fieldInfo);
            }
        }
    }

    private void deserializeField(
            final VPackSlice parent, final VPackSlice vpack, final Object entity, final FieldInfo fieldInfo)
            throws ReflectiveOperationException, VPackException {
        if (!vpack.isNone()) {
            final Object value = getValue(parent, vpack, fieldInfo.getType(), fieldInfo.getFieldName(),
                    fieldInfo.getReferencingElement());
            fieldInfo.set(entity, value);
        }
    }

    private <T> Object getValue(
            final VPackSlice parent, final VPackSlice vpack, final Type type, final String fieldName)
            throws ReflectiveOperationException, VPackException {
        return getValue(parent, vpack, type, fieldName, null);
    }

    private <T> Object getValue(
            final VPackSlice parent,
            final VPackSlice vpack,
            final Type type,
            final String fieldName,
            final AnnotatedElement referencingElement) throws ReflectiveOperationException, VPackException {
        final Object value;
        final Type realType = determineType(vpack, type);
        if (vpack.isNull()) {
            final VPackDeserializer<?> deserializer = getDeserializerWithSelfNullHandle(fieldName, realType);
            if (deserializer != null) {
                if (VPackDeserializerParameterizedType.class.isAssignableFrom(deserializer.getClass())
                        && ParameterizedType.class.isAssignableFrom(realType.getClass())) {
                    value = ((VPackDeserializerParameterizedType<Object>) deserializer)
                            .deserialize(parent, vpack, deserializationContext, (ParameterizedType) realType);
                } else {
                    value = ((VPackDeserializer<Object>) deserializer).deserialize(parent, vpack,
                            deserializationContext);
                }
            } else {
                value = null;
            }
        } else {
            final VPackDeserializer<?> deserializer = getDeserializer(fieldName, realType);
            if (deserializer != null) {
                if (VPackDeserializerParameterizedType.class.isAssignableFrom(deserializer.getClass())
                        && ParameterizedType.class.isAssignableFrom(realType.getClass())) {
                    value = ((VPackDeserializerParameterizedType<Object>) deserializer).deserialize(parent, vpack,
                            deserializationContext, (ParameterizedType) realType);
                } else {
                    value = ((VPackDeserializer<Object>) deserializer).deserialize(parent, vpack,
                            deserializationContext);
                }
            } else if (realType instanceof ParameterizedType) {
                final ParameterizedType pType = (ParameterizedType) realType;
                final Type rawType = pType.getRawType();
                if (Iterable.class.isAssignableFrom((Class<?>) rawType)) {
                    value = deserializeIterable(parent, vpack, realType, pType.getActualTypeArguments()[0]);
                } else if (Map.class.isAssignableFrom((Class<?>) rawType)) {
                    final Type[] parameterizedTypes = pType.getActualTypeArguments();
                    value = deserializeMap(parent, vpack, realType, parameterizedTypes[0], parameterizedTypes[1]);
                } else {
                    value = deserializeObject(parent, vpack, realType, fieldName, referencingElement);
                }
            } else if (realType instanceof WildcardType) {
                final WildcardType wType = (WildcardType) realType;
                final Type rawType = wType.getUpperBounds()[0];
                value = getValue(
                        parent,
                        vpack,
                        rawType,
                        fieldName,
                        referencingElement
                );
            } else if (realType instanceof GenericArrayType) {
                throw new VPackParserException(new IllegalArgumentException("Generic arrays are not supported!"));
            } else if (Iterable.class.isAssignableFrom((Class<?>) realType)) {
                value = deserializeIterable(parent, vpack, realType, Object.class);
            } else if (Map.class.isAssignableFrom((Class<?>) realType)) {
                value = deserializeMap(parent, vpack, realType, String.class, Object.class);
            } else if (((Class) realType).isArray()) {
                value = deserializeArray(parent, vpack, realType);
            } else if (((Class) realType).isEnum()) {
                value = Enum.valueOf((Class<? extends Enum>) realType, vpack.getAsString());
            } else {
                value = deserializeObject(parent, vpack, realType, fieldName, referencingElement);
            }
        }
        return value;
    }

    private <T> Object deserializeArray(final VPackSlice parent, final VPackSlice vpack, final Type type)
            throws ReflectiveOperationException, VPackException {
        final int length = vpack.getLength();
        final Class<?> componentType = ((Class<?>) type).getComponentType();
        final Object value = Array.newInstance(componentType, length);
        int i = 0;
        for (final Iterator<VPackSlice> iterator = vpack.arrayIterator(); iterator.hasNext(); ) {
            Array.set(value, i++, getValue(parent, iterator.next(), componentType, null));
        }
        return value;
    }

    private <T, C> Object deserializeIterable(
            final VPackSlice parent, final VPackSlice vpack, final Type type, final Type contentType)
            throws ReflectiveOperationException, VPackException {
        final Collection value = createInstance(type);
        final long length = vpack.getLength();
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                value.add(getValue(parent, vpack.get(i), contentType, null));
            }
        }
        return value;
    }

    private <T, K, C> Object deserializeMap(
            final VPackSlice parent, final VPackSlice vpack, final Type type, final Type keyType, final Type valueType)
            throws ReflectiveOperationException, VPackException {
        final int length = vpack.getLength();
        final Map value = createInstance(type);
        if (length > 0) {
            final VPackKeyMapAdapter<Object> keyMapAdapter = getKeyMapAdapter(keyType);
            if (keyMapAdapter != null) {
                for (final Iterator<Entry<String, VPackSlice>> iterator = vpack.objectIterator(); iterator
                        .hasNext(); ) {
                    final Entry<String, VPackSlice> next = iterator.next();
                    final Object name = keyMapAdapter.deserialize(next.getKey());
                    value.put(name, getValue(vpack, next.getValue(), valueType, name.toString()));
                }
            } else {
                for (int i = 0; i < vpack.getLength(); i++) {
                    final VPackSlice entry = vpack.get(i);
                    final Object mapKey = getValue(parent, entry.get(ATTR_KEY), keyType, null);
                    final Object mapValue = getValue(parent, entry.get(ATTR_VALUE), valueType, null);
                    value.put(mapKey, mapValue);
                }
            }
        }
        return value;
    }

    public static class SerializeOptions {
        private Type type;
        private Map<String, Object> additionalFields;

        public SerializeOptions() {
            super();
            type = null;
            additionalFields = Collections.emptyMap();
        }

        public Type getType() {
            return type;
        }

        /**
         * @param type The source type of the Object.
         * @return options
         */
        public SerializeOptions type(final Type type) {
            this.type = type;
            return this;
        }

        public Map<String, Object> getAdditionalFields() {
            return additionalFields;
        }

        /**
         * @param additionalFields Additional Key/Value pairs to include in the created VelocyPack.
         * @return options
         */
        public SerializeOptions additionalFields(final Map<String, Object> additionalFields) {
            this.additionalFields = additionalFields;
            return this;
        }
    }

    /**
     * Serialize a given Object to VelocyPack
     *
     * @param entity The Object to serialize.
     * @return the serialized VelocyPack
     * @throws VPackParserException
     */
    public VPackSlice serialize(final Object entity) throws VPackParserException {
        return serialize(entity, new SerializeOptions().type(entity.getClass()));
    }

    /**
     * Serialize a given Object to VelocyPack
     *
     * @param entity           The Object to serialize.
     * @param additionalFields Additional Key/Value pairs to include in the created VelocyPack.
     * @return the serialized VelocyPack
     * @throws VPackParserException
     * @deprecated use {@link #serialize(Object, SerializeOptions)} instead
     */
    @Deprecated
    public VPackSlice serialize(final Object entity, final Map<String, Object> additionalFields)
            throws VPackParserException {
        return serialize(entity, new SerializeOptions().type(entity.getClass()).additionalFields(additionalFields));
    }

    /**
     * Serialize a given Object to VelocyPack
     *
     * @param entity The Object to serialize.
     * @param type   The source type of the Object.
     * @return the serialized VelocyPack
     * @throws VPackParserException
     * @deprecated use {@link #serialize(Object, SerializeOptions)} instead
     */
    @Deprecated
    public VPackSlice serialize(final Object entity, final Type type) throws VPackParserException {
        return serialize(entity, new SerializeOptions().type(type));
    }

    /**
     * Serialize a given Object to VelocyPack
     *
     * @param entity           The Object to serialize.
     * @param type             The source type of the Object.
     * @param additionalFields Additional Key/Value pairs to include in the created VelocyPack.
     * @return the serialized VelocyPack
     * @throws VPackParserException
     * @deprecated use {@link #serialize(Object, SerializeOptions)} instead
     */
    @Deprecated
    public VPackSlice serialize(final Object entity, final Type type, final Map<String, Object> additionalFields)
            throws VPackParserException {
        return serialize(entity, new SerializeOptions().type(type).additionalFields(additionalFields));
    }

    /**
     * Serialize a given Object to VelocyPack
     *
     * @param entity  The Object to serialize.
     * @param options Additional options
     * @return the serialized VelocyPack
     * @throws VPackParserException
     */
    public VPackSlice serialize(final Object entity, final SerializeOptions options) throws VPackParserException {
        Type type = options.getType();
        if (type == null) {
            type = entity.getClass();
        }
        if (type == VPackSlice.class) {
            return (VPackSlice) entity;
        }
        final VPackBuilder builder = new VPackBuilder(builderOptions);
        serialize(null, entity, type, builder, new HashMap<>(options.getAdditionalFields()));
        return builder.slice();
    }

    private void serialize(
            final String name,
            final Object entity,
            final Type type,
            final VPackBuilder builder,
            final Map<String, Object> additionalFields) throws VPackParserException {
        try {
            addValue(name, type, entity, builder, null, additionalFields);
        } catch (final Exception e) {
            throw new VPackParserException(e);
        }
    }

    private void serializeObject(
            final String name,
            final Object entity,
            final VPackBuilder builder,
            final Map<String, Object> additionalFields) throws ReflectiveOperationException, VPackException {

        final Class<?> type = entity.getClass();

        final VPackSerializer<?> serializer = getSerializer(type);
        if (serializer != null) {
            ((VPackSerializer<Object>) serializer).serialize(builder, name, entity, serializationContext);
        } else {

            builder.add(name, ValueType.OBJECT);
            serializeFields(entity, builder, additionalFields);
            builder.close(true);
        }
    }

    private void serializeFields(
            final Object entity,
            final VPackBuilder builder,
            final Map<String, Object> additionalFields) throws ReflectiveOperationException, VPackException {
        final Map<String, FieldInfo> fields = cache.getFields(entity.getClass());
        for (final FieldInfo fieldInfo : fields.values()) {
            if (fieldInfo.isSerialize()) {
                serializeField(entity, builder, fieldInfo, Collections.<String, Object>emptyMap());
            }
        }
        for (final Entry<String, Object> entry : additionalFields.entrySet()) {
            final String key = entry.getKey();
            if (!fields.containsKey(key)) {
                final Object value = entry.getValue();
                addValue(key, value != null ? value.getClass() : null, value, builder, null,
                        Collections.<String, Object>emptyMap());
            }
        }
    }

    private void serializeField(
            final Object entity,
            final VPackBuilder builder,
            final FieldInfo fieldInfo,
            final Map<String, Object> additionalFields) throws ReflectiveOperationException, VPackException {

        final String fieldName = fieldInfo.getFieldName();
        final Type type = fieldInfo.getType();
        final Object value = fieldInfo.get(entity);
        addValue(fieldName, type, value, builder, fieldInfo, additionalFields);
    }

    private void addValue(
            final String name,
            final Type type,
            final Object value,
            final VPackBuilder builder,
            final FieldInfo fieldInfo,
            final Map<String, Object> additionalFields) throws ReflectiveOperationException, VPackException {

        if (value == null) {
            if (serializeNullValues) {
                builder.add(name, ValueType.NULL);
            }
        } else {
            final VPackSerializer<?> serializer = getSerializer(type);
            if (serializer != null) {
                ((VPackSerializer<Object>) serializer).serialize(builder, name, value, serializationContext);
            } else if (type instanceof ParameterizedType) {
                final ParameterizedType pType = (ParameterizedType) type;
                final Type rawType = pType.getRawType();
                if (Iterable.class.isAssignableFrom((Class<?>) rawType)) {
                    serializeIterable(name, value, builder, pType.getActualTypeArguments()[0]);
                } else if (Map.class.isAssignableFrom((Class<?>) rawType)) {
                    serializeMap(name, value, builder, pType.getActualTypeArguments()[0], additionalFields);
                } else {
                    serializeObject(name, value, builder, additionalFields);
                }
            } else if (type instanceof WildcardType) {
                final WildcardType wType = (WildcardType) type;
                final Type rawType = wType.getUpperBounds()[0];
                addValue(name, rawType, value, builder, fieldInfo, additionalFields);
            } else if (type instanceof Class && Iterable.class.isAssignableFrom((Class<?>) type)) {
                serializeIterable(name, value, builder, null);
            } else if (type instanceof Class && Map.class.isAssignableFrom((Class<?>) type)) {
                serializeMap(name, value, builder, String.class, additionalFields);
            } else if (type instanceof Class && ((Class) type).isArray()) {
				final Type elemType = ((Class<?>) type).getComponentType();
				serializeArray(name, value, builder, elemType);
            } else if (type instanceof Class && ((Class) type).isEnum()) {
                builder.add(name, ((Enum) value).name());
            } else if (shouldAddTypeHint(type, value, fieldInfo)) {
                addValue(name, value.getClass(), value, builder, fieldInfo,
                        Collections.<String, Object>singletonMap(typeKey, value.getClass().getName()));
            } else {
                serializeObject(name, value, builder, additionalFields);
            }
        }
    }

    private boolean shouldAddTypeHint(
            final Type type,
            final Object value,
            final FieldInfo fieldInfo
    ) {
        if (!useTypeHint) {
            return false;
        }
        final AnnotatedElement referencingElement = fieldInfo != null ? fieldInfo.getReferencingElement() : null;
        final BuilderInfo builderInfo = builderUtils.getBuilderInfo(type, referencingElement);
        if (builderInfo != null) {
            return false;
        }

        final VPackCreatorMethodInfo factoryMethodInfo = vPackCreatorMethodUtils.getCreatorMethodInfo(type);
        if (factoryMethodInfo != null) {
            return false;
        }

        return type != value.getClass();
    }

    private void serializeArray(final String name, final Object value, final VPackBuilder builder, final Type type)
            throws ReflectiveOperationException, VPackException {
        builder.add(name, ValueType.ARRAY);
        for (int i = 0; i < Array.getLength(value); i++) {
            final Object element = Array.get(value, i);
            if (element != null) {
                final Type t = type != null ? type : element.getClass();
                addValue(null, t, element, builder, null, Collections.<String, Object>emptyMap());
            } else {
                builder.add(ValueType.NULL);
            }
        }
        builder.close();
    }

    private void serializeIterable(final String name, final Object value, final VPackBuilder builder, final Type type)
            throws ReflectiveOperationException, VPackException {
        builder.add(name, ValueType.ARRAY);
        for (final Object element : (Iterable) value) {
            final Type t = type != null ? type : element != null ? element.getClass() : null;
            addValue(null, t, element, builder, null, Collections.<String, Object>emptyMap());
        }
        builder.close();
    }

    private void serializeMap(
            final String name,
            final Object value,
            final VPackBuilder builder,
            final Type keyType,
            final Map<String, Object> additionalFields) throws ReflectiveOperationException, VPackException {
        final Map map = (Map) value;
        if (map.size() > 0) {
            final VPackKeyMapAdapter<Object> keyMapAdapter = getKeyMapAdapter(keyType);
            if (keyMapAdapter != null) {
                builder.add(name, ValueType.OBJECT);
                final Set<Entry<?, ?>> entrySet = map.entrySet();
                for (final Entry<?, ?> entry : entrySet) {
                    final Object entryValue = entry.getValue();
                    addValue(keyMapAdapter.serialize(entry.getKey()),
                            entryValue != null ? entryValue.getClass() : Object.class, entry.getValue(), builder, null,
                            Collections.<String, Object>emptyMap());
                }
                for (final Entry<String, Object> entry : additionalFields.entrySet()) {
                    final String key = entry.getKey();
                    if (!map.containsKey(key)) {
                        final Object additionalValue = entry.getValue();
                        addValue(key, additionalValue != null ? additionalValue.getClass() : null, additionalValue,
                                builder, null, Collections.<String, Object>emptyMap());
                    }
                }
            } else {
                builder.add(name, ValueType.ARRAY);
                final Set<Entry<?, ?>> entrySet = map.entrySet();
                for (final Entry<?, ?> entry : entrySet) {
                    builder.add((String) null, ValueType.OBJECT);
                    addValue(ATTR_KEY, entry.getKey().getClass(), entry.getKey(), builder, null,
                            Collections.<String, Object>emptyMap());
                    addValue(ATTR_VALUE, entry.getValue().getClass(), entry.getValue(), builder, null,
                            Collections.<String, Object>emptyMap());
                    builder.close();
                }
            }
        } else {
            builder.add(name, ValueType.OBJECT);
        }
        builder.close();
    }

    private VPackKeyMapAdapter<Object> getKeyMapAdapter(final Type type) {
        VPackKeyMapAdapter<?> adapter = keyMapAdapters.get(type);
        if (adapter == null && Enum.class.isAssignableFrom((Class<?>) type)) {
            adapter = VPackKeyMapAdapters.createEnumAdapter(type);
        }
        return (VPackKeyMapAdapter<Object>) adapter;
    }

    private VPackSerializer<?> getSerializer(final Type type) {
        VPackSerializer<?> serializer = serializers.get(type);
        if (serializer == null) {
            if (type instanceof Class && ((Class<?>) type).isMemberClass()) {
                serializer = enclosingSerializers.get(((Class<?>) type).getEnclosingClass());
            }
        }
        if (serializer == null && ParameterizedType.class.isAssignableFrom(type.getClass())) {
            serializer = getSerializer(((ParameterizedType) type).getRawType());
        }
        return serializer;
    }
}
