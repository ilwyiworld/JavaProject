package com.znv.faceInvoke;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import org.opencv.core.CvType;
//import org.opencv.core.Mat;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.File;
//import org.opencv.*;

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
            String folderPath="/data/lrj2904/ffmpeg-rstp/dll_test/"+folder;
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

    /*private static BufferedImage toBufferedImage(Mat matrix) {

        /*Mat matImage=new Mat(240,320, CvType.CV_8UC1);
            matImage.put(0,0,imgStr);
            BufferedImage image=toBufferedImage(matImage);
            ImageIO.write(image, "jpg", new File(folderPath+"/"+path+".jpg"));*/

        /*
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (matrix.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = matrix.channels() * matrix.cols() * matrix.rows();
        byte[] buffer = new byte[bufferSize];
        matrix.get(0, 0, buffer); // 获取所有的像素点
        BufferedImage image = new BufferedImage(matrix.cols(), matrix.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }*/
}
