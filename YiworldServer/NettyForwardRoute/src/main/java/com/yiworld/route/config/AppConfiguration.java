package com.yiworld.route.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Function:
 */
@Data
@Component
public class AppConfiguration {

    @Value("${app.zk.root}")
    private String zkRoot;

    @Value("${app.zk.addr}")
    private String zkAddr;

    @Value("${server.port}")
    private int port;

    @Value("${app.zk.connect.timeout}")
    private int zkConnectTimeout;

    @Value("${app.route.way}")
    private String routeWay;

    @Value("${app.route.way.consitenthash}")
    private String consistentHashWay;
}
