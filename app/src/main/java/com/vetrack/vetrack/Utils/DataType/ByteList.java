package com.vetrack.vetrack.Utils.DataType;

import java.io.Serializable;

/**
 * Vetrack
 * Create on 2019/5/28.
 */
public class ByteList implements Serializable {
    private byte[] array_b;

    public ByteList(byte[] array) {
        array_b = array;
    }

    public byte get(int index) {
        return array_b[index];
    }
}
