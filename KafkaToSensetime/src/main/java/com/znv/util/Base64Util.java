package com.znv.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.misc.BASE64Decoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class Base64Util {

    private static final Logger LOGGER = LogManager.getLogger(Base64Util.class);

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

    public static boolean generateImage(String folder,String imgStr, String name) {
        if(imgStr==null)
            return false;
        BASE64Decoder decoder=new BASE64Decoder();
        try{
            //解码
            byte[] b=decoder.decodeBuffer(imgStr);
            for (int i = 0; i < b.length; i++) {
                if(b[i]<0){
                    b[i]+=256;
                }
            }
            String folderPath="/var/log/KafkaPics/"+folder+"/";
            File folderFile=new File(folderPath);
            if(!folderFile.exists()){
                folderFile.mkdirs();
            }
            OutputStream out=new FileOutputStream(folderPath+"/"+name+".jpg");
            out.write(b);
            out.flush();
            out.close();
            return true;
        }catch(Exception e){
            LOGGER.error("图片解码失败",e);
            return false;
        }
    }

}
