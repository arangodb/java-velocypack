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

import com.arangodb.velocypack.annotations.VPackDeserialize;

import java.util.Objects;

/**
 * @author Michele Rastelli
 */
@VPackDeserialize(builder = AnnotatedExternalBuilder.class)
public class PersonWithAnnotatedExternalBuilder {

	private final String name;
	private final int age;

	public PersonWithAnnotatedExternalBuilder(String name, int age) {
		this.name = name;
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public int getAge() {
		return age;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PersonWithAnnotatedExternalBuilder that = (PersonWithAnnotatedExternalBuilder) o;
		return age == that.age && Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, age);
	}

	@Override
	public String toString() {
		return "PersonWithAnnotatedExternalBuilder{" + "name='" + name + '\'' + ", age=" + age + '}';
	}
}

