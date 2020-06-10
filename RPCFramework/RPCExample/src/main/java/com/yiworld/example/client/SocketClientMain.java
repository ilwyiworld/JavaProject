package com.yiworld.example.client;

import com.yiworld.core.proxy.RpcClientProxy;
import com.yiworld.core.remoting.transport.ClientTransport;
import com.yiworld.core.remoting.transport.socket.SocketRpcClient;
import com.yiworld.serverapi.Hello;
import com.yiworld.serverapi.HelloService;

public class SocketClientMain {
    public static void main(String[] args) {
        ClientTransport clientTransport = new SocketRpcClient();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(clientTransport);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Hello("111", "222"));
        System.out.println(hello);
    }
}
