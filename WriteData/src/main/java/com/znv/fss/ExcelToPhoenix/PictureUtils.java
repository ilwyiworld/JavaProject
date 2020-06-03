package com.znv.fss.ExcelToPhoenix;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.stream.FileImageInputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONObject;
import com.znv.fss.ExcelToPhoenix.conf.ConfigManager;
import com.znv.fss.ExcelToPhoenix.constant.Constant;

/**
 * Created by Administrator on 2017/6/12.
 */
public class PictureUtils {
    public static final String url = ConfigManager.getString(Constant.IP) + ":" + ConfigManager.getString(Constant.PORT)
        + "/verify/feature/gets";

    public static String httpExecute(File file) {
        String res = "";
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            //HttpPost httpPost = new HttpPost("http://"+ip+":80/verify/feature/gets");
            HttpEntity entity = MultipartEntityBuilder.create().addPart("imageData", new FileBody(file)).build();
            httpPost.setEntity(entity);
            CloseableHttpResponse response = client.execute(httpPost);
            try {
                HttpEntity entity1 = response.getEntity();
                int status = response.getStatusLine().getStatusCode();
                if (status == 200) {
                    res = EntityUtils.toString(entity1);
                    // System.out.println("status " + status + " response " + response);
                } else {
                    // error code
                    res = EntityUtils.toString(entity1);
                    // System.out.println("status " + status + " response " + response);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                response.close();
                client.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static String getStrFeature(String feature1) {
        JSONObject resData1 = JSONObject.parseObject(feature1);
        JSONObject result1 = new JSONObject();
        String strFeature = "";
        if (!StringUtils.isEmpty(feature1)) {
            if ("success".equals(resData1.getString("result"))) {
                result1.put("feature", resData1.getString("feature"));
                strFeature = result1.get("feature").toString();
            } else if ("error".equals(resData1.getString("result"))) {
                // todo 异常处理
                System.out.println("get feature error !");
            }
        }
        return strFeature;
    }

    public static byte[] getFeature(String feature1) {
        JSONObject resData1 = JSONObject.parseObject(feature1);
        JSONObject result1 = new JSONObject();
        String strFeature1 = "";
        if (!StringUtils.isEmpty(feature1)) {
            if ("success".equals(resData1.getString("result"))) {
                result1.put("feature", resData1.getString("feature"));
                strFeature1 = result1.get("feature").toString();
            } else if ("error".equals(resData1.getString("result"))) {
                // todo 异常处理
                System.out.println("get feature error !");
            }
        }
        Base64 base64 = new Base64();
        return base64.decode(strFeature1);
    }

    public static String savePicture(byte[] image, String filename, String filePath) {
        String fileName = filePath + File.separator + filename + ".jpg";
//        String fileName = filePath + "/" + filename + ".jpg";
        try {
            OutputStream outStr = new FileOutputStream(fileName);
            InputStream inStr = new ByteArrayInputStream(image);

            byte[] b = new byte[1024];
            int nRead = 0;
            while ((nRead = inStr.read(b)) != -1) {
                outStr.write(b, 0, nRead);
            }
            outStr.flush();
            outStr.close();
            inStr.close();

        } catch (FileNotFoundException e) {
            System.out.println("FileNotFoundException !");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IOException");
            e.printStackTrace();
        }
        return fileName;
    }

    public static byte[] image2byte(String path) {
        byte[] data = null;
        FileImageInputStream input = null;
        try {
            input = new FileImageInputStream(new File(path));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int numBytesRead = 0;
            while ((numBytesRead = input.read(buf)) != -1) {
                output.write(buf, 0, numBytesRead);
            }
            data = output.toByteArray();
            output.close();
            input.close();
        } catch (FileNotFoundException ex1) {
            ex1.printStackTrace();
        } catch (IOException ex1) {
            ex1.printStackTrace();
        }
        return data;
    }

    public static float[] getFloatArray(byte[] bytes) throws Exception {
        int offset = 12;
        if (0 != (bytes.length - offset) % 4) {
            throw new Exception("feature dimension is incompeleted");
        }
        int len = (bytes.length - offset) / 4;
        float feature[] = new float[len];
        for (int i = 0; i < len; i++) {
            feature[i] = Float.intBitsToFloat(GetInt(bytes, offset));
            offset += 4;
        }
        return feature;
    }

    public static int GetInt(byte[] bytes, int offset) {
        // return (0xff & bytes[offset]) | (0xff00 & (bytes[offset + 1] << 8)) | (0xff0000 & (bytes[offset + 2] << 16))
        // | (0xff000000 & (bytes[offset + 3] << 24));
        return (0xff & bytes[offset]) | ((0xff & bytes[offset + 1]) << 8) | ((0xff & bytes[offset + 2]) << 16)
                | ((0xff & bytes[offset + 3]) << 24);
    }

    public static void main(String[] args) {
        // 输出float型feature
        File file = new File("D:\\project\\不超过64KB的照片\\1\\znv2.jpg");
        //String ip = "10.45.157.114";
        String feature1 = httpExecute(file);
        byte[] feature = getFeature(feature1);
        System.out.println("feature.size = " + feature.length);
        try {
            float[] floatFeature = getFloatArray(feature);
            System.out.print("floatFeature = [");
            for (int i = 0; i < floatFeature.length - 1; i++) {
                System.out.print(floatFeature[i] + ",");
            }
            System.out.print(floatFeature[floatFeature.length - 1]);
            System.out.println("]");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
