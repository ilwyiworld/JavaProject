package com.yiworld.weather.config;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

/**
 * Rest Configuration.
 */
@Configuration
public class HttpConfiguration {
	
	@Autowired
	private RestTemplateBuilder builder;

	//SpringBoot1.5.*版本发送请求会有中文乱码，改用httpclient
	//SpringBoot2.0.0正常
	@Bean
	public RestTemplate restTemplate() {
		RestTemplate restTemplate= builder.build();
		//RestTemplate restTemplate= new RestTemplate();
		//restTemplate.getMessageConverters().set(2, new StringHttpMessageConverter(StandardCharsets.UTF_8));
		//restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
		return restTemplate;
	}

	@Bean
	public CloseableHttpClient httpClient(){
		return HttpClients.createDefault();
	}
}
