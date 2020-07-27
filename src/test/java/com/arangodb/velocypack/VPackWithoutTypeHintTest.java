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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Michele Rastelli
 */
@RunWith(Parameterized.class)
public class VPackWithoutTypeHintTest {

	private final boolean useTypeHint;

	@Parameterized.Parameters
	public static Collection<Boolean> useTypeHint() {
		return Arrays.asList(true, false);
	}

	public VPackWithoutTypeHintTest(final boolean useTypeHint) {
		this.useTypeHint = useTypeHint;
	}

	public static class NestedIterable {
		public Iterable<String> value;
	}

	@Test
	public void nestedIterableWithoutTypeInformation() {
		NestedCollection input = new NestedCollection();
		input.value = Arrays.asList("a", "b", "c");

		final VPack vpack = new VPack.Builder().useTypeHint(useTypeHint).build();
		final VPackSlice slice = vpack.serialize(input);

		assertThat(slice.isObject(), is(true));
		assertThat(slice.get("value").isArray(), is(true));
		if (!useTypeHint)
			assertThat(slice.get("_class").isNone(), is(true));

		NestedIterable output = vpack.deserialize(slice, NestedIterable.class);
		assertThat(output.value, instanceOf(List.class));

		assertThat(output.value, equalTo(input.value));
	}

	public static class NestedCollection {
		public Collection<String> value;
	}

	@Test
	public void nestedCollectionWithoutTypeInformation() {
		NestedCollection input = new NestedCollection();
		input.value = Arrays.asList("a", "b", "c");

		final VPack vpack = new VPack.Builder().useTypeHint(useTypeHint).build();
		final VPackSlice slice = vpack.serialize(input);

		assertThat(slice.isObject(), is(true));
		assertThat(slice.get("value").isArray(), is(true));
		if (!useTypeHint)
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
		Map<String, String> map = new HashMap<>();
		map.put("a", "b");

		NestedMap input = new NestedMap();
		input.value = map;

		final VPack vpack = new VPack.Builder().useTypeHint(useTypeHint).build();
		final VPackSlice slice = vpack.serialize(input);

		assertThat(slice.isObject(), is(true));
		assertThat(slice.get("value").isObject(), is(true));
		if (!useTypeHint) {
			assertThat(slice.get("_class").isNone(), is(true));
			assertThat(slice.get("value").get("_class").isNone(), is(true));
		}
		NestedMap output = vpack.deserialize(slice, NestedMap.class);
		assertThat(output.value, instanceOf(Map.class));

		assertThat(output.value, equalTo(input.value));
	}

	public static class NestedObject {
		public Object value;
	}

	@Test
	public void nestedObjectListWithoutTypeInformation() {
		NestedObject input = new NestedObject();
		input.value = Arrays.asList("a", "b", "c");

		final VPack vpack = new VPack.Builder().useTypeHint(useTypeHint).build();
		final VPackSlice slice = vpack.serialize(input);
		assertThat(slice.isObject(), is(true));
		assertThat(slice.get("value").isArray(), is(true));
		if (!useTypeHint)
			assertThat(slice.get("_class").isNone(), is(true));

		NestedObject output = vpack.deserialize(slice, NestedObject.class);
		assertThat(output.value, instanceOf(List.class));

		assertThat(output.value, equalTo(input.value));
	}

	@Test
	public void nestedObjectMapWithoutTypeInformation() {
		Map<String, String> map = new HashMap<>();
		map.put("a", "b");

		NestedObject input = new NestedObject();
		input.value = map;

		final VPack vpack = new VPack.Builder().useTypeHint(useTypeHint).build();
		final VPackSlice slice = vpack.serialize(input);
		assertThat(slice.isObject(), is(true));
		assertThat(slice.get("value").isObject(), is(true));
		if (!useTypeHint) {
			assertThat(slice.get("_class").isNone(), is(true));
			assertThat(slice.get("value").get("_class").isNone(), is(true));
		}

		NestedObject output = vpack.deserialize(slice, NestedObject.class);
		assertThat(output.value, instanceOf(Map.class));

		((Map<?, ?>) output.value).remove("_class");
		assertThat(output.value, equalTo(input.value));
	}

	public static class NestedGeneric<T> {
		public T value;
	}

	@Test
	public void nestedGenericOfCollectionWithoutTypeInformation() {
		NestedGeneric<Collection<String>> input = new NestedGeneric<>();
		input.value = Arrays.asList("a", "b", "c");

		final VPack vpack = new VPack.Builder().useTypeHint(useTypeHint).build();
		final VPackSlice slice = vpack.serialize(input);

		assertThat(slice.isObject(), is(true));
		assertThat(slice.get("value").isArray(), is(true));
		if (!useTypeHint)
			assertThat(slice.get("_class").isNone(), is(true));

		NestedGeneric<?> output = vpack.deserialize(slice, NestedGeneric.class);
		assertThat(output.value, instanceOf(List.class));

		assertThat(output.value, equalTo(input.value));
	}

	@Test
	public void nestedGenericOfMapWithoutTypeInformation() {
		Map<String, String> map = new HashMap<>();
		map.put("a", "b");

		NestedGeneric<Map<String, String>> input = new NestedGeneric<>();
		input.value = map;

		final VPack vpack = new VPack.Builder().useTypeHint(useTypeHint).build();
		final VPackSlice slice = vpack.serialize(input);

		assertThat(slice.isObject(), is(true));
		assertThat(slice.get("value").isObject(), is(true));
		if (!useTypeHint) {
			assertThat(slice.get("_class").isNone(), is(true));
			assertThat(slice.get("value").get("_class").isNone(), is(true));
		}

		NestedGeneric<?> output = vpack.deserialize(slice, NestedGeneric.class);
		assertThat(output.value, instanceOf(Map.class));

		((Map<?, ?>) output.value).remove("_class");
		assertThat(output.value, equalTo(input.value));
	}

	@Test
	public void nestedGenericOfObjectWithoutTypeInformation() {
		Map<String, String> map = new HashMap<>();
		map.put("a", "b");

		NestedGeneric<Object> input = new NestedGeneric<>();
		input.value = map;

		final VPack vpack = new VPack.Builder().useTypeHint(useTypeHint).build();
		final VPackSlice slice = vpack.serialize(input);

		assertThat(slice.isObject(), is(true));
		assertThat(slice.get("value").isObject(), is(true));
		if (!useTypeHint) {
			assertThat(slice.get("_class").isNone(), is(true));
			assertThat(slice.get("value").get("_class").isNone(), is(true));
		}

		NestedGeneric<?> output = vpack.deserialize(slice, NestedGeneric.class);
		assertThat(output.value, instanceOf(Map.class));

		((Map<?, ?>) output.value).remove("_class");
		assertThat(output.value, equalTo(input.value));
	}

}
