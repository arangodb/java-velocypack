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

import com.arangodb.velocypack.exception.VPackBuilderException;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocypack.internal.util.DateUtil;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Mark Vollmary
 *
 */
public class VPackParser {

	private static final char OBJECT_OPEN = '{';
	private static final char OBJECT_CLOSE = '}';
	private static final char ARRAY_OPEN = '[';
	private static final char ARRAY_CLOSE = ']';
	private static final char FIELD = ':';
	private static final char SEPARATOR = ',';
	private static final String NULL = "null";
	private static final String NON_REPRESENTABLE_TYPE = "(non-representable type)";

	private static final JsonFactory jf = new JsonFactory();
	private final Map<ValueType, VPackJsonDeserializer> deserializers;
	private final Map<String, Map<ValueType, VPackJsonDeserializer>> deserializersByName;
	private final Map<Class<?>, VPackJsonSerializer<?>> serializers;
	private final Map<String, Map<Class<?>, VPackJsonSerializer<?>>> serializersByName;

	public static class Builder implements VPackParserSetupContext<Builder> {
		private final Map<ValueType, VPackJsonDeserializer> deserializers;
		private final Map<String, Map<ValueType, VPackJsonDeserializer>> deserializersByName;
		private final Map<Class<?>, VPackJsonSerializer<?>> serializers;
		private final Map<String, Map<Class<?>, VPackJsonSerializer<?>>> serializersByName;

		public Builder() {
			super();
			deserializers = new HashMap<>();
			deserializersByName = new HashMap<>();
			serializers = new HashMap<>();
			serializersByName = new HashMap<>();
		}

		@Override
		public VPackParser.Builder registerDeserializer(
			final String attribute,
			final ValueType type,
			final VPackJsonDeserializer deserializer) {
			Map<ValueType, VPackJsonDeserializer> byName = deserializersByName.get(attribute);
			if (byName == null) {
				byName = new HashMap<>();
				deserializersByName.put(attribute, byName);
			}
			byName.put(type, deserializer);
			return this;
		}

		@Override
		public VPackParser.Builder registerDeserializer(
			final ValueType type,
			final VPackJsonDeserializer deserializer) {
			deserializers.put(type, deserializer);
			return this;
		}

		@Override
		public <T> VPackParser.Builder registerSerializer(
			final String attribute,
			final Class<T> type,
			final VPackJsonSerializer<T> serializer) {
			Map<Class<?>, VPackJsonSerializer<?>> byName = serializersByName.get(attribute);
			if (byName == null) {
				byName = new HashMap<>();
				serializersByName.put(attribute, byName);
			}
			byName.put(type, serializer);
			return this;
		}

		@Override
		public <T> VPackParser.Builder registerSerializer(
			final Class<T> type,
			final VPackJsonSerializer<T> serializer) {
			serializers.put(type, serializer);
			return this;
		}

		@Override
		public Builder registerModule(final VPackParserModule module) {
			module.setup(VPackParser.Builder.this);
			return this;
		}

		@Override
		public Builder registerModules(final VPackParserModule... modules) {
			for (final VPackParserModule module : modules) {
				registerModule(module);
			}
			return this;
		}

		public synchronized VPackParser build() {
			return new VPackParser(new HashMap<>(serializers),
					new HashMap<>(serializersByName),
					new HashMap<>(deserializers), new HashMap<>(deserializersByName));
		}
	}

	/**
	 * @deprecated use {@link VPack.Builder#build()} instead
	 */
	@Deprecated
	public VPackParser() {
		this(new HashMap<Class<?>, VPackJsonSerializer<?>>(),
				new HashMap<String, Map<Class<?>, VPackJsonSerializer<?>>>(),
				new HashMap<ValueType, VPackJsonDeserializer>(),
				new HashMap<String, Map<ValueType, VPackJsonDeserializer>>());
	}

	private VPackParser(final Map<Class<?>, VPackJsonSerializer<?>> serializers,
		final Map<String, Map<Class<?>, VPackJsonSerializer<?>>> serializersByName,
		final Map<ValueType, VPackJsonDeserializer> deserializers,
		final Map<String, Map<ValueType, VPackJsonDeserializer>> deserializersByName) {
		super();
		this.serializers = serializers;
		this.serializersByName = serializersByName;
		this.deserializers = deserializers;
		this.deserializersByName = deserializersByName;
	}

	/**
	 * @deprecated use {@link VPackParser.Builder#registerDeserializer(String, ValueType, VPackJsonDeserializer)}
	 *             instead
	 * @param attribute
	 * @param type
	 * @param deserializer
	 * @return this
	 */
	@Deprecated
	public VPackParser registerDeserializer(
		final String attribute,
		final ValueType type,
		final VPackJsonDeserializer deserializer) {
		Map<ValueType, VPackJsonDeserializer> byName = deserializersByName.get(attribute);
		if (byName == null) {
			byName = new HashMap<>();
			deserializersByName.put(attribute, byName);
		}
		byName.put(type, deserializer);
		return this;
	}

	/**
	 * @deprecated use {@link VPackParser.Builder#registerDeserializer(ValueType, VPackJsonDeserializer)} instead
	 * @param type
	 * @param deserializer
	 * @return this
	 */
	@Deprecated
	public VPackParser registerDeserializer(final ValueType type, final VPackJsonDeserializer deserializer) {
		deserializers.put(type, deserializer);
		return this;
	}

	/**
	 * @deprecated use {@link VPackParser.Builder#registerSerializer(String, Class, VPackJsonSerializer)} instead
	 * @param attribute
	 * @param type
	 * @param serializer
	 * @return this
	 */
	@Deprecated
	public <T> VPackParser registerSerializer(
		final String attribute,
		final Class<T> type,
		final VPackJsonSerializer<T> serializer) {
		Map<Class<?>, VPackJsonSerializer<?>> byName = serializersByName.get(attribute);
		if (byName == null) {
			byName = new HashMap<>();
			serializersByName.put(attribute, byName);
		}
		byName.put(type, serializer);
		return this;
	}

	/**
	 * @deprecated use {@link VPackParser.Builder#registerSerializer(Class, VPackJsonSerializer)} instead
	 * @param type
	 * @param serializer
	 * @return this
	 */
	@Deprecated
	public <T> VPackParser registerSerializer(final Class<T> type, final VPackJsonSerializer<T> serializer) {
		serializers.put(type, serializer);
		return this;
	}

	public String toJson(final VPackSlice vpack) throws VPackException {
		return toJson(vpack, false);
	}

	public String toJson(final VPackSlice vpack, final boolean includeNullValues) throws VPackException {
		final StringBuilder json = new StringBuilder();
		parse(null, null, vpack, json, includeNullValues);
		return json.toString();
	}

	private VPackJsonDeserializer getDeserializer(final String attribute, final ValueType type) {
		VPackJsonDeserializer deserializer = null;
		final Map<ValueType, VPackJsonDeserializer> byName = deserializersByName.get(attribute);
		if (byName != null) {
			deserializer = byName.get(type);
		}
		if (deserializer == null) {
			deserializer = deserializers.get(type);
		}
		return deserializer;
	}

	private VPackJsonSerializer<?> getSerializer(final String attribute, final Class<?> type) {
		VPackJsonSerializer<?> serializer = null;
		final Map<Class<?>, VPackJsonSerializer<?>> byName = serializersByName.get(attribute);
		if (byName != null) {
			serializer = byName.get(type);
		}
		if (serializer == null) {
			serializer = serializers.get(type);
		}
		return serializer;
	}

	private void parse(
		final VPackSlice parent,
		final String attribute,
		final VPackSlice value,
		final StringBuilder json,
		final boolean includeNullValues) throws VPackException {

		VPackJsonDeserializer deserializer = null;
		if (attribute != null) {
			appendField(attribute, json);
			deserializer = getDeserializer(attribute, value.getType());
		}
		if (deserializer != null) {
			deserializer.deserialize(parent, attribute, value, json);
		} else {
			if (value.isObject()) {
				parseObject(value, json, includeNullValues);
			} else if (value.isArray()) {
				parseArray(value, json, includeNullValues);
			} else if (value.isBoolean()) {
				json.append(value.getAsBoolean());
			} else if (value.isString()) {
				json.append("\"");
				json.append(value.getAsString().replace("\"", "\\\""));
				json.append("\"");
			} else if (value.isDouble()) {
				json.append(value.getAsDouble());
			} else if (value.isInt()) {
				json.append(value.getAsLong());
			} else if (value.isNumber()) {
				json.append(value.getAsNumber());
			} else if (value.isDate()) {
				json.append("\"");
				json.append(DateUtil.format(value.getAsDate()).replace("\"", "\\\""));
				json.append("\"");
			} else if (value.isNull()) {
				json.append(NULL);
			} else {
				json.append((NON_REPRESENTABLE_TYPE));
			}
		}
	}

	private static void appendField(final String attribute, final StringBuilder json) {
		json.append("\"");
		json.append(attribute.replace("\"", "\\\""));
		json.append("\"");
		json.append(FIELD);
	}

	private void parseObject(final VPackSlice value, final StringBuilder json, final boolean includeNullValues)
			throws VPackException {
		json.append(OBJECT_OPEN);
		int added = 0;
		for (final Iterator<Entry<String, VPackSlice>> iterator = value.objectIterator(); iterator.hasNext();) {
			final Entry<String, VPackSlice> next = iterator.next();
			final VPackSlice nextValue = next.getValue();
			if (!nextValue.isNull() || includeNullValues) {
				if (added++ > 0) {
					json.append(SEPARATOR);
				}
				parse(value, next.getKey(), nextValue, json, includeNullValues);
			}
		}
		json.append(OBJECT_CLOSE);
	}

	private void parseArray(final VPackSlice value, final StringBuilder json, final boolean includeNullValues)
			throws VPackException {
		json.append(ARRAY_OPEN);
		int added = 0;
		for (final Iterator<VPackSlice> iterator = value.arrayIterator(); iterator.hasNext();) {
			final VPackSlice valueAt = iterator.next();
			if (!valueAt.isNull() || includeNullValues) {
				if (added++ > 0) {
					json.append(SEPARATOR);
				}
				parse(value, null, valueAt, json, includeNullValues);
			}
		}
		json.append(ARRAY_CLOSE);
	}

	public VPackSlice fromJson(final String json) throws VPackException {
		return fromJson(json, false);
	}

	public VPackSlice fromJson(final String json, final VPackBuilder builder) throws VPackException {
		return fromJson(json, false, builder);
	}

	public VPackSlice fromJson(final String json, final boolean includeNullValues) throws VPackException {
		return fromJson(json, includeNullValues, new VPackBuilder());
	}

	public VPackSlice fromJson(final String json, final boolean includeNullValues, final VPackBuilder builder) throws VPackException {
		try {
			parse(json, builder, includeNullValues);
		} catch (final IOException e) {
			throw new VPackBuilderException(e);
		}
		return builder.slice();
	}

	public VPackSlice fromJson(final Iterable<String> jsons) throws VPackException {
		return fromJson(jsons, false);
	}

	public VPackSlice fromJson(final Iterable<String> jsons, final VPackBuilder builder) throws VPackException {
		return fromJson(jsons, false, builder);
	}

	public VPackSlice fromJson(final Iterable<String> jsons, final boolean includeNullValues) throws VPackException {
		return fromJson(jsons, includeNullValues, new VPackBuilder());
	}

	public VPackSlice fromJson(final Iterable<String> jsons, final boolean includeNullValues, final VPackBuilder builder) throws VPackException {
		try {
			builder.add(ValueType.ARRAY);
			for (final String json : jsons) {
				parse(json, builder, includeNullValues);
			}
		} catch (final IOException e) {
			throw new VPackBuilderException(e);
		}
		builder.close();
		return builder.slice();
	}

	private void parse(final String json, final VPackBuilder builder, final boolean includeNullValues)
			throws IOException {
		final JsonParser parser = jf.createParser(json);
		String fieldName = null;
		JsonToken token;
		while (!parser.isClosed() && (token = parser.nextToken()) != null) {
			switch (token) {
				case START_OBJECT:
				case VALUE_EMBEDDED_OBJECT:
					builder.add(fieldName, ValueType.OBJECT);
					fieldName = null;
					break;
				case START_ARRAY:
				builder.add(fieldName, ValueType.ARRAY);
				fieldName = null;
				break;
			case END_OBJECT:
			case END_ARRAY:
				builder.close();
				break;
			case FIELD_NAME:
				fieldName = parser.getCurrentName();
				break;
			case VALUE_TRUE:
			case VALUE_FALSE:
				parseValue(builder, fieldName, parser.getBooleanValue());
				fieldName = null;
				break;
			case VALUE_NULL:
				if (includeNullValues) {
					builder.add(fieldName, ValueType.NULL);
				}
				fieldName = null;
				break;
			case VALUE_NUMBER_FLOAT:
				parseValue(builder, fieldName, parser.getDoubleValue());
				fieldName = null;
				break;
			case VALUE_NUMBER_INT:
				parseValue(builder, fieldName, parser.getLongValue());
				fieldName = null;
				break;
			case VALUE_STRING:
				parseValue(builder, fieldName, parser.getValueAsString());
				fieldName = null;
				break;
			case NOT_AVAILABLE:
				fieldName = null;
			default:
				break;
			}
		}

	}

	@SuppressWarnings("unchecked")
	private void parseValue(final VPackBuilder builder, final String fieldName, final Object value) {
		final VPackJsonSerializer<?> serializer = getSerializer(fieldName, value.getClass());
		if (serializer != null) {
			((VPackJsonSerializer<Object>) serializer).serialize(builder, fieldName, value);
		} else if (String.class.isAssignableFrom(value.getClass())) {
			builder.add(fieldName, (String) value);
		} else if (Boolean.class.isAssignableFrom(value.getClass())) {
			builder.add(fieldName, (Boolean) value);
		} else if (Double.class.isAssignableFrom(value.getClass())) {
			builder.add(fieldName, (Double) value);
		} else if (Long.class.isAssignableFrom(value.getClass())) {
			builder.add(fieldName, (Long) value);
		}
	}

	public static String toJSONString(final String text) {
		final StringWriter writer = new StringWriter();
		try {
			final JsonGenerator generator = jf.createGenerator(writer);
			generator.writeString(text);
			generator.close();
		} catch (final IOException e) {
			throw new VPackBuilderException(e);
		}
		return writer.toString();
	}

}