package com.znv.client;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by Administrator on 2017/7/18.
 */
public class TestFeature {
    public static void main(String[] args) throws IOException {
        String result = null;
        String url="http://10.45.146.78:8787/verify/features/gets";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        File file=new File("C:\\Users\\Administrator\\Desktop\\ceshi.jpg");
        HttpEntity entity = MultipartEntityBuilder.create().addPart("imageData", new FileBody(file)).build();
        httpPost.setEntity(entity);
        CloseableHttpResponse response = null;
        HttpEntity resEntity = null;
        response = client.execute(httpPost);
        resEntity = response.getEntity();
        result = EntityUtils.toString(resEntity);
        System.out.println(result);
    }
}
