package com.yiworld;

import com.yiworld.demo.DemoService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Consumer {
    public static void main(String[] args) {
        //测试常规服务
        ClassPathXmlApplicationContext context =
                new ClassPathXmlApplicationContext(new String[]{"classpath*:kafka.consumer.xml"});
        context.start();
        System.out.println("kafka.consumer start");
        //DemoService demoService = context.getBean(DemoService.class);
        DemoService demoService = (DemoService)context.getBean("permissionService");
        System.out.println("kafka.consumer");
        System.out.println(demoService.getPermissions(1L));
    }
}
