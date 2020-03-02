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

import com.arangodb.velocypack.annotations.SerializedName;
import com.arangodb.velocypack.annotations.VPackCreator;

import java.util.Objects;

/**
 * @author Michele Rastelli
 */
public class FactoryMethodPerson {

	private final String name;
	private final int age;
	private final LombokPerson lombokPerson;
	private final PersonBean personBean;
	private final PersonWithAnnotatedExternalBuilder personWithAnnotatedExternalBuilder;
	@SerializedName("person-with-inner-builder")
	private final PersonWithInnerBuilder personWithInnerBuilder;

	public FactoryMethodPerson(
			String name,
			int age,
			LombokPerson lombokPerson,
			PersonBean personBean,
			PersonWithAnnotatedExternalBuilder personWithAnnotatedExternalBuilder,
			PersonWithInnerBuilder personWithInnerBuilder) {
		this.name = name;
		this.age = age;
		this.lombokPerson = lombokPerson;
		this.personBean = personBean;
		this.personWithAnnotatedExternalBuilder = personWithAnnotatedExternalBuilder;
		this.personWithInnerBuilder = personWithInnerBuilder;
	}

	@VPackCreator
	public static FactoryMethodPerson of(
			@SerializedName("name")
					String name,
			@SerializedName("age")
					int age,
			@SerializedName("lombokPerson")
					LombokPerson lombokPerson,
			@SerializedName("personBean")
					PersonBean personBean,
			@SerializedName("personWithAnnotatedExternalBuilder")
					PersonWithAnnotatedExternalBuilder personWithAnnotatedExternalBuilder,
			@SerializedName("person-with-inner-builder")
					PersonWithInnerBuilder personWithInnerBuilder

	) {
		return new FactoryMethodPerson(name, age, lombokPerson, personBean, personWithAnnotatedExternalBuilder,
				personWithInnerBuilder);
	}

	public String getName() {
		return name;
	}

	public int getAge() {
		return age;
	}

	public LombokPerson getLombokPerson() {
		return lombokPerson;
	}

	public PersonBean getPersonBean() {
		return personBean;
	}

	public PersonWithAnnotatedExternalBuilder getPersonWithAnnotatedExternalBuilder() {
		return personWithAnnotatedExternalBuilder;
	}

	public PersonWithInnerBuilder getPersonWithInnerBuilder() {
		return personWithInnerBuilder;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		FactoryMethodPerson that = (FactoryMethodPerson) o;
		return age == that.age && Objects.equals(name, that.name) && Objects.equals(lombokPerson, that.lombokPerson)
				&& Objects.equals(personBean, that.personBean) && Objects
				.equals(personWithAnnotatedExternalBuilder, that.personWithAnnotatedExternalBuilder) && Objects
				.equals(personWithInnerBuilder, that.personWithInnerBuilder);
	}

	@Override
	public int hashCode() {
		return Objects
				.hash(name, age, lombokPerson, personBean, personWithAnnotatedExternalBuilder, personWithInnerBuilder);
	}

	@Override
	public String toString() {
		return "FactoryMethodPerson{" + "name='" + name + '\'' + ", age=" + age + ", lombokPerson=" + lombokPerson
				+ ", personBean=" + personBean + ", personWithAnnotatedExternalBuilder="
				+ personWithAnnotatedExternalBuilder + ", personWithInnerBuilder=" + personWithInnerBuilder + '}';
	}
}
