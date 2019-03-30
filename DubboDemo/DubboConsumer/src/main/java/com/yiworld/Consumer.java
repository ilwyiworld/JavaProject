package com.yiworld;

import com.yiworld.demo.DemoService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Consumer {
    public static void main(String[] args) {
        //测试常规服务
        ClassPathXmlApplicationContext context =
                new ClassPathXmlApplicationContext(new String[]{"classpath*:consumer.xml"});
        context.start();
        System.out.println("consumer start");
        //DemoService demoService = context.getBean(DemoService.class);
        DemoService demoService = (DemoService)context.getBean("permissionService");
        System.out.println("consumer");
        System.out.println(demoService.getPermissions(1L));
    }
}
