package com.yiworld.weather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 天气预报微服务客户端
 * 依赖于城市数据微服务API和天气数据微服务API
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class SpringCloudWeatherServiceReportEurekaFeignApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringCloudWeatherServiceReportEurekaFeignApplication.class, args);
	}
}
