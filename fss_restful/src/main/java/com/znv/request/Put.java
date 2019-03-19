package com.znv.request;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by Administrator on 2017/7/12.
 */
public class Put {

    /**
     * 修改和添加
     */
    public static String sendPutData(String url, String data) {
        HttpPut put = new HttpPut(url);
        //只接受json格式的参数
        StringEntity se = new StringEntity(data, ContentType.create("application/json", "UTF-8"));
        put.setEntity(se);
        CloseableHttpClient http = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
            response = http.execute(put);
            return EntityUtils.toString(response.getEntity(), Charset.forName("utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
