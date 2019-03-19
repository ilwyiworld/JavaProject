package org.elasticsearch.util;

import java.util.BitSet;

/**
 * Created by Administrator on 2017/10/26.
 */
public class TypeConvertUtil {
    public static long intarrayToLong(int[] array) {
        BitSet bs = new BitSet(Long.SIZE);
        assert array.length == Long.SIZE;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == 1)
                bs.set(i);
        }
        long[] bs_l = bs.toLongArray();
        if (bs_l.length < 1) {
            return 0;
        } else {
            return bs_l[0];
        }
    }

    public static short intarrayToShort(int[] array) {
        BitSet bs = new BitSet(Short.SIZE);
        assert array.length == Short.SIZE;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == 1)
                bs.set(i);
        }
        long[] bs_l = bs.toLongArray();
        if (bs_l.length < 1) {
            return 0;
        } else {
            short result = (short) bs_l[0];
            return result;
        }

    }

    public static String intarrayToString(int[] array) {
        StringBuilder result = new StringBuilder(array.length);
        for (int i = 0; i < array.length; i++) {
            result.append(array[i]);
        }
        return result.toString();
    }

    public static int longOf1(long n) {
        int count = 0;
        while (n != 0) {
            count++;
            n = n & (n - 1);
        }
        return count;
    }

    public static int intOf1(int n) {
        int count = 0;
        while (n != 0) {
            count++;
            n = n & (n - 1);
        }
        return count;
    }

}
