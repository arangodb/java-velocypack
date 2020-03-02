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


package com.arangodb.velocypack.immutable;

import com.arangodb.velocypack.annotations.VPackPOJOBuilder;

@VPackPOJOBuilder(buildMethodName = "buildIt",
				  withSetterPrefix = "with")
public class AnnotatedExternalBuilder {
	private String name;
	private int age;

	public AnnotatedExternalBuilder() {
	}

	public AnnotatedExternalBuilder withName(String name) {
		this.name = name;
		return this;
	}

	public AnnotatedExternalBuilder withAge(int age) {
		this.age = age;
		return this;
	}

	public PersonWithAnnotatedExternalBuilder buildIt() {
		return new PersonWithAnnotatedExternalBuilder(name, age);
	}

}
