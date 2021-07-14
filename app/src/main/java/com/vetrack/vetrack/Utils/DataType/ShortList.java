package com.vetrack.vetrack.Utils.DataType;

import java.io.Serializable;

/**
 * Vetrack
 * Create on 2019/5/28.
 */
public class ShortList implements Serializable {
    private short[] array_s;

    public ShortList(short[] array) {
        array_s = array;
    }

    public short get(int index) {
        return array_s[index];
    }
}
