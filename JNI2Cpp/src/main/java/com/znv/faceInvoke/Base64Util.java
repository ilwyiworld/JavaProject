package com.znv.faceInvoke;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.misc.BASE64Decoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2018/1/15.
 */
public class Base64Util {

    protected static final Logger logger = LogManager.getLogger(Base64Util.class);

    public static boolean generateImage(String imgStr, String path,String folder) {
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
            String folderPath="/data/lrj2904/ffmpeg-rstp/dll_test/test_so/"+folder;
            File folderFile=new File(folderPath);
            if(!folderFile.exists()){
                folderFile.mkdirs();
            }
            OutputStream out=new FileOutputStream(folderPath+"/"+path+".jpg");
            out.write(b);
            out.flush();
            out.close();
            return true;
        }catch(Exception e){
            logger.error("图片解码失败",e);
            return false;
        }
    }

}
