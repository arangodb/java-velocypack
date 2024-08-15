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

package com.arangodb.velocypack.internal.util;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * @author Mark Vollmary
 *
 */
public class DateUtil {

	private final static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
			.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
			.withZone(ZoneOffset.UTC);

	private DateUtil() {
		super();
	}

	public static java.util.Date toDate(final byte[] array, final int offset, final int length) {
		final long milliseconds = NumberUtil.toLong(array, offset, length);
		return new java.util.Date(milliseconds);
	}

	public static java.sql.Date toSQLDate(final byte[] array, final int offset, final int length) {
		final long milliseconds = NumberUtil.toLong(array, offset, length);
		return new java.sql.Date(milliseconds);
	}

	public static java.sql.Timestamp toSQLTimestamp(final byte[] array, final int offset, final int length) {
		final long milliseconds = NumberUtil.toLong(array, offset, length);
		return new java.sql.Timestamp(milliseconds);
	}

	public static java.util.Date parse(final String source) throws ParseException {
		try {
			return new Date(ZonedDateTime.parse(source).toInstant().toEpochMilli());
		} catch (DateTimeParseException e) {
			throw new ParseException("Unparseable date: \"" + e.getParsedString() + "\"", e.getErrorIndex());
		}
	}

	public static String format(final java.util.Date date) {
		return DATE_FORMATTER.format(ZonedDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneOffset.UTC));
	}

}
