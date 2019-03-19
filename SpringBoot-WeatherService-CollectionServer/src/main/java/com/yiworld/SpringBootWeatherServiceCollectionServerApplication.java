package com.yiworld;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 天气数据收集微服务
 * 即时同步天气数据，保存在redis中，城市数据从城市数据API微服务来提供
 */
@SpringBootApplication
public class SpringBootWeatherServiceCollectionServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWeatherServiceCollectionServerApplication.class, args);
	}
}
