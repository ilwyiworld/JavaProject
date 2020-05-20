package com.yiworld.controller;

import com.yiworld.annotation.AccessLimit;
import com.yiworld.annotation.ApiIdempotent;
import com.yiworld.annotation.LoggerManage;
import com.yiworld.annotation.ResponseResult;
import com.yiworld.common.ServerResponse;
import com.yiworld.pojo.Mail;
import com.yiworld.service.TestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

    @Autowired
    private TestService testService;

    @ApiIdempotent
    @PostMapping("testIdempotence")
    public ServerResponse testIdempotence() {
        return testService.testIdempotence();
    }

    @AccessLimit(maxCount = 5, seconds = 5)
    @PostMapping("accessLimit")
    public ServerResponse accessLimit() {
        return testService.accessLimit();
    }

    @PostMapping("send")
    public ServerResponse sendMail(@Validated Mail mail, Errors errors) {
        if (errors.hasErrors()) {
            String msg = errors.getFieldError().getDefaultMessage();
            return ServerResponse.error(msg);
        }
        return testService.send(mail);
    }

    @PostMapping("testResult")
    @ResponseResult
    @ResponseBody
    @LoggerManage(description = "日志管理")
    public Object testResult() {
        return "测试数据";
    }
}
