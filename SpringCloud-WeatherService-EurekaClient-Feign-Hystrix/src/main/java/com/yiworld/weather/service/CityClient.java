package com.yiworld.weather.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * City Client.
 */
@FeignClient("SpringCloud-WeatherService-CityEureka")
public interface CityClient {

    @GetMapping("/cities")
    String listCity();
}
