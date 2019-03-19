package com.yiworld.weather.job;

import com.yiworld.weather.service.CityClient;
import com.yiworld.weather.service.WeatherDataCollectionService;
import com.yiworld.weather.vo.City;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Weather Data Sync Job.
 */
public class WeatherDataSyncJob extends QuartzJobBean {
	
	private final static Logger logger = LoggerFactory.getLogger(WeatherDataSyncJob.class);

	@Autowired
	private WeatherDataCollectionService weatherDataCollectionService;

	@Autowired
	private CityClient cityClient;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		logger.info("Weather Data Sync Job. Start！");
		// 获取城市ID列表
		// TODO 改为由城市数据API微服务来提供数据
		List<City> cityList = null;

		try {
			// 由城市数据API微服务提供数据
			cityList = cityClient.listCity();
		} catch (Exception e) {
			logger.error("Exception!", e);
		}

		// 遍历城市ID获取天气
		for (City city : cityList) {
			String cityId = city.getCityId();
			logger.info("Weather Data Sync Job, cityId:" + cityId);
			weatherDataCollectionService.syncDateByCityId(cityId);
		}

		logger.info("Weather Data Sync Job. End！");
	}

}
