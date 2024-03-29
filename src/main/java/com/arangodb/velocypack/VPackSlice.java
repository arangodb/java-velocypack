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

import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocypack.exception.VPackKeyTypeException;
import com.arangodb.velocypack.exception.VPackNeedAttributeTranslatorException;
import com.arangodb.velocypack.exception.VPackValueTypeException;
import com.arangodb.velocypack.internal.VPackAttributeTranslatorImpl;
import com.arangodb.velocypack.internal.util.BinaryUtil;
import com.arangodb.velocypack.internal.util.DateUtil;
import com.arangodb.velocypack.internal.util.NumberUtil;
import com.arangodb.velocypack.internal.util.ObjectArrayUtil;
import com.arangodb.velocypack.internal.util.ValueLengthUtil;
import com.arangodb.velocypack.internal.util.ValueTypeUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Mark Vollmary
 *
 */
public class VPackSlice implements Serializable {

	private static final long serialVersionUID = -3452953589283603980L;

	private static final byte[] NONE_SLICE_DATA = new byte[] { 0x00 };
	public static final VPackSlice NONE_SLICE = new VPackSlice();

	public static final VPackAttributeTranslator attributeTranslator = new VPackAttributeTranslatorImpl();

	private final byte[] vpack;
	private final int start;

	protected VPackSlice() {
		this(NONE_SLICE_DATA, 0);
	}

	public VPackSlice(final byte[] vpack) {
		this(vpack, 0);
	}

	public VPackSlice(final byte[] vpack, final int start) {
		super();
		this.vpack = vpack;
		this.start = start;
	}

	public byte head() {
		return vpack[start];
	}

	public byte[] getBuffer() {
		return vpack;
	}

	/**
	 * @return VPackSlice buffer without trailing zeros
	 */
	public byte[] toByteArray() {
		return Arrays.copyOfRange(vpack, start, start + getByteSize());
	}

	public int getStart() {
		return start;
	}

	public int getValueStart() {
		return start + tagsOffset(start);
	}

	public VPackSlice value() {
		return isTagged() ? new VPackSlice(vpack, getValueStart()) : this;
	}

	public ValueType getType() {
		return ValueTypeUtil.get(head());
	}

	private int length() {
		return ValueLengthUtil.get(head()) - 1;
	}

	public boolean isType(final ValueType type) {
		return getType() == type;
	}

	public boolean isNone() {
		return isType(ValueType.NONE);
	}

	public boolean isNull() {
		return isType(ValueType.NULL);
	}

	public boolean isIllegal() {
		return isType(ValueType.ILLEGAL);
	}

	public boolean isBoolean() {
		return isType(ValueType.BOOL);
	}

	public boolean isTrue() {
		return head() == 0x1a;
	}

	public boolean isFalse() {
		return head() == 0x19;
	}

	public boolean isArray() {
		return isType(ValueType.ARRAY);
	}

	public boolean isObject() {
		return isType(ValueType.OBJECT);
	}

	public boolean isDouble() {
		return isType(ValueType.DOUBLE);
	}

	public boolean isDate() {
		return isType(ValueType.UTC_DATE);
	}

	public boolean isExternal() {
		return isType(ValueType.EXTERNAL);
	}

	public boolean isMinKey() {
		return isType(ValueType.MIN_KEY);
	}

	public boolean isMaxKey() {
		return isType(ValueType.MAX_KEY);
	}

	public boolean isInt() {
		return isType(ValueType.INT);
	}

	public boolean isUInt() {
		return isType(ValueType.UINT);
	}

	public boolean isSmallInt() {
		return isType(ValueType.SMALLINT);
	}

	public boolean isInteger() {
		return isInt() || isUInt() || isSmallInt();
	}

	public boolean isByte() {
		return isInteger();
	}

	public boolean isNumber() {
		return isInteger() || isDouble();
	}

	public boolean isString() {
		return isType(ValueType.STRING);
	}

	public boolean isBinary() {
		return isType(ValueType.BINARY);
	}

	public boolean isBCD() {
		return isType(ValueType.BCD);
	}

	public boolean isCustom() {
		return isType(ValueType.CUSTOM);
	}

	public boolean isTagged() {
		return isType(ValueType.TAGGED);
	}

	public long getFirstTag() {
		if(isTagged()) {
			if(vpack[start] == (byte)0xee) {
				return NumberUtil.toLong(vpack, start+1, 1, false);
			} else if(vpack[start] == (byte)0xef) {
				return NumberUtil.toLong(vpack, start+1, 8, false);
			} else {
				throw new IllegalStateException("Invalid tag type ID");
			}
		}

		return 0;
	}

	public List<Long> getTags() {
		if(!isTagged()) {
			return Collections.emptyList();
		}

		List<Long> ret = new ArrayList<>();
		int start = this.start;

		while(ValueTypeUtil.get(vpack[start]) == ValueType.TAGGED) {
			int offset;
			long tag;

			if(vpack[start] == (byte)0xee) {
				tag = NumberUtil.toLong(vpack, start+1, 1, false);
				offset = 2;
			} else if(vpack[start] == (byte)0xef) {
				tag = NumberUtil.toLong(vpack, start+1, 8, false);
				offset = 9;
			} else {
				throw new IllegalStateException("Invalid tag type ID");
			}

			ret.add(tag);
			start += offset;
		}

		return ret;
	}

	public boolean hasTag(long tagId) {
		int start = this.start;

		while(ValueTypeUtil.get(vpack[start]) == ValueType.TAGGED) {
			int offset;
			long tag;

			if(vpack[start] == (byte)0xee) {
				tag = NumberUtil.toLong(vpack, start+1, 1, false);
				offset = 2;
			} else if(vpack[start] == (byte)0xef) {
				tag = NumberUtil.toLong(vpack, start+1, 8, false);
				offset = 9;
			} else {
				throw new IllegalStateException("Invalid tag type ID");
			}

			if(tag == tagId) {
				return true;
			}

			start += offset;
		}

		return false;
	}

	public boolean getAsBoolean() {
		if (!isBoolean()) {
			throw new VPackValueTypeException(ValueType.BOOL);
		}
		return isTrue();
	}

	public double getAsDouble() {
		return getAsNumber().doubleValue();
	}

	private double getAsDoubleUnchecked() {
		return NumberUtil.toDouble(vpack, start + 1, length());
	}

	public BigDecimal getAsBigDecimal() {
		if (isString()) {
			return new BigDecimal(getAsString());
		} else if (isDouble()) {
			return BigDecimal.valueOf(getAsDouble());
		} else if (isSmallInt() || isInt()) {
			return BigDecimal.valueOf(getAsLong());
		} else if (isUInt()) {
			return new BigDecimal(NumberUtil.toBigInteger(vpack, start + 1, length()));
		} else {
			throw new VPackValueTypeException(ValueType.STRING, ValueType.DOUBLE);
		}
	}

	private long getSmallInt() {
		final byte head = head();
		final long smallInt;
		if (head >= 0x30 && head <= 0x39) {
			smallInt = head - 0x30;
		} else /* if (head >= 0x3a && head <= 0x3f) */ {
			smallInt = head - 0x3a - 6;
		}
		return smallInt;
	}

	private long getInt() {
		return NumberUtil.toLong(vpack, start + 1, length(), true);
	}

	private long getUInt() {
		return NumberUtil.toLong(vpack, start + 1, length());
	}

	public Number getAsNumber() {
		final Number result;
		if (isSmallInt()) {
			result = getSmallInt();
		} else if (isInt()) {
			result = getInt();
		} else if (isUInt()) {
			result = getUInt();
		} else if (isDouble()) {
			result = getAsDoubleUnchecked();
		} else {
			throw new VPackValueTypeException(ValueType.INT, ValueType.UINT, ValueType.SMALLINT);
		}
		return result;
	}

	public long getAsLong() {
		return getAsNumber().longValue();
	}

	public int getAsInt() {
		return getAsNumber().intValue();
	}

	public float getAsFloat() {
		return getAsNumber().floatValue();
	}

	public short getAsShort() {
		return getAsNumber().shortValue();
	}

	public byte getAsByte() {
		return getAsNumber().byteValue();
	}

	public BigInteger getAsBigInteger() {
		if (isString()) {
			return new BigInteger(getAsString());
		} else if (isSmallInt() || isInt()) {
			return BigInteger.valueOf(getAsLong());
		} else if (isUInt()) {
			return NumberUtil.toBigInteger(vpack, start + 1, length());
		} else {
			throw new VPackValueTypeException(ValueType.STRING, ValueType.INT, ValueType.UINT, ValueType.SMALLINT);
		}
	}

	public Date getAsDate() {
		if (!isDate()) {
			throw new VPackValueTypeException(ValueType.UTC_DATE);
		}
		return DateUtil.toDate(vpack, start + 1, length());
	}

	public java.sql.Date getAsSQLDate() {
		if (!isDate()) {
			throw new VPackValueTypeException(ValueType.UTC_DATE);
		}
		return DateUtil.toSQLDate(vpack, start + 1, length());
	}

	public java.sql.Timestamp getAsSQLTimestamp() {
		if (!isDate()) {
			throw new VPackValueTypeException(ValueType.UTC_DATE);
		}
		return DateUtil.toSQLTimestamp(vpack, start + 1, length());
	}

	public String getAsString() {
		return getAsStringSlice().toString();
	}

	public VPackStringSlice getAsStringSlice() {
		if (!isString()) {
			throw new VPackValueTypeException(ValueType.STRING);
		}
		return isLongString() ? getLongString() : getShortString();
	}

	public char getAsChar() {
		return getAsString().charAt(0);
	}

	private boolean isLongString() {
		return head() == (byte) 0xbf;
	}

	private VPackStringSlice getShortString() {
		return new VPackStringSlice(vpack, start + 1, length());
	}

	private VPackStringSlice getLongString() {
		return new VPackStringSlice(vpack, start + 9, getLongStringLength());
	}

	private int getLongStringLength() {
		return (int) NumberUtil.toLong(vpack, start + 1, 8);
	}

	private int getStringLength() {
		return isLongString() ? getLongStringLength() : head() - 0x40;
	}

	public byte[] getAsBinary() {
		if (!isBinary()) {
			throw new VPackValueTypeException(ValueType.BINARY);
		}
		return BinaryUtil.toBinary(vpack, start + 1 + head() - ((byte) 0xbf), getBinaryLength());
	}

	public int getBinaryLength() {
		if (!isBinary()) {
			throw new VPackValueTypeException(ValueType.BINARY);
		}
		return getBinaryLengthUnchecked();
	}

	private int getBinaryLengthUnchecked() {
		return (int) NumberUtil.toLong(vpack, start + 1, head() - ((byte) 0xbf));
	}

	/**
	 * @return the number of members for an Array, Object or String
	 */
	public int getLength() {
		final long length;
		if (isString()) {
			length = getStringLength();
		} else if (!isArray() && !isObject()) {
			throw new VPackValueTypeException(ValueType.ARRAY, ValueType.OBJECT, ValueType.STRING);
		} else {
			final byte head = head();
			if (head == 0x01 || head == 0x0a) {
				// empty
				length = 0;
			} else if (head == 0x13 || head == 0x14) {
				// compact array or object
				final long end = NumberUtil.readVariableValueLength(vpack, start + 1, false);
				length = NumberUtil.readVariableValueLength(vpack, (int) (start + end - 1), true);
			} else {
				final int offsetsize = ObjectArrayUtil.getOffsetSize(head);
				final long end = NumberUtil.toLong(vpack, start + 1, offsetsize);
				if (head <= 0x05) {
					// array with no offset table or length
					final int dataOffset = findDataOffset();
					final VPackSlice first = new VPackSlice(vpack, start + dataOffset);
					length = (end - dataOffset) / first.getByteSize();
				} else if (offsetsize < 8) {
					length = NumberUtil.toLong(vpack, start + 1 + offsetsize, offsetsize);
				} else {
					length = NumberUtil.toLong(vpack, (int) (start + end - offsetsize), offsetsize);
				}
			}
		}
		return (int) length;
	}

	public int size() {
		return getLength();
	}

	/**
	 * Must be called for a nonempty array or object at start():
	 */
	protected int findDataOffset() {
		final int fsm = ObjectArrayUtil.getFirstSubMap(head());
		final int offset;
		if (fsm <= 2 && vpack[start + 2] != 0) {
			offset = 2;
		} else if (fsm <= 3 && vpack[start + 3] != 0) {
			offset = 3;
		} else if (fsm <= 5 && vpack[start + 6] != 0) {
			offset = 5;
		} else {
			offset = 9;
		}
		return offset;
	}

	public int getByteSize() {
		return getByteSize(start);
	}

	private int getByteSize(int start) {
		long size;
		final byte head = vpack[start];
		final int valueLength = ValueLengthUtil.get(head);
		if (valueLength != 0) {
			size = valueLength;
		} else {
			switch (ValueTypeUtil.get(head)) {
			case ARRAY:
			case OBJECT:
				if (head == 0x13 || head == 0x14) {
					// compact Array or Object
					size = NumberUtil.readVariableValueLength(vpack, start + 1, false);
				} else /* if (head <= 0x14) */ {
					size = NumberUtil.toLong(vpack, start + 1, ObjectArrayUtil.getOffsetSize(head));
				}
				break;
			case STRING:
				// long UTF-8 String
				size = NumberUtil.toLong(vpack, start + 1, 8) + 1 + 8;
				break;
			case BINARY:
				size = 1 + head - ((byte) 0xbf) + NumberUtil.toLong(vpack, start + 1, head - ((byte) 0xbf));
				break;
			case BCD:
				if (head <= (byte) 0xcf) {
					size = 1 + head - ((byte) 0xc7) + NumberUtil.toLong(vpack, start + 1, head - ((byte) 0xc7)) + 4;
				} else {
					size = 1 + head - ((byte) 0xcf) + NumberUtil.toLong(vpack, start + 1, head - ((byte) 0xcf)) + 4;
				}
				break;
			case TAGGED:
				int offset = tagsOffset(start);
				size = getByteSize(start + offset) + offset;
				break;
			case CUSTOM:
				if (head == (byte) 0xf4 || head == (byte) 0xf5 || head == (byte) 0xf6) {
					size = 2 + NumberUtil.toLong(vpack, start + 1, 1);
				} else if (head == (byte) 0xf7 || head == (byte) 0xf8 || head == (byte) 0xf9) {
					size = 3 + NumberUtil.toLong(vpack, start + 1, 2);
				} else if (head == (byte) 0xfa || head == (byte) 0xfb || head == (byte) 0xfc) {
					size = 5 + NumberUtil.toLong(vpack, start + 1, 4);
				} else /* if (head == 0xfd || head == 0xfe || head == 0xff) */ {
					size = 9 + NumberUtil.toLong(vpack, start + 1, 8);
				}
				break;
			default:
				// TODO
				throw new IllegalStateException("Invalid type for byteSize()");
			}
		}
		return (int) size;
	}

	private int tagOffset(int start) {
		byte v = vpack[start];

		if(ValueTypeUtil.get(v) == ValueType.TAGGED) {
			if(v == (byte)0xee) {
				return 2;
			} else if(v == (byte)0xef) {
				return 9;
			} else {
			  throw new IllegalStateException("Invalid tag type ID");
			}
		}

		return 0;
	}

	private int tagsOffset(int start) {
		int ret = 0;

		while(ValueTypeUtil.get(vpack[start]) == ValueType.TAGGED) {
			int offset = tagOffset(start);
			ret += offset;
			start += offset;
		}

		return ret;
	}

	/**
	 * @return array value at the specified index
	 * @throws VPackValueTypeException
	 */
	public VPackSlice get(final int index) {
		if (!isArray()) {
			throw new VPackValueTypeException(ValueType.ARRAY);
		}
		return getNth(index);
	}

	public VPackSlice get(final String attribute) throws VPackException {
		if (!isObject()) {
			throw new VPackValueTypeException(ValueType.OBJECT);
		}
		final byte head = head();
		VPackSlice result;
		if (attribute == null) {
			result = NONE_SLICE;
		} else if (head == 0x0a) {
			// special case, empty object
			result = NONE_SLICE;
		} else if (head == 0x14) {
			// compact Object
			result = getFromCompactObject(attribute);
		} else {
			final int offsetsize = ObjectArrayUtil.getOffsetSize(head);
			final long end = NumberUtil.toLong(vpack, start + 1, offsetsize);
			final long n;
			if (offsetsize < 8) {
				n = NumberUtil.toLong(vpack, start + 1 + offsetsize, offsetsize);
			} else {
				n = NumberUtil.toLong(vpack, (int) (start + end - offsetsize), offsetsize);
			}
			if (n == 1) {
				// Just one attribute, there is no index table!
				final VPackSlice key = new VPackSlice(vpack, start + findDataOffset());

				if (key.isString()) {
					if (key.isEqualString(attribute)) {
						result = new VPackSlice(vpack, key.start + key.getByteSize());
					} else {
						// no match
						result = NONE_SLICE;
					}
				} else if (key.isInteger()) {
					// translate key
					if (key.translateUnchecked().isEqualString(attribute)) {
						result = new VPackSlice(vpack, key.start + key.getByteSize());
					} else {
						// no match
						result = NONE_SLICE;
					}
				} else {
					// no match
					result = NONE_SLICE;
				}
			} else {
				final long ieBase = end - n * offsetsize - (offsetsize == 8 ? 8 : 0);

				// only use binary search for attributes if we have at least
				// this many entries
				// otherwise we'll always use the linear search
				final long sortedSearchEntriesThreshold = 4;

				final boolean sorted = head >= 0x0b && head <= 0x0e;
				if (sorted && n >= sortedSearchEntriesThreshold) {
					// This means, we have to handle the special case n == 1
					// only in the linear search!
					result = searchObjectKeyBinary(attribute, ieBase, offsetsize, n);
				} else {
					result = searchObjectKeyLinear(attribute, ieBase, offsetsize, n);
				}
			}
		}
		return result;
	}

	/**
	 * translates an integer key into a string, without checks
	 */
	protected VPackSlice translateUnchecked() {
		final VPackSlice result = attributeTranslator.translate(getAsInt());
		return result != null ? result : NONE_SLICE;
	}

	protected VPackSlice makeKey() throws VPackKeyTypeException, VPackNeedAttributeTranslatorException {
		if (isString()) {
			return this;
		}
		if (isInteger()) {
			return translateUnchecked();
		}
		throw new VPackKeyTypeException("Cannot translate key of this type");
	}

	private VPackSlice getFromCompactObject(final String attribute)
			throws VPackKeyTypeException, VPackNeedAttributeTranslatorException {
		for (final Iterator<Entry<String, VPackSlice>> iterator = objectIterator(); iterator.hasNext();) {
			final Entry<String, VPackSlice> next = iterator.next();
			if (next.getKey().equals(attribute)) {
				return next.getValue();
			}
		}
		// not found
		return NONE_SLICE;
	}

	private VPackSlice searchObjectKeyBinary(
		final String attribute,
		final long ieBase,
		final int offsetsize,
		final long n) throws VPackValueTypeException, VPackNeedAttributeTranslatorException {

		VPackSlice result;
		long l = 0;
		long r = n - 1;

		byte[] attributeBytes = attribute.getBytes(StandardCharsets.UTF_8);
		for (;;) {
			// midpoint
			final long index = l + ((r - l) / 2);
			final long offset = ieBase + index * offsetsize;
			final long keyIndex = NumberUtil.toLong(vpack, (int) (start + offset), offsetsize);
			final VPackSlice key = new VPackSlice(vpack, (int) (start + keyIndex));
			int res;
			if (key.isString()) {
				res = key.getAsStringSlice().compareToBytes(attributeBytes);
			} else if (key.isInteger()) {
				// translate key
				res = key.translateUnchecked().getAsStringSlice().compareToBytes(attributeBytes);
			} else {
				// invalid key
				result = NONE_SLICE;
				break;
			}
			if (res == 0) {
				// found
				result = new VPackSlice(vpack, key.start + key.getByteSize());
				break;
			}
			if (res > 0) {
				if (index == 0) {
					result = NONE_SLICE;
					break;
				}
				r = index - 1;
			} else {
				l = index + 1;
			}
			if (r < l) {
				result = NONE_SLICE;
				break;
			}
		}
		return result;
	}

	private VPackSlice searchObjectKeyLinear(
		final String attribute,
		final long ieBase,
		final int offsetsize,
		final long n) throws VPackValueTypeException, VPackNeedAttributeTranslatorException {

		VPackSlice result = NONE_SLICE;
		for (long index = 0; index < n; index++) {
			final long offset = ieBase + index * offsetsize;
			final long keyIndex = NumberUtil.toLong(vpack, (int) (start + offset), offsetsize);
			final VPackSlice key = new VPackSlice(vpack, (int) (start + keyIndex));
			if (key.isString()) {
				if (!key.isEqualString(attribute)) {
					continue;
				}
			} else if (key.isInteger()) {
				// translate key
				if (!key.translateUnchecked().isEqualString(attribute)) {
					continue;
				}
			} else {
				// invalid key type
				result = NONE_SLICE;
				break;
			}
			// key is identical. now return value
			result = new VPackSlice(vpack, key.start + key.getByteSize());
			break;
		}
		return result;

	}

	public VPackSlice keyAt(final int index) {
		if (!isObject()) {
			throw new VPackValueTypeException(ValueType.OBJECT);
		}
		return getNthKey(index);
	}

	public VPackSlice valueAt(final int index) {
		if (!isObject()) {
			throw new VPackValueTypeException(ValueType.OBJECT);
		}
		final VPackSlice key = getNthKey(index);
		return new VPackSlice(vpack, key.start + key.getByteSize());
	}

	private VPackSlice getNthKey(final int index) {
		return new VPackSlice(vpack, start + getNthOffset(index));
	}

	public VPackSlice getNth(final int index) {
		return new VPackSlice(vpack, start + getNthOffset(index));
	}

	/**
	 *
	 * @return the offset for the nth member from an Array or Object type
	 */
	private int getNthOffset(final int index) {
		final int offset;
		final byte head = head();
		if (head == 0x13 || head == 0x14) {
			// compact Array or Object
			offset = getNthOffsetFromCompact(index);
		} else if (head == 0x01 || head == 0x0a) {
			// special case: empty Array or empty Object
			throw new IndexOutOfBoundsException();
		} else {
			final long n;
			final int offsetsize = ObjectArrayUtil.getOffsetSize(head);
			final long end = NumberUtil.toLong(vpack, start + 1, offsetsize);
			int dataOffset = findDataOffset();
			if (head <= 0x05) {
				// array with no offset table or length
				final VPackSlice first = new VPackSlice(vpack, start + dataOffset);
				n = (end - dataOffset) / first.getByteSize();
			} else if (offsetsize < 8) {
				n = NumberUtil.toLong(vpack, start + 1 + offsetsize, offsetsize);
			} else {
				n = NumberUtil.toLong(vpack, (int) (start + end - offsetsize), offsetsize);
			}
			if (index >= n) {
				throw new IndexOutOfBoundsException();
			}
			if (head <= 0x05 || n == 1) {
				// no index table, but all array items have the same length
				// or only one item is in the array
				// now fetch first item and determine its length
				if (dataOffset == 0) {
					dataOffset = findDataOffset();
				}
				offset = dataOffset + index * new VPackSlice(vpack, start + dataOffset).getByteSize();
			} else {
				final long ieBase = end - n * offsetsize + index * offsetsize - (offsetsize == 8 ? 8 : 0);
				offset = (int) NumberUtil.toLong(vpack, (int) (start + ieBase), offsetsize);
			}
		}
		return offset;
	}

	/**
	 * @return the offset for the nth member from a compact Array or Object type
	 */
	private int getNthOffsetFromCompact(final int index) {
		final long end = NumberUtil.readVariableValueLength(vpack, start + 1, false);
		final long n = NumberUtil.readVariableValueLength(vpack, (int) (start + end - 1), true);
		if (index >= n) {
			throw new IndexOutOfBoundsException();
		}
		final byte head = head();
		long offset = 1 + NumberUtil.getVariableValueLength(end);
		long current = 0;
		while (current != index) {
			final long byteSize = new VPackSlice(vpack, (int) (start + offset)).getByteSize();
			offset += byteSize;
			if (head == 0x14) {
				offset += byteSize;
			}
			++current;
		}
		return (int) offset;
	}

	private boolean isEqualString(final String s) {
		final String string = getAsString();
		return string.equals(s);
	}

	private int compareString(final String s) {
		final String string = getAsString();
		return string.compareTo(s);
	}

	public Iterator<VPackSlice> arrayIterator() {
		if (isArray()) {
			return new ArrayIterator(this);
		} else {
			throw new VPackValueTypeException(ValueType.ARRAY);
		}
	}

	public Iterator<Entry<String, VPackSlice>> objectIterator() {
		if (isObject()) {
			return new ObjectIterator(this);
		} else {
			throw new VPackValueTypeException(ValueType.OBJECT);
		}
	}

	/**
	 * @return a pretty-printable schema of the VPackSlice, for debug purposes only
	 */
	public String getSchemaDescription() {
		StringBuilder sb = new StringBuilder();
		doGetSchemaDescription(sb, 0);
		return sb.toString();
	}

	private void doGetSchemaDescription(StringBuilder sb, int level) {
		ValueType type = getType();
		sb.append(" ");
		sb.append(type);

		if (type == ValueType.OBJECT) {
			level++;
			Iterator<Map.Entry<String, VPackSlice>> it = objectIterator();
			while (it.hasNext()) {
				Map.Entry<String, VPackSlice> f = it.next();
				sb.append("\n");
				for (int i = 0; i < level; i++) {
					sb.append(" |----");
				}
				sb.append(" ");
				sb.append(f.getKey());
				f.getValue().doGetSchemaDescription(sb, level);
			}
		} else if (type == ValueType.ARRAY) {
			// only print the schema of the first element of the array
			Iterator<VPackSlice> ai = arrayIterator();
			if (ai.hasNext()) {
				level++;
				VPackSlice firstValue = ai.next();
				firstValue.doGetSchemaDescription(sb, level);
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + start;

		int arrayHash = 1;
		for (int i = start, max = getByteSize(); i < max; i++)
			arrayHash = 31 * arrayHash + vpack[i];

		result = prime * result + arrayHash;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final VPackSlice other = (VPackSlice) obj;

		int byteSize = getByteSize();
		int otherByteSize = other.getByteSize();

		if(byteSize != otherByteSize) {
			return false;
		}

		for(int i = 0; i < byteSize; i++) {
			if(vpack[i+start] != other.vpack[i+other.start]) {
				return false;
			}
		}

		return true;
	}


}
