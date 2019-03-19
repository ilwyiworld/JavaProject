package com.yiworld.weather.service;

import com.yiworld.weather.vo.Weather;
import com.yiworld.weather.vo.WeatherResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Weather Report Service.
 */
@Service
public class WeatherReportServiceImpl implements WeatherReportService {

	@Autowired
	private DataClient dataClient;

	@Override
	public Weather getDataByCityId(String cityId) {
		// 由网关API微服务来提供
		WeatherResponse resp = dataClient.getDataByCityId(cityId);
		Weather data = resp.getData();
		return data;
	}

}
