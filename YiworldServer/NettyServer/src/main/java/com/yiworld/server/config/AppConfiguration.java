package com.yiworld.server.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class AppConfiguration {

    @Value("${app.zk.root}")
    private String zkRoot;

    @Value("${app.zk.addr}")
    private String zkAddr;

    @Value("${app.zk.switch}")
    private boolean zkSwitch;

    @Value("${yiworld.server.port}")
    private int serverPort;

    @Value("${yiworld.route.url}")
    private String routeUrl ;

    @Value("${yiworld.heartbeat.time}")
    private long heartBeatTime ;
    
    @Value("${app.zk.connect.timeout}")
    private int zkConnectTimeout;
}
