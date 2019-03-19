package com.yiworld;

import com.yiworld.conf.PropertiesConfig;
import com.yiworld.conf.PropertiesListenerConfig;
import com.yiworld.listener.PropertiesListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RestController
public class SpringDemoWithFreemarkApplication {

	@Autowired
	private PropertiesConfig propertiesConfig;

	/**
	 * 第一种方式：使用`@ConfigurationProperties`注解将配置文件属性注入到配置对象类中
	 */
	@RequestMapping("/config")
	public Map<String, Object> configurationProperties() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("type", propertiesConfig.getType3());
		map.put("title", propertiesConfig.getTitle3());
		map.put("login", propertiesConfig.getLogin());
		map.put("urls", propertiesConfig.getUrls());
		return map;
	}

	@Value("${com.yiworld.type}")
	private String type;

	@Value("${com.yiworld.title}")
	private String title;

	/**
	 * 第二种方式：使用`@Value("${propertyName}")`注解
	 */
	@RequestMapping("/value")
	public Map<String, Object> value() throws UnsupportedEncodingException {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("type", type);
		map.put("title", title);
		return map;
	}

	@Autowired
	private Environment env;

	/**
	 * 第三种方式：使用`Environment`
	 */
	@RequestMapping("/env")
	public Map<String, Object> env() throws UnsupportedEncodingException {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("type", env.getProperty("com.yiworld.type2"));
		map.put("title", env.getProperty("com.yiworld.title2"));
		return map;
	}

	/**
	 * 第四种方式：通过注册监听器(`Listeners`) + `PropertiesLoaderUtils`的方式
	 */
	@RequestMapping("/listener")
	public Map<String, Object> listener() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.putAll(PropertiesListenerConfig.getAllProperty());
		return map;
	}

	public static void main(String[] args) throws Exception {
		SpringApplication application = new SpringApplication(SpringDemoWithFreemarkApplication.class);
		// 第四种方式：注册监听器
		application.addListeners(new PropertiesListener("app-config.properties"));
		application.run(args);
	}
}
