package com.znv.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

public class Base64Util {

    public static byte[] decode(String imageString) {
        if (StringUtils.isEmpty(imageString)) {
            return null;
        }
        return Base64.decodeBase64(imageString);
    }
}
