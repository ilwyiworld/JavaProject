package com.znv.request;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
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
public class Post {

    /**
     * 查询
     */
    public static String sendPostData(String url, String data) {
        HttpPost post = new HttpPost(url);
        post.setHeader("DIY","YILIANG");    //设置请求头
        //只接受json格式的参数
        StringEntity se = new StringEntity(data, ContentType.create("application/json", "UTF-8"));
        post.setEntity(se);
        CloseableHttpClient http = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
            response = http.execute(post);
            return EntityUtils.toString(response.getEntity(), Charset.forName("utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

}
