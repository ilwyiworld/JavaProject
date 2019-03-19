package com.znv.request;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import javax.ws.rs.core.MediaType;

/**
 * Created by Administrator on 2017/7/12.
 */
public class Get {

    public static WebResource getResource(String url) {
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        WebResource resource = client.resource(url);
        //WebResource requestResource = resource.path("rest").path(PATH_REQUEST);
        return resource;
    }

    /**
     * 返回客户端请求。 例如： GET
     * http://localhost:8090/RESTfulWS/rest/UserInfoService/name/Pavithra
     * 返回请求结果状态“200 OK”。
     */
    private static String getClientResponse(String url) {
        WebResource resource =getResource(url);
        return resource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class).toString();
    }

    /**
     * 返回请求结果XML 例如：<User><Name>Pavithra</Name></User>
     */
    private static String getResponse(String url) {
        WebResource resource =getResource(url);
        return resource.accept(MediaType.APPLICATION_JSON).get(String.class);
    }
}
