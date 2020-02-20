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
import com.arangodb.velocypack.annotations.VPackPOJOBuilder;

import java.util.Objects;

/**
 * @author Michele Rastelli
 */
public class PersonBean {
	private String name;
	private int age;
	@VPackDeserialize(builder = ImmutablePersonWithoutAnnotations.Builder.class,
					  builderConfig = @VPackPOJOBuilder(buildMethodName = "buildIt",
														withSetterPrefix = "with"))
	private PersonWithoutAnnotations friend;

	public PersonBean() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public PersonWithoutAnnotations getFriend() {
		return friend;
	}

	public void setFriend(PersonWithoutAnnotations friend) {
		this.friend = friend;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PersonBean that = (PersonBean) o;
		return age == that.age && Objects.equals(name, that.name) && Objects.equals(friend, that.friend);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, age, friend);
	}

	@Override
	public String toString() {
		return "PersonBean{" + "name='" + name + '\'' + ", age=" + age + ", friend=" + friend + '}';
	}
}
