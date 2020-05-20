package com.yiworld.annotation;

import java.lang.annotation.*;

/**
 * @Description: 日志注解
 */
@Target(ElementType.METHOD)  
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LoggerManage {
	public String description();
}
