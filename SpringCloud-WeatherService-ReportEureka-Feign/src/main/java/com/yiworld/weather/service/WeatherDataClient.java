package com.yiworld.weather.service;

import com.yiworld.weather.vo.WeatherResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


/**
 * Weather Data Client.
 */
@FeignClient("SpringCloud-WeatherService-DataEureka")
public interface WeatherDataClient {
	
	@GetMapping("/weather/cityId/{cityId}")
	WeatherResponse getDataByCityId(@PathVariable("cityId") String cityId);
}
