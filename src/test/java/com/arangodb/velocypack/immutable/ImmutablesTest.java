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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * @author Michele Rastelli
 */
public class ImmutablesTest {

	@Test
	public void serdePerson() {
		VPack vpack = new VPack.Builder().build();
		Person original = Person.builderFunction().withName("name").withAge(99).buildIt();
		System.out.println(original);
		VPackSlice serialized = vpack.serialize(original);
		System.out.println(serialized);
		Person deserialized = vpack.deserialize(serialized, Person.class);
		System.out.println(deserialized);
		assertThat(deserialized, is(original));
	}

	@Test
	public void serdePersonWithBuilder() {
		VPack vpack = new VPack.Builder().build();
		PersonWithBuilder original = PersonWithBuilder.builder().setFullName("name").setAge(99).build();
		System.out.println(original);
		VPackSlice serialized = vpack.serialize(original);
		System.out.println(serialized);
		PersonWithBuilder deserialized = vpack.deserialize(serialized, PersonWithBuilder.class);
		System.out.println(deserialized);
		assertThat(deserialized.getFullName(), is(original.getFullName()));
		assertThat(deserialized.getAge(), is(nullValue()));
	}

}
