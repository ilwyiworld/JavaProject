package com.znv.request;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import javax.ws.rs.core.MediaType;

/**
 * Created by Administrator on 2017/7/12.
 */
public class Delete {

    /**
     * 删除
     */
    public static String sendDeleteData(String url, String data) {
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        WebResource resource = client.resource(url);
        try {
            //只接受json格式的参数
            return resource.type("application/json").accept(MediaType.APPLICATION_JSON).delete(String.class,data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
