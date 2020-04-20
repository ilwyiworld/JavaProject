package kafka.producer;

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

import java.io.IOException;
import java.util.UUID;

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


}
