package com.yiworld.listener;

import com.yiworld.conf.PropertiesListenerConfig;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;


/**
 * 配置文件监听器，用来加载自定义配置文件
 */
public class PropertiesListener implements ApplicationListener<ApplicationStartedEvent> {
	
	private String propertyFileName;

	public PropertiesListener(String propertyFileName) {
		this.propertyFileName = propertyFileName;
	}

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		PropertiesListenerConfig.loadAllProperties(propertyFileName);
	}
}
