package com.yiworld.annotation;

import java.lang.annotation.*;

/**
 * 在需要包装返回值的 Controller 的方法上使用此注解
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseResult {

}
