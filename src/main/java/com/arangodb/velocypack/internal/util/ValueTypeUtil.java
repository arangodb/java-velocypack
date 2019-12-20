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

import com.arangodb.velocypack.ValueType;

/**
 * @author Mark Vollmary
 *
 */
public class ValueTypeUtil {

	private static final ValueType[] MAP = {
		ValueType.NONE, // 0x00
		ValueType.ARRAY, // 0x01
		ValueType.ARRAY, // 0x02
		ValueType.ARRAY, // 0x03
		ValueType.ARRAY, // 0x04
		ValueType.ARRAY, // 0x05
		ValueType.ARRAY, // 0x06
		ValueType.ARRAY, // 0x07
		ValueType.ARRAY, // 0x08
		ValueType.ARRAY, // 0x09
		ValueType.OBJECT, // 0x0a
		ValueType.OBJECT, // 0x0b
		ValueType.OBJECT, // 0x0c
		ValueType.OBJECT, // 0x0d
		ValueType.OBJECT, // 0x0e
		ValueType.OBJECT, // 0x0f
		ValueType.OBJECT, // 0x10
		ValueType.OBJECT, // 0x11
		ValueType.OBJECT, // 0x12
		ValueType.ARRAY, // 0x13
		ValueType.OBJECT, // 0x14
		ValueType.NONE, // 0x15
		ValueType.NONE, // 0x16
		ValueType.ILLEGAL, // 0x17
		ValueType.NULL, // 0x18
		ValueType.BOOL, // 0x19
		ValueType.BOOL, // 0x1a
		ValueType.DOUBLE, // 0x1b
		ValueType.UTC_DATE, // 0x1c
		ValueType.EXTERNAL, // 0x1d
		ValueType.MIN_KEY, // 0x1e
		ValueType.MAX_KEY, // 0x1f
		ValueType.INT, // 0x20
		ValueType.INT, // 0x21
		ValueType.INT, // 0x22
		ValueType.INT, // 0x23
		ValueType.INT, // 0x24
		ValueType.INT, // 0x25
		ValueType.INT, // 0x26
		ValueType.INT, // 0x27
		ValueType.UINT, // 0x28
		ValueType.UINT, // 0x29
		ValueType.UINT, // 0x2a
		ValueType.UINT, // 0x2b
		ValueType.UINT, // 0x2c
		ValueType.UINT, // 0x2d
		ValueType.UINT, // 0x2e
		ValueType.UINT, // 0x2f
		ValueType.SMALLINT, // 0x30
		ValueType.SMALLINT, // 0x31
		ValueType.SMALLINT, // 0x32
		ValueType.SMALLINT, // 0x33
		ValueType.SMALLINT, // 0x34
		ValueType.SMALLINT, // 0x35
		ValueType.SMALLINT, // 0x36
		ValueType.SMALLINT, // 0x37
		ValueType.SMALLINT, // 0x38
		ValueType.SMALLINT, // 0x39
		ValueType.SMALLINT, // 0x3a
		ValueType.SMALLINT, // 0x3b
		ValueType.SMALLINT, // 0x3c
		ValueType.SMALLINT, // 0x3d
		ValueType.SMALLINT, // 0x3e
		ValueType.SMALLINT, // 0x3f
		ValueType.STRING, // 0x40
		ValueType.STRING, // 0x41
		ValueType.STRING, // 0x42
		ValueType.STRING, // 0x43
		ValueType.STRING, // 0x44
		ValueType.STRING, // 0x45
		ValueType.STRING, // 0x46
		ValueType.STRING, // 0x47
		ValueType.STRING, // 0x48
		ValueType.STRING, // 0x49
		ValueType.STRING, // 0x4a
		ValueType.STRING, // 0x4b
		ValueType.STRING, // 0x4c
		ValueType.STRING, // 0x4d
		ValueType.STRING, // 0x4e
		ValueType.STRING, // 0x4f
		ValueType.STRING, // 0x50
		ValueType.STRING, // 0x51
		ValueType.STRING, // 0x52
		ValueType.STRING, // 0x53
		ValueType.STRING, // 0x54
		ValueType.STRING, // 0x55
		ValueType.STRING, // 0x56
		ValueType.STRING, // 0x57
		ValueType.STRING, // 0x58
		ValueType.STRING, // 0x59
		ValueType.STRING, // 0x5a
		ValueType.STRING, // 0x5b
		ValueType.STRING, // 0x5c
		ValueType.STRING, // 0x5d
		ValueType.STRING, // 0x5e
		ValueType.STRING, // 0x5f
		ValueType.STRING, // 0x60
		ValueType.STRING, // 0x61
		ValueType.STRING, // 0x62
		ValueType.STRING, // 0x63
		ValueType.STRING, // 0x64
		ValueType.STRING, // 0x65
		ValueType.STRING, // 0x66
		ValueType.STRING, // 0x67
		ValueType.STRING, // 0x68
		ValueType.STRING, // 0x69
		ValueType.STRING, // 0x6a
		ValueType.STRING, // 0x6b
		ValueType.STRING, // 0x6c
		ValueType.STRING, // 0x6d
		ValueType.STRING, // 0x6e
		ValueType.STRING, // 0x6f
		ValueType.STRING, // 0x70
		ValueType.STRING, // 0x71
		ValueType.STRING, // 0x72
		ValueType.STRING, // 0x73
		ValueType.STRING, // 0x74
		ValueType.STRING, // 0x75
		ValueType.STRING, // 0x76
		ValueType.STRING, // 0x77
		ValueType.STRING, // 0x78
		ValueType.STRING, // 0x79
		ValueType.STRING, // 0x7a
		ValueType.STRING, // 0x7b
		ValueType.STRING, // 0x7c
		ValueType.STRING, // 0x7d
		ValueType.STRING, // 0x7e
		ValueType.STRING, // 0x7f
		ValueType.STRING, // 0x80
		ValueType.STRING, // 0x81
		ValueType.STRING, // 0x82
		ValueType.STRING, // 0x83
		ValueType.STRING, // 0x84
		ValueType.STRING, // 0x85
		ValueType.STRING, // 0x86
		ValueType.STRING, // 0x87
		ValueType.STRING, // 0x88
		ValueType.STRING, // 0x89
		ValueType.STRING, // 0x8a
		ValueType.STRING, // 0x8b
		ValueType.STRING, // 0x8c
		ValueType.STRING, // 0x8d
		ValueType.STRING, // 0x8e
		ValueType.STRING, // 0x8f
		ValueType.STRING, // 0x90
		ValueType.STRING, // 0x91
		ValueType.STRING, // 0x92
		ValueType.STRING, // 0x93
		ValueType.STRING, // 0x94
		ValueType.STRING, // 0x95
		ValueType.STRING, // 0x96
		ValueType.STRING, // 0x97
		ValueType.STRING, // 0x98
		ValueType.STRING, // 0x99
		ValueType.STRING, // 0x9a
		ValueType.STRING, // 0x9b
		ValueType.STRING, // 0x9c
		ValueType.STRING, // 0x9d
		ValueType.STRING, // 0x9e
		ValueType.STRING, // 0x9f
		ValueType.STRING, // 0xa0
		ValueType.STRING, // 0xa1
		ValueType.STRING, // 0xa2
		ValueType.STRING, // 0xa3
		ValueType.STRING, // 0xa4
		ValueType.STRING, // 0xa5
		ValueType.STRING, // 0xa6
		ValueType.STRING, // 0xa7
		ValueType.STRING, // 0xa8
		ValueType.STRING, // 0xa9
		ValueType.STRING, // 0xaa
		ValueType.STRING, // 0xab
		ValueType.STRING, // 0xac
		ValueType.STRING, // 0xad
		ValueType.STRING, // 0xae
		ValueType.STRING, // 0xaf
		ValueType.STRING, // 0xb0
		ValueType.STRING, // 0xb1
		ValueType.STRING, // 0xb2
		ValueType.STRING, // 0xb3
		ValueType.STRING, // 0xb4
		ValueType.STRING, // 0xb5
		ValueType.STRING, // 0xb6
		ValueType.STRING, // 0xb7
		ValueType.STRING, // 0xb8
		ValueType.STRING, // 0xb9
		ValueType.STRING, // 0xba
		ValueType.STRING, // 0xbb
		ValueType.STRING, // 0xbc
		ValueType.STRING, // 0xbd
		ValueType.STRING, // 0xbe
		ValueType.STRING, // 0xbf
		ValueType.BINARY, // 0xc0
		ValueType.BINARY, // 0xc1
		ValueType.BINARY, // 0xc2
		ValueType.BINARY, // 0xc3
		ValueType.BINARY, // 0xc4
		ValueType.BINARY, // 0xc5
		ValueType.BINARY, // 0xc6
		ValueType.BINARY, // 0xc7
		ValueType.BCD, // 0xc8
		ValueType.BCD, // 0xc9
		ValueType.BCD, // 0xca
		ValueType.BCD, // 0xcb
		ValueType.BCD, // 0xcc
		ValueType.BCD, // 0xcd
		ValueType.BCD, // 0xce
		ValueType.BCD, // 0xcf
		ValueType.BCD, // 0xd0
		ValueType.BCD, // 0xd1
		ValueType.BCD, // 0xd2
		ValueType.BCD, // 0xd3
		ValueType.BCD, // 0xd4
		ValueType.BCD, // 0xd5
		ValueType.BCD, // 0xd6
		ValueType.BCD, // 0xd7
		ValueType.NONE, // 0xd8
		ValueType.NONE, // 0xd9
		ValueType.NONE, // 0xda
		ValueType.NONE, // 0xdb
		ValueType.NONE, // 0xdc
		ValueType.NONE, // 0xdd
		ValueType.NONE, // 0xde
		ValueType.NONE, // 0xdf
		ValueType.NONE, // 0xe0
		ValueType.NONE, // 0xe1
		ValueType.NONE, // 0xe2
		ValueType.NONE, // 0xe3
		ValueType.NONE, // 0xe4
		ValueType.NONE, // 0xe5
		ValueType.NONE, // 0xe6
		ValueType.NONE, // 0xe7
		ValueType.NONE, // 0xe8
		ValueType.NONE, // 0xe9
		ValueType.NONE, // 0xea
		ValueType.NONE, // 0xeb
		ValueType.NONE, // 0xec
		ValueType.NONE, // 0xed
		ValueType.TAGGED, // 0xee
		ValueType.TAGGED, // 0xef
		ValueType.CUSTOM, // 0xf0
		ValueType.CUSTOM, // 0xf1
		ValueType.CUSTOM, // 0xf2
		ValueType.CUSTOM, // 0xf3
		ValueType.CUSTOM, // 0xf4
		ValueType.CUSTOM, // 0xf5
		ValueType.CUSTOM, // 0xf6
		ValueType.CUSTOM, // 0xf7
		ValueType.CUSTOM, // 0xf8
		ValueType.CUSTOM, // 0xf9
		ValueType.CUSTOM, // 0xfa
		ValueType.CUSTOM, // 0xfb
		ValueType.CUSTOM, // 0xfc
		ValueType.CUSTOM, // 0xfd
		ValueType.CUSTOM, // 0xfe
		ValueType.CUSTOM, // 0xff
	};

	private ValueTypeUtil() {
		super();
	}

	public static ValueType get(final byte key) {
		return MAP[(int) key & 0xff];
	}

}
