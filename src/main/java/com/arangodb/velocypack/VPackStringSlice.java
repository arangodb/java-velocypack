package com.arangodb.velocypack;

import java.nio.charset.StandardCharsets;

/**
 * Wrapper around a {@link ValueType.STRING} supporting fast bytewise comparison.
 *
 * @see https://github.com/arangodb/velocypack/blob/master/VelocyPack.md#objects
 */
public class VPackStringSlice implements Comparable<VPackStringSlice> {
    private byte[] vpack;
    /**
     * Index of the string bytes within {@link vpack},
     * i.e. tag byte and length are somewhere before this index.
     */
    private int start;
    private int length;

    public VPackStringSlice(byte[] vpack, int start, int length) {
        this.vpack = vpack;
        this.start = start;
        this.length = length;
    }

    @Override
    public int compareTo(VPackStringSlice o) {
        return compareToBytes(o.vpack, o.start, o.length);
    }

    public int compareToBytes(byte[] other) {
        return compareToBytes(other, 0, other.length);
    }

    public int compareToBytes(byte[] other, int off, int oLen) {
        for (int i = 0; i < length && i < oLen; i++) {
            int c = (vpack[start + i] & 0xff) - (other[off + i] & 0xff);
            if (c != 0) return c;
        }
        return length - oLen;
    }

    @Override
    public String toString() {
        return new String(vpack, start, length, StandardCharsets.UTF_8);
    }
}
