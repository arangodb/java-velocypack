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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.arangodb.velocypack.exception.VPackException;

/**
 * @author Mark Vollmary
 *
 */
public class VPackModuleTest {

	private static class TestVPackModule implements VPackModule {

		@Override
		public <C extends VPackSetupContext<C>> void setup(final C context) {
			context.registerSerializer(String.class, new VPackSerializer<String>() {

				@Override
				public void serialize(
					final VPackBuilder builder,
					final String attribute,
					final String value,
					final VPackSerializationContext context) throws VPackException {
					builder.add(attribute, "test");
				}
			});
		}

	}

	@Test
	public void registerModule() {
		final VPack vpack = new VPack.Builder().registerModule(new TestVPackModule()).build();
		final VPackSlice value = vpack.serialize("notest");
		assertThat(value.isString(), is(true));
		assertThat(value.getAsString(), is("test"));
	}

}
