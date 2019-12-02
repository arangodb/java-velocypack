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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mark Vollmary
 *
 */
public class ObjectArrayUtil {

	private static final int[] FIRST_SUB_MAP = {
		0, // 0x00 None
		1, // 0x01 empty array
		2, // 0x02 array without index table
		3, // 0x03 array without index table
		5, // 0x04 array without index table
		9, // 0x05 array without index table
		3, // 0x06 array with index table
		5, // 0x07 array with index table
		9, // 0x08 array with index table
		9, // 0x09 array with index table
		1, // 0x0a empty object
		3, // 0x0b object with sorted index table
		5, // 0x0c object with sorted index table
		9, // 0x0d object with sorted index table
		9, // 0x0e object with sorted index table
		3, // 0x0f object with unsorted index table
		5, // 0x10 object with unsorted index table
		9, // 0x11 object with unsorted index table
		9 //  0x12 object with unsorted index table
	};

	public static int getFirstSubMap(final byte key) {
		return FIRST_SUB_MAP[key & 0xff];
	}

	private static final int[] OFFSET_SIZE = {
		0, // 0x00 None
		1, // 0x01 empty array
		1, // 0x02 array without index table
		2, // 0x03 array without index table
		4, // 0x04 array without index table
		8, // 0x05 array without index table
		1, // 0x06 array with index table
		2, // 0x07 array with index table
		4, // 0x08 array with index table
		8, // 0x09 array with index table
		1, // 0x0a empty object
		1, // 0x0b object with sorted index table
		2, // 0x0c object with sorted index table
		4, // 0x0d object with sorted index table
		8, // 0x0e object with sorted index table
		1, // 0x0f object with unsorted index table
		2, // 0x10 object with unsorted index table
		4, // 0x11 object with unsorted index table
		8 //  0x12 object with unsorted index table
	};

	private ObjectArrayUtil() {
		super();
	}

	public static int getOffsetSize(final byte key) {
		return OFFSET_SIZE[key & 0xff];
	}
}
