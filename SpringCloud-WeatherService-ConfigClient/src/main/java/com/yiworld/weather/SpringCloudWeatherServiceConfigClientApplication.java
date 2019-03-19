package com.yiworld.weather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SpringCloudWeatherServiceConfigClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringCloudWeatherServiceConfigClientApplication.class, args);
	}
}
