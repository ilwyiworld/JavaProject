package com.yiworld;

import com.yiworld.demo.DemoService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Cousumer2 {
    public static void main(String[] args) {
        //测试常规服务
        ClassPathXmlApplicationContext context =
                new ClassPathXmlApplicationContext(new String[]{"classpath:kafka.consumer.xml"});
        context.start();
        System.out.println("consumer2 start");
        DemoService demoService = context.getBean(DemoService.class);
        System.out.println("consumer2");
        System.out.println(demoService.getPermissions(1L));
    }
}
