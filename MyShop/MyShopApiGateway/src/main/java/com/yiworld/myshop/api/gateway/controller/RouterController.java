package com.yiworld.myshop.api.gateway.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.rpc.RpcContext;
import com.yiworld.myshop.service.content.api.ContentConsumerService;
import com.yiworld.myshop.service.user.api.UserConsumerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "router")
public class RouterController {

    @Reference(version = "${services.versions.user.v1}")
    private UserConsumerService userConsumerService;

    @Reference(version = "${services.versions.content.v1}")
    private ContentConsumerService contentConsumerService;

    @Value("${services.port.user}")
    private String userPort;

    @Value("${services.port.content}")
    private String contentPort;

    @GetMapping(value = "user")
    public String user(String path) {
        // 远程调用
        userConsumerService.info();
        // 本端是否为消费端
        boolean isConsumerSide = RpcContext.getContext().isConsumerSide();
        if(isConsumerSide){
            // 获取最后一次调用的提供方IP地址
            String serverIP = RpcContext.getContext().getRemoteHost();
            // 获取当前服务配置信息，所有配置信息都将转换为URL的参数
            //String application = RpcContext.getContext().getUrl().getParameter("application");
            return String.format("redirect:http://%s:%s%s",serverIP,userPort,path);
        }
        return null;
    }

    @GetMapping(value = "content")
    public String content(String path) {
        // 远程调用
        contentConsumerService.info();
        // 本端是否为消费端
        boolean isConsumerSide = RpcContext.getContext().isConsumerSide();
        if(isConsumerSide){
            // 获取最后一次调用的提供方IP地址
            String serverIP = RpcContext.getContext().getRemoteHost();
            // 获取当前服务配置信息，所有配置信息都将转换为URL的参数
            return String.format("redirect:http://%s:%s%s",serverIP,contentPort,path);
        }
        return null;
    }
}
