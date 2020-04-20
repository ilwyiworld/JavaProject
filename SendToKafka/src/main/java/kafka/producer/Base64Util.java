package kafka.producer;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

public class Base64Util {

    public static String encode(byte[] imageByte) {
        if (imageByte == null) {
            return "";
        }
        return Base64.encodeBase64String(imageByte);
    }

    public static byte[] decode(String imageString) {
        if (StringUtils.isEmpty(imageString)) {
            return null;
        }
        return Base64.decodeBase64(imageString);
    }

}
