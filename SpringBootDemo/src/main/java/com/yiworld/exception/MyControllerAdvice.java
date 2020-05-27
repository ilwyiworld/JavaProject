package com.yiworld.exception;

import com.yiworld.common.ResponseCode;
import com.yiworld.common.ServerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class MyControllerAdvice {

    @ResponseBody
    @ExceptionHandler(ServiceException.class)
    public ServerResponse serviceExceptionHandler(ServiceException se) {
        return ServerResponse.error(se.getMsg());
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ServerResponse exceptionHandler(Exception e) {
        log.error("Exception: ", e);
        return ServerResponse.error(ResponseCode.SERVER_ERROR.getMsg());
    }

    // 全局数据绑定
    @ModelAttribute(name = "md")
    public Map<String, Object> myDate() {
        Map<String, Object> map = new HashMap<>();
        map.put("age", 90);
        map.put("gender", "男");
        return map;
    }

    @GetMapping("hello")
    public void hello(Model model) {
        Map<String, Object> map = model.asMap();
        System.out.println(map);
        // {md={gender=男, age=90}}
    }
}
