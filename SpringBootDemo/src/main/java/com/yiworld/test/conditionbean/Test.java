package com.yiworld.test.conditionbean;

import org.springframework.context.annotation.Bean;

public class Test {
    // 测试
    @Bean
    @ConditionalOnBeanUndefined(CustomRealm.class)
    public CustomRealm customRealm(){
        return new CustomRealm();
    }
}

class CustomRealm{

}
