package com.yiworld.example.client;

import com.yiworld.core.proxy.RpcClientProxy;
import com.yiworld.core.remoting.transport.ClientTransport;
import com.yiworld.core.remoting.transport.netty.client.NettyClientTransport;
import com.yiworld.serverapi.Hello;
import com.yiworld.serverapi.HelloService;

public class NettyClientMain {
    public static void main(String[] args) {
        ClientTransport rpcClient = new NettyClientTransport();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Hello("111", "222"));
        // 如需使用 assert 断言，需要在 VM options 添加参数：-ea
        assert "Hello description is 222".equals(hello);
        for (int i = 0; i < 50; i++) {
            String des = helloService.hello(new Hello("111", "~~~" + i));
            System.out.println(des);
        }
    }
}
