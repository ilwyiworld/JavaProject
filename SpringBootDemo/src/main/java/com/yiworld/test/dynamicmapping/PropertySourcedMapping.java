package com.yiworld.test.dynamicmapping;

import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Mapping
/**
 * 添加一个注解用于动态注入请求 URL
 */
public @interface PropertySourcedMapping {
    String propertyKey();
}