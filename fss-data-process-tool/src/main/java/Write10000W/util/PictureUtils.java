package Write10000W.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.imageio.stream.FileImageInputStream;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/6/12.
 */
public class PictureUtils {
    public static final String url = "http://10.45.157.108:80/verify/feature/gets"; //http://10.45.157.114:80/verify/feature/gets";
    // 银川动态服务器：10.110.10.241，
    // 南京：10.45.150.39  //10.45.152.113

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
                    // System.out.println("status " + status + " res " + res);
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

    /**
     * 根据图片获取特征
     *
     * @param fileName
     * @param data
     * @param url
     * @return
     */
    public static String getFeatureByImage(String fileName, byte[] data, String url) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody("imageData", data, ContentType.DEFAULT_BINARY, fileName).build();

        httpPost.setEntity(entity);

        CloseableHttpResponse response = null;
        HttpEntity resEntity = null;
        String result = httpCommon(httpPost, response, resEntity, client);
        return result;
    }


    /**
     * 商汤批量获取特征
     *
     * @param picMap
     * @param faceUrl
     * @return
     */
    public static String getFeatureBatch(HashMap<String, Result> picMap, String faceUrl) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(faceUrl);
        MultipartEntityBuilder entity = MultipartEntityBuilder.create();
        for (Map.Entry<String, Result> entry : picMap.entrySet()) {
            byte[] pic = entry.getValue().getValue(Bytes.toBytes("PICS"), Bytes.toBytes("PERSON_IMG"));
            if (pic != null && pic.length > 0) {
                entity.addBinaryBody("imageDatas", pic, ContentType.DEFAULT_BINARY, entry.getKey());
            } else {
                System.out.println(entry.getValue().getValue(Bytes.toBytes("ATTR"), Bytes.toBytes("PERSON_NAME")) + "获取图片失败！");
            }
        }
        httpPost.setEntity(entity.build());

        CloseableHttpResponse response = null;
        HttpEntity resEntity = null;
        String result = httpCommon(httpPost, response, resEntity, client);
        return result;
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

    public static byte[] getFeatures(String feature1) {
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

    public static String getFeature(String fileName, byte[] data, String url) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        HttpEntity entity = MultipartEntityBuilder.create().addBinaryBody("imageData", data, ContentType.DEFAULT_BINARY, fileName).build();
        httpPost.setEntity(entity);
        Object response = null;
        Object resEntity = null;
        String result = httpCommon(httpPost, (CloseableHttpResponse) response, (HttpEntity) resEntity, client);
        return result;
    }

    public static String httpCommon(HttpPost httpPost, CloseableHttpResponse response, HttpEntity resEntity, CloseableHttpClient httpClient) {
        String result = null;

        try {
            response = httpClient.execute(httpPost);
            resEntity = response.getEntity();
            result = EntityUtils.toString(resEntity, "UTF-8");
        } catch (ClientProtocolException var26) {
            var26.printStackTrace();
        } catch (IOException var27) {
            var27.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException var25) {
                    var25.printStackTrace();
                }
            }

            if (resEntity != null) {
                try {
                    EntityUtils.consume(resEntity);
                } catch (IOException var24) {
                    var24.printStackTrace();
                }
            }

            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException var23) {
                    var23.printStackTrace();
                }
            }

        }

        return result;
    }

    public static String savePicture(byte[] image, String filename, String filePath) {
        String fileName = filePath + "\\" + filename; //+ ".jpg";

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

    public static void main(String[] args) {
        // 输出float型feature
        File file = new File("D:\\项目\\不超过64KB的照片\\222picture\\person0.jpg");
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
