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

		public Class<?> builderClass;

		public VPackPOJOBuilder.Value annotation;

		public BuilderInfo(Class<?> builderClass, VPackPOJOBuilder.Value annotation) {
			this.builderClass = builderClass;
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

		BuilderInfo builderInfo;

		builderInfo = getReferencingElementInfo(referencingElement);
		if (builderInfo == null) {
			builderInfo = getDeserializeClassInfo(type);
		}
		if (builderInfo == null) {
			builderInfo = getBuilderMethodInfo(type);
		}
		if (builderInfo == null) {
			builderInfo = getInnerBuilderInfo(type);
		}

		if (builderInfo == null) {
			return null;
		}

		// search builder info in class referenced by @VPackDeserialize.builder
		if (builderInfo.annotation == null) {
			Class<?> builderClass = builderInfo.builderClass;
			BuilderInfo additionalBuilderInfo = getBuilderMethodInfo(builderClass);
			if (additionalBuilderInfo == null) {
				additionalBuilderInfo = getBuilderInfo(builderClass);
			}
			if (additionalBuilderInfo != null) {
				builderInfo.annotation = additionalBuilderInfo.annotation;
			}
		}

		if (builderInfo.annotation == null) {
			builderInfo.annotation = new VPackPOJOBuilder.Value();
		}

		cache.put(key, builderInfo);
		return builderInfo;
	}

	private BuilderInfo getBuilderMethodInfo(Type type) {
		if (!(type instanceof Class<?>))
			return null;

		Class<?> clazz = (Class<?>) type;
		for (final Method method : clazz.getDeclaredMethods()) {
			for (final Annotation annotation : method.getDeclaredAnnotations()) {
				if (annotation instanceof VPackPOJOBuilder) {
					return new BuilderInfo(method.getReturnType(), mapVPackPOJOBuilder((VPackPOJOBuilder) annotation)) {
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
					return new BuilderInfo(innerClass, mapVPackPOJOBuilder((VPackPOJOBuilder) annotation)) {
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

	private BuilderInfo getBuilderInfo(Type type) {
		if (!(type instanceof Class<?>))
			return null;

		Class<?> clazz = (Class<?>) type;
		for (final Annotation annotation : clazz.getDeclaredAnnotations()) {
			if (annotation instanceof VPackPOJOBuilder) {
				return new BuilderInfo(clazz, mapVPackPOJOBuilder((VPackPOJOBuilder) annotation)) {
					@Override
					public Object createBuilder() throws ReflectiveOperationException {
						return clazz.newInstance();
					}
				};
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
				return new BuilderInfo(vPackDeserialize.builder(),
						mapVPackPOJOBuilder(vPackDeserialize.builderConfig())) {
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
				return new BuilderInfo(vPackDeserialize.builder(),
						mapVPackPOJOBuilder(vPackDeserialize.builderConfig())) {
					@Override
					public Object createBuilder() throws ReflectiveOperationException {
						return vPackDeserialize.builder().newInstance();
					}
				};
			}
		}

		return null;
	}

	private VPackPOJOBuilder.Value mapVPackPOJOBuilder(VPackPOJOBuilder annotation) {
		if (annotation.withSetterPrefix().equals(VPackDeserialize.UNDEFINED_WITH_PREFIX) && annotation.buildMethodName()
				.equals(VPackDeserialize.UNDEFINED_BUILD_METHOD_NAME)) {
			return null;
		}

		return new VPackPOJOBuilder.Value(annotation);
	}
}
