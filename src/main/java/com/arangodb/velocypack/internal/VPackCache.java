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

package com.arangodb.velocypack.internal;

import com.arangodb.velocypack.VPackAnnotationFieldFilter;
import com.arangodb.velocypack.VPackAnnotationFieldNaming;
import com.arangodb.velocypack.VPackFieldNamingStrategy;
import com.arangodb.velocypack.internal.VPackCreatorMethodUtils.ParameterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Mark Vollmary
 *
 */
public class VPackCache {

	public abstract static class FieldInfo {
		private final AnnotatedElement referencingElement;
		private final Type type;
		private final String fieldName;
		private final boolean serialize;
		private final boolean deserialize;

		private FieldInfo(
				final AnnotatedElement referencingElement,
				final Type type,
				final String fieldName,
				final boolean serialize,
				final boolean deserialize) {
			super();
			this.referencingElement = referencingElement;
			this.type = type;
			this.fieldName = fieldName;
			this.serialize = serialize;
			this.deserialize = deserialize;
		}

		public AnnotatedElement getReferencingElement() {
			return referencingElement;
		}

		public Type getType() {
			return type;
		}

		public String getFieldName() {
			return fieldName;
		}

		public boolean isSerialize() {
			return serialize;
		}

		public boolean isDeserialize() {
			return deserialize;
		}

		public abstract void set(Object obj, Object value) throws ReflectiveOperationException;

		public abstract Object get(Object obj) throws ReflectiveOperationException;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(VPackCache.class);

	private final Map<Type, Map<String, FieldInfo>> cache;
	private final Comparator<Entry<String, FieldInfo>> fieldComparator;
	private final VPackFieldNamingStrategy fieldNamingStrategy;

	private final Map<Class<? extends Annotation>, VPackAnnotationFieldFilter<? extends Annotation>> annotationFilter;
	private final Map<Class<? extends Annotation>, VPackAnnotationFieldNaming<? extends Annotation>> annotationFieldNaming;

	public VPackCache(final VPackFieldNamingStrategy fieldNamingStrategy,
		final Map<Class<? extends Annotation>, VPackAnnotationFieldFilter<? extends Annotation>> annotationFieldFilter,
		final Map<Class<? extends Annotation>, VPackAnnotationFieldNaming<? extends Annotation>> annotationFieldNaming) {
		super();
		cache = new ConcurrentHashMap<>();
		fieldComparator = new Comparator<Map.Entry<String, FieldInfo>>() {
			@Override
			public int compare(final Entry<String, FieldInfo> o1, final Entry<String, FieldInfo> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		};
		this.fieldNamingStrategy = fieldNamingStrategy;
		this.annotationFilter = annotationFieldFilter;
		this.annotationFieldNaming = annotationFieldNaming;
	}

	public Map<String, FieldInfo> getFields(final Type entityClass) {
		Map<String, FieldInfo> fields = cache.get(entityClass);
		if (fields == null) {
			fields = new HashMap<>();
			Class<?> tmp = (Class<?>) entityClass;
			while (tmp != null && tmp != Object.class) {
				final Field[] declaredFields = tmp.getDeclaredFields();
				for (final Field field : declaredFields) {
					if (!field.isSynthetic() && !Modifier.isStatic(field.getModifiers()) && !Modifier
							.isTransient(field.getModifiers())) {
						field.setAccessible(true);
						final FieldInfo fieldInfo = createFieldInfo(field);
						if (fieldInfo.serialize || fieldInfo.deserialize) {
							fields.put(fieldInfo.getFieldName(), fieldInfo);
						}
					}
				}
				tmp = tmp.getSuperclass();
			}
			fields = sort(fields.entrySet());
			cache.put(entityClass, fields);
		}
		return fields;
	}

	// TODO: add cache
	public LinkedHashMap<String, ParameterInfo> getParameters(final Executable factoryMethod) {
		LinkedHashMap<String, ParameterInfo> fields = new LinkedHashMap<>();
		for (Parameter parameter : factoryMethod.getParameters()) {
			final ParameterInfo parameterInfo = createParameterInfo(parameter);
			fields.put(parameterInfo.name, parameterInfo);
		}
		return fields;
	}

	private boolean matchSetter(final Method method, String withSetterPrefix) {
		// check name
		if (!method.getName().startsWith(withSetterPrefix))
			return false;

		int modifiers = method.getModifiers();

		// check modifiers
		if (method.isSynthetic() || Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers))
			return false;

		// check public
		if (!Modifier.isPublic(modifiers))
			return false;

		// check parameters
		if (method.getGenericParameterTypes().length != 1)
			return false;

		return true;
	}

	public Map<String, FieldInfo> getBuiderSetters(final Type entityClass, String withSetterPrefix) {
		Map<String, FieldInfo> fields = cache.get(entityClass);
		if (fields == null) {
			fields = new HashMap<>();
			Class<?> tmp = (Class<?>) entityClass;
			while (tmp != null && tmp != Object.class) {
				final Method[] declaredMethods = tmp.getDeclaredMethods();
				for (final Method method : declaredMethods) {
					if (matchSetter(method, withSetterPrefix)) {
						final FieldInfo fieldInfo = createSetterInfo(method, withSetterPrefix);
						if (fieldInfo.serialize || fieldInfo.deserialize) {
							fields.put(fieldInfo.getFieldName(), fieldInfo);
						}
					}
				}
				tmp = tmp.getSuperclass();
			}
			fields = sort(fields.entrySet());
			cache.put(entityClass, fields);
		}
		return fields;
	}

	private Map<String, FieldInfo> sort(final Set<Entry<String, FieldInfo>> entrySet) {
		final Map<String, FieldInfo> sorted = new LinkedHashMap<>();
		final List<Entry<String, FieldInfo>> tmp = new ArrayList<>(entrySet);
		Collections.sort(tmp, fieldComparator);
		for (final Entry<String, FieldInfo> entry : tmp) {
			sorted.put(entry.getKey(), entry.getValue());
		}
		return sorted;
	}

	@SuppressWarnings("unchecked")
	private FieldInfo createFieldInfo(final Field field) {
		String fieldName = field.getName();
		if (fieldNamingStrategy != null) {
			fieldName = fieldNamingStrategy.translateName(field);
		}
		boolean found = false;
		for (final Entry<Class<? extends Annotation>, VPackAnnotationFieldNaming<? extends Annotation>> entry : annotationFieldNaming
				.entrySet()) {
			final Annotation annotation = field.getAnnotation(entry.getKey());
			if (annotation != null) {
				fieldName = ((VPackAnnotationFieldNaming<Annotation>) entry.getValue()).name(annotation);
				if (found) {
					LOGGER.warn(String.format(
							"Found additional annotation %s for field %s. Override previous annotation informations.",
							entry.getKey().getSimpleName(), field.getName()));
				}
				found = true;
			}
		}
		boolean serialize = true;
		boolean deserialize = true;
		found = false;
		for (final Entry<Class<? extends Annotation>, VPackAnnotationFieldFilter<? extends Annotation>> entry : annotationFilter
				.entrySet()) {
			final Annotation annotation = field.getAnnotation(entry.getKey());
			if (annotation != null) {
				final VPackAnnotationFieldFilter<Annotation> filter = (VPackAnnotationFieldFilter<Annotation>) entry
						.getValue();
				serialize = filter.serialize(annotation);
				deserialize = filter.deserialize(annotation);
				if (found) {
					LOGGER.warn(String.format(
							"Found additional annotation %s for field %s. Override previous annotation informations.",
							entry.getKey().getSimpleName(), field.getName()));
				}
				found = true;
			}
		}
		final Class<?> clazz = field.getType();
		final Type type = (clazz == Object.class) ? Object.class : field.getGenericType();
		return new FieldInfo(field, type, fieldName, serialize, deserialize) {
			@Override
			public void set(final Object obj, final Object value) throws IllegalAccessException {
				field.set(obj, value);
			}

			@Override
			public Object get(final Object obj) throws IllegalAccessException {
				return field.get(obj);
			}
		};
	}

	@SuppressWarnings("unchecked")
	private ParameterInfo createParameterInfo(final Parameter parameter) {
		String fieldName = parameter.getName();
		for (final Entry<Class<? extends Annotation>, VPackAnnotationFieldNaming<? extends Annotation>> entry : annotationFieldNaming
				.entrySet()) {
			final Annotation annotation = parameter.getAnnotation(entry.getKey());
			if (annotation != null) {
				fieldName = ((VPackAnnotationFieldNaming<Annotation>) entry.getValue()).name(annotation);
			}
		}
		final Class<?> clazz = parameter.getType();
		final Type type = (clazz == Object.class) ? Object.class : parameter.getParameterizedType();
		return new ParameterInfo(parameter, type, fieldName);
	}

	@SuppressWarnings("unchecked")
	private FieldInfo createSetterInfo(final Method setter, String withSetterPrefix) {
		int prefixLength = withSetterPrefix.length();
		String setterName = setter.getName();
		String fieldName = setterName.substring(prefixLength, prefixLength + 1).toLowerCase() + setterName
				.substring(prefixLength + 1);

		boolean found = false;
		for (final Entry<Class<? extends Annotation>, VPackAnnotationFieldNaming<? extends Annotation>> entry : annotationFieldNaming
				.entrySet()) {
			final Annotation annotation = setter.getAnnotation(entry.getKey());
			if (annotation != null) {
				fieldName = ((VPackAnnotationFieldNaming<Annotation>) entry.getValue()).name(annotation);
				if (found) {
					LOGGER.warn(String.format(
							"Found additional annotation %s for field %s. Override previous annotation informations.",
							entry.getKey().getSimpleName(), setter.getName()));
				}
				found = true;
			}
		}
		boolean serialize = true;
		boolean deserialize = true;
		found = false;
		for (final Entry<Class<? extends Annotation>, VPackAnnotationFieldFilter<? extends Annotation>> entry : annotationFilter
				.entrySet()) {
			final Annotation annotation = setter.getAnnotation(entry.getKey());
			if (annotation != null) {
				final VPackAnnotationFieldFilter<Annotation> filter = (VPackAnnotationFieldFilter<Annotation>) entry
						.getValue();
				serialize = filter.serialize(annotation);
				deserialize = filter.deserialize(annotation);
				if (found) {
					LOGGER.warn(String.format(
							"Found additional annotation %s for field %s. Override previous annotation informations.",
							entry.getKey().getSimpleName(), setter.getName()));
				}
				found = true;
			}
		}

		final Class<?> clazz = setter.getParameterTypes()[0];
		final Type type = (clazz == Object.class) ? Object.class : setter.getGenericParameterTypes()[0];
		return new FieldInfo(setter, type, fieldName, serialize, deserialize) {
			@Override
			public void set(final Object obj, final Object value) throws ReflectiveOperationException {
				setter.invoke(obj, value);
			}

			@Override
			public Object get(final Object obj) {
				throw new UnsupportedOperationException();
			}
		};
	}

}
