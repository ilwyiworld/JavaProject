package com.yiworld.myshop.service.content.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;

@EnableHystrix
@EnableHystrixDashboard
@SpringBootApplication(scanBasePackages = "com.yiworld.myshop",exclude = DataSourceAutoConfiguration.class)
public class MyShopServiceContentConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyShopServiceContentConsumerApplication.class, args);
    }
}