package com.yiworld.weather.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;

/**
 * Created by Administrator on 2018/3/30.
 */
public class HttpUtils {

    @Autowired
    private CloseableHttpClient client;

    private final static Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    public static String getGetResponse(String uri){
        String json="";
        // 设置参数
        try {
            HttpGet httpGet=new HttpGet();
            httpGet.setURI(new URI(uri));
            CloseableHttpClient client = HttpClients.createDefault();
            // 发送请求
            HttpResponse httpResponse = client.execute(httpGet);
            // 获取返回的数据
            HttpEntity entity = httpResponse.getEntity();
            byte[] body = EntityUtils.toByteArray(entity);
            StatusLine sL = httpResponse.getStatusLine();
            int statusCode = sL.getStatusCode();
            if (statusCode == 200) {
                json = new String(body, "UTF-8");
                entity.consumeContent();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return json;
    }

}
