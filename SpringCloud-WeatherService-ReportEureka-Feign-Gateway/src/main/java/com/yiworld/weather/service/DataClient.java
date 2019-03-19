package com.yiworld.weather.service;

import java.util.List;

import com.yiworld.weather.vo.City;
import com.yiworld.weather.vo.WeatherResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Data Client.
 * 整合了城市数据API和天气数据微服务API
 * 通过网关API来获取
 */
@FeignClient("SpringCloud-WeatherService-EurekaClient-Zuul")
public interface DataClient {
	/**
	 * 获取城市列表
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/city/cities")
	List<City> listCity() throws Exception;
	
	/**
	 * 根据城市ID查询天气数据
	 */
	@GetMapping("/data/weather/cityId/{cityId}")
	WeatherResponse getDataByCityId(@PathVariable("cityId") String cityId);
}
