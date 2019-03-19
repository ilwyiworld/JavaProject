package com.yiworld.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker //注解表示开启使用STOMP协议来传输基于代理的消息，Broker就是代理的意思
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {
    //注册STOMP协议的节点，并指定映射的URL
    @Override
    public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
        //注册STOMP协议节点，同时指定使用SockJS协议
        stompEndpointRegistry.addEndpoint("/endpointSang").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //配置消息代理，由于我们是实现推送功能，这里的消息代理是/topic
        registry.enableSimpleBroker("/topic");
    }
}
