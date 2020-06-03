package com.yiworld.server.config;

import com.yiworld.server.endpoint.CustomEndpoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Function: 监控端点配置
 */
@Configuration
public class EndPointConfig {

    @Value("${monitor.channel.map.key}")
    private String channelMap;

    @Bean
    public CustomEndpoint buildEndPoint(){
        CustomEndpoint customEndpoint = new CustomEndpoint(channelMap) ;
        return customEndpoint ;
    }
}
