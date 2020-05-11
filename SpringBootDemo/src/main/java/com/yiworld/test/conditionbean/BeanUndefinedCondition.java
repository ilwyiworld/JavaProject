package com.yiworld.test.conditionbean;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * 如果指定的 Bean 全部不存在，才注入某个 Bean
 * 模拟 SpringBoot 的 @ConditionalOnMissingBean 注解
 */
public class BeanUndefinedCondition implements Condition {
    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        Map<String, Object> map = annotatedTypeMetadata.getAnnotationAttributes(ConditionalOnBeanUndefined.class.getName());
        ConfigurableListableBeanFactory beanFactory = conditionContext.getBeanFactory();

        Class<?>[] clazzArray = (Class<?>[]) map.get("value");
        if (clazzArray != null && clazzArray.length > 0) {
            int count = 0;
            for (Class<?> clazz : clazzArray) {
                try {
                    //如果目标Bean不存在，则会抛出异常
                    Object obj = beanFactory.getBean(clazz);
                } catch (Exception e) {
                    //计数
                    count++;
                }
            }
            //如果指定的Bean全部不存在，则返回true
            return count == clazzArray.length;
        }
        return false;
    }
}
