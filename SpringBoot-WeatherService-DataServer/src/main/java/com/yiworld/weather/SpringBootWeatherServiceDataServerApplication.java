package com.yiworld.weather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * 天气数据API微服务
 */
@SpringBootApplication
public class SpringBootWeatherServiceDataServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWeatherServiceDataServerApplication.class, args);
	}
}
