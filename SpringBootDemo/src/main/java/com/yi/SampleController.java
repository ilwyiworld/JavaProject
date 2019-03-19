package com.yi;

import com.yi.bean.UserBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Administrator on 2017/10/10.
 */
@Controller
@EnableAutoConfiguration
@EnableConfigurationProperties({UserBean.class})
@Configuration
public class SampleController {

    @Autowired
    private UserBean userBean;

    @Value("${com.yi.name}")
    private String name;
    @Value("${com.yi.age}")
    private String age;

    @RequestMapping("/")
    @ResponseBody
    String home(){
        return "hello world";
    }

    @RequestMapping("/name")
    @ResponseBody
    String sayHello(){
        return name+","+age+"岁";
    }

    @RequestMapping("/name2")
    @ResponseBody
    String sayHello2(){
        return userBean.getName()+","+userBean.getAge()+"岁岁";
    }

}
