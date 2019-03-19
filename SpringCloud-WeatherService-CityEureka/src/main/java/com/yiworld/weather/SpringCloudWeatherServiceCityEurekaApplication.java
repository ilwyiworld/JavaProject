package com.yiworld.weather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 城市数据API微服务
 */
@SpringBootApplication
@EnableDiscoveryClient
public class SpringCloudWeatherServiceCityEurekaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringCloudWeatherServiceCityEurekaApplication.class, args);
	}
}
