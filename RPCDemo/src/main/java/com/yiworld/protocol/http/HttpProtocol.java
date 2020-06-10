package com.yiworld.protocol.http;

import com.yiworld.framework.Invocation;
import com.yiworld.framework.Protocol;
import com.yiworld.framework.URL;

public class HttpProtocol implements Protocol {
    @Override
    public void start(URL url) {
        new HttpServer().start(url.getHostname(), url.getPort());
    }

    @Override
    public String send(URL url, Invocation invocation) {
        return new HttpClient().send(url.getHostname(), url.getPort(), invocation);
    }
}
