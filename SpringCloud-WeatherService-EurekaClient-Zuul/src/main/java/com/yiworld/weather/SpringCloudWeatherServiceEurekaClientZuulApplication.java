package com.yiworld.weather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * 请求转发
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableZuulProxy
public class SpringCloudWeatherServiceEurekaClientZuulApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringCloudWeatherServiceEurekaClientZuulApplication.class, args);
	}
}
