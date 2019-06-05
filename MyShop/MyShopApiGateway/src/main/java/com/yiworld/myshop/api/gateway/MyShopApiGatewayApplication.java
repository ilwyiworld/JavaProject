package com.yiworld.myshop.api.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;

@EnableHystrix
@EnableHystrixDashboard
@SpringBootApplication(scanBasePackages = "com.yiworld.myshop", exclude = DataSourceAutoConfiguration.class)
public class MyShopApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyShopApiGatewayApplication.class, args);
    }
}
