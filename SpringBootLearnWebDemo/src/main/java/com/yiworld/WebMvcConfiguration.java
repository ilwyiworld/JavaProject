package com.yiworld;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by Administrator on 2019-1-11.
 */
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {
    @Value("${file.location}")
    String picPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("image/**").addResourceLocations("file:./".concat(picPath).concat("/"));
        super.addResourceHandlers(registry);
    }
}
