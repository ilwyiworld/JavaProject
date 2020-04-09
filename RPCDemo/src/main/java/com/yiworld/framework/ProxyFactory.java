package com.yiworld.framework;

import com.yiworld.protocol.http.HttpProtocol;
import com.yiworld.register.RemoteRegister;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyFactory {
    public static <T> T getProxy(Class interfaceClass){
        return (T)Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Invocation invocation=new Invocation(interfaceClass.getName(),method.getName(),method.getParameterTypes(),args);
                URL url=RemoteRegister.random(interfaceClass.getName());
                Protocol protocol=ProtocolFactory.getProtocol();
                String result=protocol.send(url,invocation);
                return result;
            }
        });
    }
}
