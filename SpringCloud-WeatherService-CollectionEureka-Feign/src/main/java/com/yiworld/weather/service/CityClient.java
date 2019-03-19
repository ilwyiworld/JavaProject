package com.yiworld.weather.service;

import java.util.List;
import com.yiworld.weather.vo.City;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * City Client.
 */
@FeignClient("SpringCloud-WeatherService-CityEureka")
public interface CityClient {
	
	@GetMapping("/cities")
	List<City> listCity() throws Exception;
}
