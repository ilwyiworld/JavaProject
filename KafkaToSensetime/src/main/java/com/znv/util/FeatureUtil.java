package com.znv.util;

import java.io.IOException;
import java.util.UUID;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.codec.binary.Base64;

public class FeatureUtil {

    private static final Logger LOGGER = LogManager.getLogger(FeatureUtil.class);

    public static String getImageFeature(String ip,String port,String imageData) {
        String result = null;
        HttpPost httpPost = new HttpPost(String.format("http://%s:%s/verify/feature/gets", ip, port));
        CloseableHttpClient client = HttpClients.createDefault();
        //CloseableHttpClient client = HttpClientPool.getInstance().getHttpClient();
        //httpPost.setHeader("Connection", "keep-alive");
        ByteArrayBody bab = new ByteArrayBody(Base64Util.decode(imageData), UUID.randomUUID().toString());
        HttpEntity entity = MultipartEntityBuilder.create().addPart("imageData", bab).build();
        httpPost.setEntity(entity);
        CloseableHttpResponse response = null;
        HttpEntity resEntity = null;
        try {
            response = client.execute(httpPost);
            resEntity = response.getEntity();
            JSONObject obj= JSON.parseObject(EntityUtils.toString(resEntity));
            if(obj.getString("result").equals("success")){
                result=obj.getString("feature");
            }else{
                result="";
            }
        } catch (ClientProtocolException e) {
            LOGGER.error(e.getLocalizedMessage());
        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage());
        } finally {
            if (null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    LOGGER.error(e.getLocalizedMessage());
                }
            }
            if (null != resEntity) {
                try {
                    EntityUtils.consume(resEntity);
                } catch (IOException e) {
                    LOGGER.error(e.getLocalizedMessage());
                }
            }
        }
        return result;
    }

    public static String getImageDetectAndQuality(String ip,String port,String imageData) {
        String result = null;
        HttpPost httpPost = new HttpPost(String.format("http://%s:%s/verify/face/detectAndQuality", ip, port));
        CloseableHttpClient client = HttpClients.createDefault();
        ByteArrayBody bab = new ByteArrayBody(Base64Util.decode(imageData), UUID.randomUUID().toString());
        HttpEntity entity = MultipartEntityBuilder.create().addPart("imageData", bab).build();
        httpPost.setEntity(entity);
        CloseableHttpResponse response = null;
        HttpEntity resEntity = null;
        try {
            response = client.execute(httpPost);
            resEntity = response.getEntity();
            result = EntityUtils.toString(resEntity);
        } catch (ClientProtocolException e) {
            LOGGER.error(e.getLocalizedMessage());
        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage());
        } finally {
            if (null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    LOGGER.error(e.getLocalizedMessage());
                }
            }
            if (null != resEntity) {
                try {
                    EntityUtils.consume(resEntity);
                } catch (IOException e) {
                    LOGGER.error(e.getLocalizedMessage());
                }
            }
        }
        return result;
    }

    //人脸比较
    public static JSONObject computeSimByFeature(String feature1,String feature2) {
        float data = 0.0f;
        JSONObject result = new JSONObject();
        try {
            data = FeatureUtil.Comp(feature1, feature2);
            result.put("errorCode", 100000);
            result.put("data", data);
        } catch (Exception e) {
            result.put("errorCode", 150001); //feature1长度不等于feature2
        }
        return result;

    }
    public static float Comp(String f1,String f2) throws Exception {
        if (f1.length()!=f2.length()){
            LOGGER.error("比对的队列图片特征值Length:"+f1.length());
            LOGGER.error("当前图片特征值Length:"+f2.length());
            throw new Exception("feature size unequal");
        }

        byte[] bin1 = Base64.decodeBase64(f1);
        byte[] bin2 = Base64.decodeBase64(f2);
        return Nomalize(Dot(bin1,bin2,12));
    }

    private static float Nomalize(float score) {
        if (score <= src_points[0]) {
            return dst_points[0];
        } else if (score >= src_points[src_points.length - 1]) {
            return dst_points[dst_points.length - 1];
        }

        float result = 0.0f;
        for (int i = 1; i < src_points.length; i++) {
            if (score < src_points[i]) {
                result = dst_points[i - 1] + (score - src_points[i - 1]) * (dst_points[i] - dst_points[i - 1])
                        / (src_points[i] - src_points[i - 1]);
                break;
            }
        }
        return result;
    }

    private static float Dot(byte[] f1, byte[] f2, int offset) throws Exception {
        // TODO Auto-generated method stub
        if (f1.length != f2.length) {
            LOGGER.error("Dot特征值Length:"+f2.length);
            throw new Exception("feature length unmatch");
        }
        if (0 != (f1.length - offset) % 4) {
            throw new Exception("feature dimension is incompeleted");
        }

        if (f1.length < offset) {
            throw new Exception("feature length is too short");
        }
        int dimCnt = (f1.length - offset) / 4;
        if (0 > dimCnt) {
            throw new Exception("");
        }
        float dist = 0.0f;
        for (int i = 0; i < dimCnt; i++) {
            dist += Float.intBitsToFloat(GetInt(f1, offset)) * Float.intBitsToFloat(GetInt(f2, offset));
            offset += 4;
        }
        return dist;
    }

    private static int GetInt(byte[] bytes, int offset) {
        return (0xff & bytes[offset]) | ((0xff & bytes[offset + 1]) << 8) | ((0xff & bytes[offset + 2]) << 16)
                | ((0xff & bytes[offset + 3]) << 24);
    }

    private static float[] src_points = { 0.0f, 0.128612995148f, 0.236073002219f, 0.316282004118f, 0.382878988981f,
            0.441266000271f, 0.490464001894f, 1.0f };

    private static float[] dst_points = { 0.0f, 0.40000000596f, 0.5f, 0.600000023842f, 0.699999988079f, 0.800000011921f,
            0.899999976158f, 1.0f };
}
