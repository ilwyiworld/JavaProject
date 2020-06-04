package com.yiworld.route.exception;

import com.yiworld.common.exception.YiworldException;
import com.yiworld.common.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
public class ExceptionHandlingController {

    @ExceptionHandler(YiworldException.class)
    @ResponseBody()
    public BaseResponse handleAllExceptions(YiworldException ex) {
        log.error("exception", ex);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setCode(ex.getErrorCode());
        baseResponse.setMessage(ex.getMessage());
        return baseResponse ;
    }

}