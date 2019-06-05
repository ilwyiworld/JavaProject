package com.yiworld.testMethodReplace;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test {
    public static void main(String[] args) {
        ApplicationContext bf=new ClassPathXmlApplicationContext("test/replacedMethodTest.xml");
        TestChangeMethod test=(TestChangeMethod)bf.getBean("testChangeMethod");
        test.changeMe();
    }
}
