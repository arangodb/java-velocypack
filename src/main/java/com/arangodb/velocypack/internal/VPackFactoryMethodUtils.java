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

import com.arangodb.velocypack.annotations.VPackCreator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Michele Rastelli
 */
public class VPackFactoryMethodUtils {
	private final Map<Type, FactoryMethodInfo> cache;

	public class FactoryMethodInfo {
		public final Method factoryMethod;

		public FactoryMethodInfo(final Method factoryMethod) {
			this.factoryMethod = factoryMethod;
		}
	}

	public VPackFactoryMethodUtils() {
		cache = new ConcurrentHashMap<>();
	}

	public FactoryMethodInfo getFactoryMethodInfo(Type type) {
		if (!(type instanceof Class<?>))
			return null;

		FactoryMethodInfo fromCache = cache.get(type);
		if (fromCache != null)
			return fromCache;

		Class<?> clazz = (Class<?>) type;
		for (final Method method : clazz.getDeclaredMethods()) {
			for (final Annotation annotation : method.getDeclaredAnnotations()) {
				if (annotation instanceof VPackCreator) {
					return new FactoryMethodInfo(method);
				}
			}
		}

		return null;
	}

}
