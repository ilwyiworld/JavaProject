package com.yiworld.weather.service;

import com.yiworld.weather.vo.City;

import java.util.List;


/**
 * City Data Service.
 */
public interface CityDataService {

	/**
	 * 获取City列表
	 * @return
	 * @throws Exception
	 */
	List<City> listCity() throws Exception;
}
