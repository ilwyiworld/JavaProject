package com.znv.fss.es.FssExactSearch;

import com.alibaba.fastjson.JSONObject;
import com.znv.fss.common.VConstants;
import com.znv.fss.es.EsConfig;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * Created by User on 2017/8/4.
 */
public class GetFeatureValue {

    protected static final Logger LOGGER = LogManager.getLogger(GetFeatureValue.class);
    public static final String URL = EsConfig.getProperty(VConstants.FACE_SERVER_IP) + ":"
            + EsConfig.getProperty(VConstants.FACE_SERVER_PORT) + "/verify/feature/gets";

    public static String httpExecute(File file) {
        String res = "";
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(URL);
            HttpEntity entity = MultipartEntityBuilder.create().addPart("imageData", new FileBody(file)).build();
            httpPost.setEntity(entity);
            CloseableHttpResponse response = client.execute(httpPost);
            try {
                HttpEntity entity1 = response.getEntity();
                int status = response.getStatusLine().getStatusCode();
                if (status == 200) {
                    res = EntityUtils.toString(entity1);
                } /*else {
                    res = EntityUtils.toString(entity1);
                }*/
            } catch (Exception e) {
                LOGGER.error("Exception: \n" + e);
            } finally {
                response.close();
                client.close();
            }
        } catch (Exception e) {
            LOGGER.error("Exception: \n" + e);
        }
        return res;
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
                LOGGER.error("get feature error !");
            }
        }
        Base64 base64 = new Base64();
        return base64.decode(strFeature1);
    }

    public static float[] getFloatArray(byte[] bytes) throws Exception {
        int offset = 12;
        if (0 != (bytes.length - offset) % 4) {
            LOGGER.error("feature dimension is incompeleted");
            throw new Exception("feature dimension is incompeleted");
        }
        int len = (bytes.length - offset) / 4;
        float feature[] = new float[len];
        for (int i = 0; i < len; i++) {
            feature[i] = Float.intBitsToFloat(getInt(bytes, offset));
            offset += 4;
        }
        return feature;
    }

    public static int getInt(byte[] bytes, int offset) {
        // return (0xff & bytes[offset]) | (0xff00 & (bytes[offset + 1] << 8)) | (0xff0000 & (bytes[offset + 2] << 16))
        // | (0xff000000 & (bytes[offset + 3] << 24));
        return (0xff & bytes[offset]) | ((0xff & bytes[offset + 1]) << 8) | ((0xff & bytes[offset + 2]) << 16)
                | ((0xff & bytes[offset + 3]) << 24);
    }

    // 传图片特征值
    public static String getFeatureValue(String featureValue1) {
        StringBuffer featureValue = new StringBuffer();
        String featureValueReturn = "";
        Base64 base64 = new Base64();
        byte[] feature = base64.decode(featureValue1);
        try {
            float[] floatFeature = getFloatArray(feature);
            featureValue.append("[");
            for (int i = 0; i < floatFeature.length - 1; i++) {
                featureValue.append(floatFeature[i]);
                featureValue.append(",");
            }
            featureValue.append(floatFeature[floatFeature.length - 1]);
            featureValue.append("]");
            featureValueReturn = featureValue.toString();
        } catch (Exception e) {
            LOGGER.error("Exception: \n" + e);
        }
        return featureValueReturn;
    }
    //传一个图片路径
    /*public static String getFeatureValuePath(String path) {
        StringBuffer featureValue = new StringBuffer();
        String featureValueReturn  = "";
        File file = new File(path);
        String feature1 = httpExecute(file);
        System.out.println("feature1:"+feature1);
        *//*String feature = JSON.parseObject(feature1).getString("feature");
        try {
            BASE64Decoder decode = new BASE64Decoder();
            byte[] image = decode.decodeBuffer(feature);
            System.out.println(image);
        }catch(Exception e){
            e.printStackTrace();
        }*//*
        byte[] feature = getFeature(feature1);
       // System.out.println("feature.size = " + feature.length);
        try {
            float[] floatFeature = getFloatArray(feature);
            featureValue.append("[");
            for (int i = 0; i < floatFeature.length - 1; i++) {
                featureValue.append(floatFeature[i]);
                featureValue.append(",");
            }
            featureValue.append(floatFeature[floatFeature.length - 1]);
            featureValue.append("]");
            featureValueReturn = featureValue.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return featureValueReturn;
    }*/

}
