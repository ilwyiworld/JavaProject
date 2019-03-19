package com.yiworld.weather.controller;

import com.yiworld.weather.service.CityClient;
import com.yiworld.weather.vo.City;
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
	public List<City> listCity() throws Exception{
		// 通过Feign客户端来查找
		return cityClient.listCity();
	}
}
