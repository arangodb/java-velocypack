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

import com.arangodb.velocypack.annotations.Expose;
import com.arangodb.velocypack.annotations.SerializedName;
import com.arangodb.velocypack.annotations.VPackPOJOBuilder;

import java.util.Objects;

/**
 * @author Michele Rastelli
 */
public class PersonWithBuilder {

	@SerializedName("name")
	private String fullName;
	private Integer age;

	private PersonWithBuilder(String fullName, Integer age) {
		this.fullName = fullName;
		this.age = age;
	}

	public String getFullName() {
		return fullName;
	}

	public Integer getAge() {
		return age;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PersonWithBuilder that = (PersonWithBuilder) o;
		return Objects.equals(fullName, that.fullName) && Objects.equals(age, that.age);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fullName, age);
	}

	@Override
	public String toString() {
		return "PersonWithBuilder{" + "fullName='" + fullName + '\'' + ", age=" + age + '}';
	}

	@VPackPOJOBuilder(withPrefix = "set")
	public static final class Builder {

		private String fullName;
		private Integer age;

		public Builder() {
		}

		@SerializedName("name")
		public final Builder setFullName(String fullName) {
			this.fullName = fullName;
			return this;
		}

		@Expose(deserialize = false)
		public final Builder setAge(Integer age) {
			this.age = age;
			return this;
		}

		public PersonWithBuilder build() {
			return new PersonWithBuilder(fullName, age);
		}
	}
}
