package com.yiworld.weather.service;

import com.yiworld.weather.vo.City;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * City Client.
 */
@FeignClient("SpringCloud-WeatherService-CityEureka")
public interface CityClient {
	
	@GetMapping("/cities")
	List<City> listCity() throws Exception;
}
