package com.yiworld.provider.impl;

public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String hello) {
        return "hello ".concat(hello);
    }
}
