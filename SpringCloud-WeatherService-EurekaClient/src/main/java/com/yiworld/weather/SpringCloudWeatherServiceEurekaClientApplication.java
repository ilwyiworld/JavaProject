package com.yiworld.weather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API微服务客户端
 */
@SpringBootApplication
@EnableDiscoveryClient
public class SpringCloudWeatherServiceEurekaClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringCloudWeatherServiceEurekaClientApplication.class, args);
	}
}
