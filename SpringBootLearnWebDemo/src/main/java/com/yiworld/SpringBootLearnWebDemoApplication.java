package com.yiworld;

import com.yiworld.storage.StorageProperties;
import com.yiworld.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executor;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
//@EnableAsync	//开启异步任务
//@EnableScheduling    //开启调度任务
public class SpringBootLearnWebDemoApplication extends AsyncConfigurerSupport {

	private static final Logger log = LoggerFactory.getLogger(SpringBootLearnWebDemoApplication.class);

/*	public static void main(String args[]) {
		RestTemplate restTemplate = new RestTemplate();
		String quote = restTemplate.getForObject("http://gturnquist-quoters.cfapps.io/api/random", String.class);
		log.info(quote.toString());
	}*/

	public static void main(String args[]) {
		SpringApplication.run(SpringBootLearnWebDemoApplication.class);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	//通过RestTemplate消费服务
	//这个服务是http:///gturnquist-quoters.cfapps.io/api/random,它会随机返回Json字符串。
	/*@Bean
	public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
		return args -> {
			String quote = restTemplate.getForObject(
					"http://gturnquist-quoters.cfapps.io/api/random", String.class);
			log.info(quote.toString());
		};
	}*/

	@Bean
	CommandLineRunner init(StorageService storageService) {
		return (args) -> {
			storageService.deleteAll();
			storageService.init();
		};
	}

	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);	//最大的线程池为2
		executor.setMaxPoolSize(2);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("GithubLookup-");
		executor.initialize();
		return executor;
	}
}
