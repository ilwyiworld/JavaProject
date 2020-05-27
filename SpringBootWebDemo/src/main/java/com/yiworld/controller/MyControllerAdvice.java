package com.yiworld.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class MyControllerAdvice {

    // 全局数据绑定
    @ModelAttribute(name = "md")
    public Map<String, Object> myDate() {
        Map<String, Object> map = new HashMap<>();
        map.put("age", 90);
        map.put("gender", "男");
        return map;
    }



}
