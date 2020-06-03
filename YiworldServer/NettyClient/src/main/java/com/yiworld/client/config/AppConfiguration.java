package com.yiworld.client.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class AppConfiguration {

    @Value("${yiworld.user.id}")
    private Long userId;

    @Value("${yiworld.user.userName}")
    private String userName;

    @Value("${yiworld.msg.logger.path}")
    private String msgLoggerPath ;

    @Value("${yiworld.clear.route.request.url}")
    private String clearRouteUrl ;

    @Value("${yiworld.heartbeat.time}")
    private long heartBeatTime ;

    @Value("${yiworld.reconnect.count}")
    private int errorCount ;
}
