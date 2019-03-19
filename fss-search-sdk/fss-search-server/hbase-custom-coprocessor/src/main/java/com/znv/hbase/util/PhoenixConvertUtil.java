package com.znv.hbase.util;

import org.apache.hadoop.hbase.util.Bytes;

/**
 * Created by Administrator on 2017/8/7.
 */
public class PhoenixConvertUtil {
    public static byte[] convertDescField(byte[] array) {
        byte[] result = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = (byte) (~array[i]);
        }
        return result;
    }

    public static float convertFloatField(byte[] array) {
        return Float.intBitsToFloat((Bytes.toInt(array) ^ 0x80000001));
    }
}
