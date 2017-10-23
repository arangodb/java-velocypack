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

import java.math.BigInteger;

/**
 * @author Mark Vollmary
 *
 */
public class NumberUtil {

	private static final int DOUBLE_BYTES = Double.SIZE / Byte.SIZE;
	private static final int LONG_BYTES = Long.SIZE / Byte.SIZE;

	private NumberUtil() {
		super();
	}

	public static double toDouble(final byte[] array, final int offset, final int length) {
		return Double.longBitsToDouble(toLong(array, offset, DOUBLE_BYTES));
	}

	public static long toLong(final byte[] array, final int offset, final int length) {
		return toLong(array, offset, length, false);
	}

	public static long toLong(final byte[] array, final int offset, final int length, final boolean fillNegativeBytes) {
		long result = 0;
		if (fillNegativeBytes && length < LONG_BYTES && array[(offset + length - 1)] <= (byte) -1) {
			for (int i = 0; i <= 8 - length; i++) {
				result <<= LONG_BYTES;
				result |= ((byte) -1 & 0xFF);
			}
		}
		for (int i = (offset + length - 1); i >= offset; i--) {
			result <<= LONG_BYTES;
			result |= (array[i] & 0xFF);
		}
		return result;
	}

	public static BigInteger toBigInteger(final byte[] array, final int offset, final int length) {
		BigInteger result = new BigInteger(1, new byte[] {});
		for (int i = (offset + length - 1); i >= offset; i--) {
			result = result.shiftLeft(LONG_BYTES);
			result = result.or(BigInteger.valueOf(array[i] & 0xFF));
		}
		return result;
	}

	/**
	 * read a variable length integer in unsigned LEB128 format
	 */
	public static long readVariableValueLength(final byte[] array, final int offset, final boolean reverse) {
		long len = 0;
		byte v;
		long p = 0;
		int i = offset;
		do {
			v = array[i];
			len += ((long) (v & (byte) 0x7f)) << p;
			p += 7;
			if (reverse) {
				--i;
			} else {
				++i;
			}
		} while ((v & (byte) 0x80) != 0);
		return len;
	}

	/**
	 * calculate the length of a variable length integer in unsigned LEB128 format
	 */
	public static long getVariableValueLength(final long value) {
		long len = 1;
		long val = value;
		while (val >= 0x80) {
			val >>= 7;
			++len;
		}
		return len;
	}

}
