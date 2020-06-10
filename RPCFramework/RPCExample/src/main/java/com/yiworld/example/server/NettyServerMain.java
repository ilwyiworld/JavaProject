package com.yiworld.example.server;

import com.yiworld.core.remoting.transport.netty.server.NettyServer;
import com.yiworld.serverapi.HelloService;

public class NettyServerMain {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        NettyServer nettyServer = new NettyServer("127.0.0.1", 9999);
        nettyServer.publishService(helloService, HelloService.class);
    }
}