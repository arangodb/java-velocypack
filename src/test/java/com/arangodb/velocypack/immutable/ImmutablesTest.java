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

import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPackSlice;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * Note: all tests are repeated 3 times to check the behavior when hitting the cache {@link com.arangodb.velocypack.internal.VPackCache}
 * @author Michele Rastelli
 */
public class ImmutablesTest {

	@Test
	public void serdePerson() {
		VPack vpack = new VPack.Builder().build();
		for (int i = 0; i < 3; i++) {
			Person original = Person.builderFunction()
					.withName("name")
					.withAge(99)
					.withSecondNames(Arrays.asList("aaa", "bbb", "ccc"))
					.withAddresses(Arrays.asList(
							Collections.singletonMap("home", "Avocado Street 14"),
							Collections.singletonMap("work", "Java Street 14")
					))
					.buildIt();
			System.out.println(original);
			VPackSlice serialized = vpack.serialize(original);
			System.out.println(serialized);
			Person deserialized = vpack.deserialize(serialized, Person.class);
			System.out.println(deserialized);
			assertThat(deserialized, is(original));
		}
	}

	@Test
	public void serdePersonWithBuilder() {
		VPack vpack = new VPack.Builder().build();
		for (int i = 0; i < 3; i++) {
			PersonWithInnerBuilder original = new PersonWithInnerBuilder.Builder().setFullName("name").setAge(99)
					.setFriend(new ImmutablePersonWithoutAnnotations.Builder().withName("friend").withAge(88).buildIt())
					.build();
			System.out.println(original);
			VPackSlice serialized = vpack.serialize(original);
			System.out.println(serialized);
			PersonWithInnerBuilder deserialized = vpack.deserialize(serialized, PersonWithInnerBuilder.class);
			System.out.println(deserialized);
			assertThat(deserialized.getFullName(), is(original.getFullName()));
			assertThat(deserialized.getAge(), is(nullValue()));
		}
	}

	@Test
	public void serdePersonWithExternalBuilder() {
		VPack vpack = new VPack.Builder().build();
		for (int i = 0; i < 3; i++) {
			PersonWithExternalBuilder original = new ImmutablePersonWithExternalBuilder.Builder().withName("name")
					.withAge(99).buildIt();
			System.out.println(original);
			VPackSlice serialized = vpack.serialize(original);
			System.out.println(serialized);
			PersonWithExternalBuilder deserialized = vpack.deserialize(serialized, PersonWithExternalBuilder.class);
			System.out.println(deserialized);
			assertThat(deserialized, is(original));
		}
	}

	@Test
	public void personBean() {
		VPack vpack = new VPack.Builder().build();
		for (int i = 0; i < 3; i++) {
			PersonBean original = new PersonBean();
			original.setName("name");
			original.setAge(77);
			original.setFriend(
					new ImmutablePersonWithoutAnnotations.Builder().withName("friend").withAge(66).buildIt());
			System.out.println(original);
			VPackSlice serialized = vpack.serialize(original);
			System.out.println(serialized);
			PersonBean deserialized = vpack.deserialize(serialized, PersonBean.class);
			System.out.println(deserialized);
			assertThat(deserialized, is(original));
		}
	}

	@Test
	public void lombokBuilder() {
		VPack vpack = new VPack.Builder().build();
		for (int i = 0; i < 3; i++) {
			LombokPerson original = LombokPerson.builder().age(5).name("lombok").build();
			System.out.println(original);
			VPackSlice serialized = vpack.serialize(original);
			System.out.println(serialized);
			LombokPerson deserialized = vpack.deserialize(serialized, LombokPerson.class);
			System.out.println(deserialized);
			assertThat(deserialized, is(original));
		}
	}

	@Test
	public void annotatedExternalBuilder() {
		VPack vpack = new VPack.Builder().build();
		for (int i = 0; i < 3; i++) {
			PersonWithAnnotatedExternalBuilder original = new AnnotatedExternalBuilder()
					.withName("PersonWithAnnotatedExternalBuilder").withAge(2).buildIt();
			System.out.println(original);
			VPackSlice serialized = vpack.serialize(original);
			System.out.println(serialized);
			PersonWithAnnotatedExternalBuilder deserialized = vpack
					.deserialize(serialized, PersonWithAnnotatedExternalBuilder.class);
			System.out.println(deserialized);
			assertThat(deserialized, is(original));
		}
	}

	@Test
	public void allArgsFactoryMethod() {
		VPack vpack = new VPack.Builder().build();
		for (int i = 0; i < 3; i++) {
			PersonBean personBean = new PersonBean();
			personBean.setName("personBean");
			personBean.setAge(9);
			personBean.setFriend(new ImmutablePersonWithoutAnnotations.Builder().withName("bla").withAge(0).buildIt());
			FactoryMethodPerson original = FactoryMethodPerson
					.of("name", 99, LombokPerson.builder().name("lombok").age(99).build(), personBean,
							new AnnotatedExternalBuilder().withName("PersonWithAnnotatedExternalBuilder").withAge(2)
									.buildIt(),
							new PersonWithInnerBuilder.Builder().setFullName("innerBuilder").build());
			System.out.println(original);
			VPackSlice serialized = vpack.serialize(original);
			System.out.println(serialized);
			FactoryMethodPerson deserialized = vpack.deserialize(serialized, FactoryMethodPerson.class);
			System.out.println(deserialized);
			assertThat(deserialized, is(original));
		}
	}

	@Test
	public void allArgsConstructor() {
		VPack vpack = new VPack.Builder().build();
		for (int i = 0; i < 3; i++) {
			PersonBean personBean = new PersonBean();
			personBean.setName("personBean");
			personBean.setAge(9);
			personBean.setFriend(new ImmutablePersonWithoutAnnotations.Builder().withName("bla").withAge(0).buildIt());
			AllArgsPerson original = new AllArgsPerson("name", 99,
					LombokPerson.builder().name("lombok").age(99).build(), personBean,
					new AnnotatedExternalBuilder().withName("PersonWithAnnotatedExternalBuilder").withAge(2).buildIt(),
					new PersonWithInnerBuilder.Builder().setFullName("innerBuilder").build());
			System.out.println(original);
			VPackSlice serialized = vpack.serialize(original);
			System.out.println(serialized);
			AllArgsPerson deserialized = vpack.deserialize(serialized, AllArgsPerson.class);
			System.out.println(deserialized);
			assertThat(deserialized, is(original));
		}
	}

	@Test
	public void covariantCollection() {
		VPack vpack = new VPack.Builder().build();
		for (int i = 0; i < 3; i++) {
			PersonWithFriends original = PersonWithFriends.builder()
					.name("name")
					.age(987)
					.friends(Collections.singletonList(
							Person.builderFunction()
									.withName("name")
									.withAge(99)
									.withSecondNames(Arrays.asList("aaa", "bbb", "ccc"))
									.withAddresses(Arrays.asList(
											Collections.singletonMap("home", "Avocado Street 14"),
											Collections.singletonMap("work", "Java Street 14")
									))
									.buildIt()
					))
					.build();

			System.out.println(original);
			VPackSlice serialized = vpack.serialize(original);
			System.out.println(serialized);
			PersonWithFriends deserialized = vpack.deserialize(serialized, PersonWithFriends.class);
			System.out.println(deserialized);
			assertThat(deserialized, is(original));
		}
	}

}
