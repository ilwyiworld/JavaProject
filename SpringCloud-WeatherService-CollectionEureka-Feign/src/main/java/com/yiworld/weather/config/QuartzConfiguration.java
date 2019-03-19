package com.yiworld.weather.config;

import com.yiworld.weather.job.WeatherDataSyncJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Quartz Configuration.
 */
@Configuration
public class QuartzConfiguration {

	private static final int TIME = 1800; // 更新频率 30分钟

	// JobDetail
	@Bean
	public JobDetail weatherDataSyncJobDetail() {
		return JobBuilder.newJob(WeatherDataSyncJob.class).withIdentity("weatherDataSyncJob")
		.storeDurably().build();
	}
	
	// Trigger
	@Bean
	public Trigger weatherDataSyncTrigger() {
		
		SimpleScheduleBuilder schedBuilder = SimpleScheduleBuilder.simpleSchedule()
				.withIntervalInSeconds(TIME).repeatForever();
		
		return TriggerBuilder.newTrigger().forJob(weatherDataSyncJobDetail())
				.withIdentity("weatherDataSyncTrigger").withSchedule(schedBuilder).build();
	}
}
