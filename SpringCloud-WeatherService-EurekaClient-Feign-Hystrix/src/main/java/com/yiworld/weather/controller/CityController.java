package com.yiworld.weather.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.yiworld.weather.service.CityClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * City Controller.
 */
@RestController
public class CityController {
	@Autowired
	private CityClient cityClient;

	@GetMapping("/cities")
	@HystrixCommand(fallbackMethod="defaultCities")	//断路器设置
	public String listCity() {
		// 通过Feign客户端来查找
		String body = cityClient.listCity();
		return body;
	}

	public String defaultCities() {
		return "City Data Server is down!";
	}
}
