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
        for (int i = 0; i < length || i < o.length; i++) {
            int c = vpack[start + i] - o.vpack[o.start + i];
            if (c != 0) return c;
        }
        return length - o.length;
    }

    @Override
    public String toString() {
        return new String(vpack, start, length, StandardCharsets.UTF_8);
    }
}
