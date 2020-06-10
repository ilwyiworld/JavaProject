package com.yiworld.provider;

import com.yiworld.framework.Protocol;
import com.yiworld.framework.ProtocolFactory;
import com.yiworld.framework.URL;
import com.yiworld.register.LocalRegister;
import com.yiworld.provider.impl.HelloService;
import com.yiworld.provider.impl.HelloServiceImpl;
import com.yiworld.register.RemoteRegister;

public class Provider {
    public static void main(String[] args) {
        //  1.本地注册
        //  服务名：实现类
        LocalRegister.register(HelloService.class.getName(), HelloServiceImpl.class);

        //  2.远程注册
        // 服务名：List<机器地址>
        URL url = new URL("localhost", 8080);
        RemoteRegister.register(HelloService.class.getName(), url);

        //  3.启动服务器 tomcat
        /*HttpServer server =new HttpServer();
        server.start("localhost",8080);*/
        Protocol protocol = ProtocolFactory.getProtocol();
        protocol.start(url);
    }
}
