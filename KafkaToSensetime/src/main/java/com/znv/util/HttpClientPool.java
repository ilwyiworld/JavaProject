package com.znv.util;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HttpContext;

/**
 * Created by Administrator on 2018/3/16.
 */
public class HttpClientPool {
    /**
     * 连接
     */
    private static final int MAX_TOTAL_CONN = 3000;

    /**
     * 单个路由的最大连接数
     */
    private static final int PER_ROUT_MAX_TOTAL_CONN = 100;

    private CloseableHttpClient httpClient = null;

    private HttpRequestRetryHandler httpRequestRetryHandler = new DefaultHttpRequestRetryHandler();// 默

    private static HttpClientPool instance = new HttpClientPool();

    private HttpClientPool() {
        init();
    }

    public static HttpClientPool getInstance() {
        return instance;
    }

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    private static final int KEEP_ALIVE_TIMEOUT = 30;

    private void init() {
        ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory sslsf = SSLConnectionSocketFactory.getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
                .register("http", plainsf).register("https", sslsf).build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
        //TODO
        int maxTotal = 0;
        // 设置最大连接数
        cm.setMaxTotal(maxTotal);
        // 设置每个路由的默认连接数
        //TODO
        int maxRouteConn = 0;
        cm.setDefaultMaxPerRoute(maxRouteConn);
        // 连接保持时间
        ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                // Honor 'keep-alive' header
                HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator());
                while (it.hasNext()) {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();
                    if (value != null && param.equalsIgnoreCase("timeout")) {
                        try {
                            return Long.parseLong(value) * 1000;
                        } catch (NumberFormatException ignore) {
                        }
                    }
                }
                //TODO
                int keepalivetimeout = 1000;
                return keepalivetimeout * 1000;
            }

        };
        httpClient = HttpClients.custom().setConnectionManager(cm).setRetryHandler(httpRequestRetryHandler)
                .setKeepAliveStrategy(myStrategy).build();
    }

}
