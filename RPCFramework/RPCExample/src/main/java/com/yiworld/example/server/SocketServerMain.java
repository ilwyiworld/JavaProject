package com.yiworld.example.server;

import com.yiworld.core.remoting.transport.socket.SocketRpcServer;
import com.yiworld.serverapi.HelloService;

public class SocketServerMain {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        SocketRpcServer socketRpcServer = new SocketRpcServer("127.0.0.1", 8080);
        socketRpcServer.publishService(helloService, HelloService.class);
    }
}