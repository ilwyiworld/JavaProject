package com.yiworld;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 天气数据收集微服务
 * 即时同步天气数据，保存在redis中，城市数据从城市数据API微服务来提供
 */
@SpringBootApplication
@EnableDiscoveryClient
public class SpringCloudWeatherServiceCollectionEurekaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringCloudWeatherServiceCollectionEurekaApplication.class, args);
	}
}
