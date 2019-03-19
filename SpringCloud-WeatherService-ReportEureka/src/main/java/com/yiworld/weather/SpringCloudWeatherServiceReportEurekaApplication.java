package com.yiworld.weather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 天气预报微服务
 */
@SpringBootApplication
@EnableDiscoveryClient
public class SpringCloudWeatherServiceReportEurekaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringCloudWeatherServiceReportEurekaApplication.class, args);
	}
}
