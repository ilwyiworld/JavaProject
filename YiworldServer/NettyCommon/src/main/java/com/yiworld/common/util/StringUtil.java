package com.yiworld.common.util;

public class StringUtil {
    private StringUtil() {
    }

    public static boolean isNullOrEmpty(String str) {
        return null == str || 0 == str.trim().length();
    }

    public static boolean isEmpty(String str) {
        return str == null || "".equals(str.trim());
    }

    public static boolean isNotEmpty(String str) {
        return str != null && !"".equals(str.trim());
    }

    public static String formatLike(String str) {
        return isNotEmpty(str)?"%" + str + "%":null;
    }
}