
package com.znv.fss.hbase.test;
import com.alibaba.fastjson.JSONObject;
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
import javax.imageio.stream.FileImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2017/6/12.
 */
public class PictureUtils {
    public static final String url = "http://10.45.157.114:80/verify/feature/gets";
    // 银川动态服务器：10.110.10.241，
    // 南京：10.45.150.39
    // 原地址：10.45.152.113
    // 新地址：10.45.157.108

    public static String httpExecute(File file) {
        String res = "";
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            HttpEntity entity = MultipartEntityBuilder.create().addPart("imageData", new FileBody(file)).build();
            httpPost.setEntity(entity);
            CloseableHttpResponse response = client.execute(httpPost);
            try {
                HttpEntity entity1 = response.getEntity();
                int status = response.getStatusLine().getStatusCode();
                if (status == 200) {
                    res = EntityUtils.toString(entity1);
                     System.out.println("status " + status + " res " + res);
                } else {
                    // error code
                    res = EntityUtils.toString(entity1);
                    // System.out.println("status " + status + " res " + res);
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

    public static ByteArrayOutputStream image2byte(String path,ByteArrayOutputStream bos){
        FileImageInputStream input = null;
        try {
            input = new FileImageInputStream(new File(path));
            byte[] buf = new byte[1024];
            int numBytesRead = 0;
            while ((numBytesRead = input.read(buf)) != -1) {
                bos.write(buf, 0, numBytesRead);
            }
            input.close();
        } catch (FileNotFoundException ex1) {
            ex1.printStackTrace();
        } catch (IOException ex1) {
            ex1.printStackTrace();
        }
        return bos;
    }

    public static void image2byte(InputStream input,ByteArrayOutputStream bos){
        try {
            byte[] buf = new byte[1024];
            int numBytesRead = 0;
            while ((numBytesRead = input.read(buf)) != -1) {
                bos.write(buf, 0, numBytesRead);
            }
            input.close();
        } catch (IOException ex1) {
            ex1.printStackTrace();
        }
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

    public static void savePicture(byte[] image, int personId, String filePath) {
        try {
            String fileName = filePath + "\\" + Integer.toString(personId) + ".jpg";
            OutputStream o = new FileOutputStream(fileName);
            InputStream in = new ByteArrayInputStream(image);
            byte[] b = new byte[1024];
            int nRead = 0;
            while ((nRead = in.read(b)) != -1) {
                o.write(b, 0, nRead);
            }
            o.flush();
            o.close();
            in.close();
        } catch (FileNotFoundException e) {
            System.out.println("FileNotFoundException !");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IOException");
            e.printStackTrace();
        }
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
        File file = new File("E:\\FssProgram\\V1.0\\全表扫描\\相似度阈值测试\\38.jpg");
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
