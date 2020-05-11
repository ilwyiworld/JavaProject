package com.yiworld.test.conditionbean;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(BeanUndefinedCondition.class)
/**
 * 使用注解{@link org.springframework.context.annotation.Bean}生成 Bean 时，
 * 会使用该注解检测目标 Bean 是否已经存在
 */
public @interface ConditionalOnBeanUndefined {
    Class<?>[] value() default {};
}
