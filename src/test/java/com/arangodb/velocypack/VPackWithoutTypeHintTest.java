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

package com.arangodb.velocypack;

import org.junit.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Michele Rastelli
 */
public class VPackWithoutTypeHintTest {

	public static class NestedCollection {
		public Collection<String> value;
	}

	@Test
	public void nestedCollectionWithoutTypeInformation() {
		NestedCollection input = new NestedCollection();
		input.value = Arrays.asList("a", "b", "c");

		final VPack vpack = new VPack.Builder().useTypeHint(false).build();
		final VPackSlice slice = vpack.serialize(input);

		assertThat(slice.isObject(), is(true));
		assertThat(slice.get("value").isArray(), is(true));
		assertThat(slice.get("_class").isNone(), is(true));

		NestedCollection output = vpack.deserialize(slice, NestedCollection.class);
		assertThat(output.value, instanceOf(List.class));

		assertThat(output.value, equalTo(input.value));
	}

	public static class NestedMap {
		public Map<String, String> value;
	}

	@Test
	public void nestedMapWithoutTypeInformation() {
		NestedMap input = new NestedMap();
		input.value = Collections.singletonMap("a", "b");

		final VPack vpack = new VPack.Builder().useTypeHint(false).build();
		final VPackSlice slice = vpack.serialize(input);

		assertThat(slice.isObject(), is(true));
		assertThat(slice.get("value").isObject(), is(true));
		assertThat(slice.get("_class").isNone(), is(true));
		assertThat(slice.get("value").get("_class").isNone(), is(true));

		NestedMap output = vpack.deserialize(slice, NestedMap.class);
		assertThat(output.value, instanceOf(Map.class));

		assertThat(output.value, equalTo(input.value));
	}

	public static class NestedObject {
		public Object value;
	}

	@Test
	public void nestedObjectWithoutTypeInformation() {
		NestedObject input = new NestedObject();
		input.value = Collections.singletonMap("a", "b");

		final VPack vpack = new VPack.Builder().useTypeHint(false).build();
		final VPackSlice slice = vpack.serialize(input);

		assertThat(slice.isObject(), is(true));
		assertThat(slice.get("value").isObject(), is(true));
		assertThat(slice.get("_class").isNone(), is(true));
		assertThat(slice.get("value").get("_class").isNone(), is(true));

		NestedObject output = vpack.deserialize(slice, NestedObject.class);
		assertThat(output.value, instanceOf(Map.class));

		assertThat(output.value, equalTo(input.value));
	}

	public static class NestedGeneric<T> {
		public T value;
	}

	@Test
	public void nestedGenericOfCollectionWithoutTypeInformation() {
		NestedGeneric<Collection<String>> input = new NestedGeneric<>();
		input.value = Arrays.asList("a", "b", "c");

		final VPack vpack = new VPack.Builder().useTypeHint(false).build();
		final VPackSlice slice = vpack.serialize(input);

		assertThat(slice.isObject(), is(true));
		assertThat(slice.get("value").isArray(), is(true));
		assertThat(slice.get("_class").isNone(), is(true));

		NestedGeneric<?> output = vpack.deserialize(slice, NestedGeneric.class);
		assertThat(output.value, instanceOf(List.class));

		assertThat(output.value, equalTo(input.value));
	}

	@Test
	public void nestedGenericOfMapWithoutTypeInformation() {
		NestedGeneric<Map<String, String>> input = new NestedGeneric<>();
		input.value = Collections.singletonMap("a", "b");

		final VPack vpack = new VPack.Builder().useTypeHint(false).build();
		final VPackSlice slice = vpack.serialize(input);

		assertThat(slice.isObject(), is(true));
		assertThat(slice.get("value").isObject(), is(true));
		assertThat(slice.get("_class").isNone(), is(true));
		assertThat(slice.get("value").get("_class").isNone(), is(true));

		NestedGeneric<?> output = vpack.deserialize(slice, NestedGeneric.class);
		assertThat(output.value, instanceOf(Map.class));

		assertThat(output.value, equalTo(input.value));
	}

	@Test
	public void nestedGenericOfObjectWithoutTypeInformation() {
		NestedGeneric<Object> input = new NestedGeneric<>();
		input.value = Collections.singletonMap("a", "b");

		final VPack vpack = new VPack.Builder().useTypeHint(false).build();
		final VPackSlice slice = vpack.serialize(input);

		assertThat(slice.isObject(), is(true));
		assertThat(slice.get("value").isObject(), is(true));
		assertThat(slice.get("_class").isNone(), is(true));
		assertThat(slice.get("value").get("_class").isNone(), is(true));

		NestedGeneric<?> output = vpack.deserialize(slice, NestedGeneric.class);
		assertThat(output.value, instanceOf(Map.class));

		assertThat(output.value, equalTo(input.value));
	}

	public static class NestedGenericBoundedCollection<T extends Collection<?>> {
		public T value;
	}

	@Test
	public void nestedGenericBoundedCollectionWithoutTypeInformation() {
		NestedGenericBoundedCollection<Collection<String>> input = new NestedGenericBoundedCollection<>();
		input.value = Arrays.asList("a", "b", "c");

		final VPack vpack = new VPack.Builder().useTypeHint(false).build();
		final VPackSlice slice = vpack.serialize(input);

		assertThat(slice.isObject(), is(true));
		assertThat(slice.get("value").isArray(), is(true));
		assertThat(slice.get("_class").isNone(), is(true));

		NestedGenericBoundedCollection<?> output = vpack.deserialize(slice, NestedGenericBoundedCollection.class);
		assertThat(output.value, instanceOf(List.class));

		assertThat(output.value, equalTo(input.value));
	}

	public static class NestedGenericBoundedMap<T extends Map<?, ?>> {
		public T value;
	}

	@Test
	public void nestedGenericBoundedMapWithoutTypeInformation() {
		NestedGenericBoundedMap<Map<String, String>> input = new NestedGenericBoundedMap<>();
		input.value = Collections.singletonMap("a", "b");

		final VPack vpack = new VPack.Builder().useTypeHint(false).build();
		final VPackSlice slice = vpack.serialize(input);

		assertThat(slice.isObject(), is(true));
		assertThat(slice.get("value").isObject(), is(true));
		assertThat(slice.get("_class").isNone(), is(true));
		assertThat(slice.get("value").get("_class").isNone(), is(true));

		NestedGenericBoundedMap<?> output = vpack.deserialize(slice, NestedGenericBoundedMap.class);
		assertThat(output.value, instanceOf(Map.class));

		assertThat(output.value, equalTo(input.value));
	}

}
