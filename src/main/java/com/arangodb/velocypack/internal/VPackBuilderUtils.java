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

import com.arangodb.velocypack.annotations.VPackDeserialize;
import com.arangodb.velocypack.annotations.VPackPOJOBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Michele Rastelli
 */
public class VPackBuilderUtils {
	private final Map<CacheKey, BuilderInfo> cache;

	public abstract static class BuilderInfo {

		public final VPackPOJOBuilder.Value annotation;

		public BuilderInfo(VPackPOJOBuilder.Value annotation) {
			this.annotation = annotation;
		}

		public abstract Object createBuilder() throws ReflectiveOperationException;

	}

	private static class CacheKey {
		private final Type type;
		private final AnnotatedElement referencingElement;

		public CacheKey(Type type, AnnotatedElement referencingElement) {
			this.type = type;
			this.referencingElement = referencingElement;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			CacheKey cacheKey = (CacheKey) o;
			return Objects.equals(type, cacheKey.type) && Objects
					.equals(referencingElement, cacheKey.referencingElement);
		}

		@Override
		public int hashCode() {
			return Objects.hash(type, referencingElement);
		}

	}

	public VPackBuilderUtils() {
		cache = new ConcurrentHashMap<>();
	}

	public BuilderInfo getBuilderInfo(Type type, AnnotatedElement referencingElement) {
		final CacheKey key = new CacheKey(type, referencingElement);
		BuilderInfo fromCache = cache.get(key);
		if (fromCache != null)
			return fromCache;

		BuilderInfo referencingElementInfo = getReferencingElementInfo(referencingElement);
		if (referencingElementInfo != null) {
			cache.put(key, referencingElementInfo);
			return referencingElementInfo;
		}

		BuilderInfo deserializeClassInfo = getDeserializeClassInfo(type);
		if (deserializeClassInfo != null) {
			cache.put(key, deserializeClassInfo);
			return deserializeClassInfo;
		}

		BuilderInfo builderMethodInfo = getBuilderMethodInfo(type);
		if (builderMethodInfo != null) {
			cache.put(key, builderMethodInfo);
			return builderMethodInfo;
		}

		BuilderInfo innerBuilderInfo = getInnerBuilderInfo(type);
		if (innerBuilderInfo != null) {
			cache.put(key, innerBuilderInfo);
			return innerBuilderInfo;
		}

		return null;
	}

	private BuilderInfo getBuilderMethodInfo(Type type) {
		if (!(type instanceof Class<?>))
			return null;

		Class<?> clazz = (Class<?>) type;
		for (final Method method : clazz.getDeclaredMethods()) {
			for (final Annotation annotation : method.getDeclaredAnnotations()) {
				if (annotation instanceof VPackPOJOBuilder) {
					return new BuilderInfo(new VPackPOJOBuilder.Value((VPackPOJOBuilder) annotation)) {
						@Override
						public Object createBuilder() throws ReflectiveOperationException {
							return method.invoke(null);
						}
					};
				}
			}
		}

		return null;
	}

	private BuilderInfo getInnerBuilderInfo(Type type) {
		if (!(type instanceof Class<?>))
			return null;

		Class<?> clazz = (Class<?>) type;
		for (final Class<?> innerClass : clazz.getDeclaredClasses()) {
			for (final Annotation annotation : innerClass.getDeclaredAnnotations()) {
				if (annotation instanceof VPackPOJOBuilder) {
					return new BuilderInfo(new VPackPOJOBuilder.Value((VPackPOJOBuilder) annotation)) {
						@Override
						public Object createBuilder() throws ReflectiveOperationException {
							return innerClass.newInstance();
						}
					};
				}
			}
		}

		return null;
	}

	private BuilderInfo getDeserializeClassInfo(Type type) {
		if (!(type instanceof Class<?>))
			return null;

		Class<?> clazz = (Class<?>) type;
		for (final Annotation annotation : clazz.getDeclaredAnnotations()) {
			if (annotation instanceof VPackDeserialize) {
				final VPackDeserialize vPackDeserialize = (VPackDeserialize) annotation;
				return new BuilderInfo(new VPackPOJOBuilder.Value(vPackDeserialize.builderConfig())) {
					@Override
					public Object createBuilder() throws ReflectiveOperationException {
						return vPackDeserialize.builder().newInstance();
					}
				};
			}
		}

		return null;
	}

	private BuilderInfo getReferencingElementInfo(AnnotatedElement ref) {
		if (ref == null)
			return null;

		for (final Annotation annotation : ref.getDeclaredAnnotations()) {
			if (annotation instanceof VPackDeserialize) {
				final VPackDeserialize vPackDeserialize = (VPackDeserialize) annotation;
				return new BuilderInfo(new VPackPOJOBuilder.Value(vPackDeserialize.builderConfig())) {
					@Override
					public Object createBuilder() throws ReflectiveOperationException {
						return vPackDeserialize.builder().newInstance();
					}
				};
			}
		}

		return null;
	}
}
