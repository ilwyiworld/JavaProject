package com.yiworld;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 熔断器
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableCircuitBreaker
public class SpringCloudWeatherServiceEurekaClientFeignHystrixApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringCloudWeatherServiceEurekaClientFeignHystrixApplication.class, args);
	}
}
