package com.yiworld;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

//Eureka服务端
@SpringBootApplication
@EnableEurekaServer
public class SpringCloudWeatherSevrviceEurekaServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringCloudWeatherSevrviceEurekaServerApplication.class, args);
	}
}
