package com.yiworld.myshop.service.upload.fastdfs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Java 配置方式定义 StorageFactory 的 Bean 使其可以被依赖注入
 */
@Configuration
public class FastDFSConfiguration {
    @Bean
    public StorageFactory storageFactory() {
        return new StorageFactory();
    }
}