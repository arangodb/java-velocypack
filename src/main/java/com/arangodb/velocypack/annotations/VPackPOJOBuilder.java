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

package com.arangodb.velocypack.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Michele Rastelli
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface VPackPOJOBuilder {

	String DEFAULT_BUILD_METHOD_NAME = "build";
	String DEFAULT_WITH_PREFIX = "";

	String buildMethodName() default DEFAULT_BUILD_METHOD_NAME;

	String withPrefix() default DEFAULT_WITH_PREFIX;

	class Value {
		public final String buildMethodName;
		public final String withPrefix;

		public Value() {
			this(DEFAULT_BUILD_METHOD_NAME, DEFAULT_WITH_PREFIX);
		}

		public Value(VPackPOJOBuilder annotation) {
			this(annotation.buildMethodName(), annotation.withPrefix());
		}

		public Value(String buildMethodName, String withPrefix) {
			this.buildMethodName = buildMethodName;
			this.withPrefix = withPrefix;
		}
	}

}
