package com.yiworld.consumer;

import com.yiworld.framework.Invocation;
import com.yiworld.framework.ProxyFactory;
import com.yiworld.protocol.http.HttpClient;
import com.yiworld.provider.impl.HelloService;

public class Consumer {
    public static void main(String[] args) {
       /* HttpClient client=new HttpClient();
        Invocation invocation=new Invocation(HelloService.class.getName(),"sayHello",new Class[]{String.class},new Object[]{"yiworld"});
        String result=client.send("localhost",8080,invocation);
        System.out.println(result);*/
        HelloService helloService = ProxyFactory.getProxy(HelloService.class);
        System.out.println(helloService.sayHello("yi"));
    }
}
