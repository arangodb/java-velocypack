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

/**
 * @author Mark Vollmary
 *
 */
public class VPackUtil {

	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String toHex(final VPackSlice vpack) {
		final byte[] bytes = vpack.getBuffer();
		final int bytesLength = vpack.getByteSize();
		final int bytesStart = vpack.getStart();
		final char[] hexChars = new char[bytesLength * 2];
		for (int j = 0; j < bytes.length; j++) {
			final int v = bytes[j+bytesStart] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars).replaceAll("(.{32})", "$1\n").replaceAll("(.{2})", "0x$1 ").toLowerCase();
	}

}
