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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public interface VPackSetupContext<C extends VPackSetupContext<C>> {

	<T> C registerSerializer(final Type type, final VPackSerializer<T> serializer);

	<T> C registerEnclosingSerializer(final Type type, final VPackSerializer<T> serializer);

	<T> C registerDeserializer(final Type type, final VPackDeserializer<T> deserializer);

	<T> C registerDeserializer(final Type type, final VPackDeserializer<T> deserializer, boolean includeNullValues);

	<T> C registerDeserializer(final String fieldName, final Type type, final VPackDeserializer<T> deserializer);

	<T> C registerDeserializer(
		final String fieldName,
		final Type type,
		final VPackDeserializer<T> deserializer,
		boolean includeNullValues);

	<T> C registerInstanceCreator(final Type type, final VPackInstanceCreator<T> creator);

	C buildUnindexedArrays(final boolean buildUnindexedArrays);

	C buildUnindexedObjects(final boolean buildUnindexedObjects);

	C serializeNullValues(final boolean serializeNullValues);

	C fieldNamingStrategy(final VPackFieldNamingStrategy fieldNamingStrategy);

	<A extends Annotation> C annotationFieldFilter(
		final Class<A> type,
		final VPackAnnotationFieldFilter<A> fieldFilter);

	<A extends Annotation> C annotationFieldNaming(
		final Class<A> type,
		final VPackAnnotationFieldNaming<A> fieldNaming);

	C registerKeyMapAdapter(final Type type, final VPackKeyMapAdapter<?> adapter);

	C registerModule(VPackModule module);

	C registerModules(VPackModule... modules);

}
