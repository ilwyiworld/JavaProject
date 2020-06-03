package com.yiworld.route.exception;

import com.yiworld.common.exception.YiworldException;
import com.yiworld.common.response.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class ExceptionHandlingController {

    private static Logger logger = LoggerFactory.getLogger(ExceptionHandlingController.class) ;

    @ExceptionHandler(YiworldException.class)
    @ResponseBody()
    public BaseResponse handleAllExceptions(YiworldException ex) {
        logger.error("exception", ex);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setCode(ex.getErrorCode());
        baseResponse.setMessage(ex.getMessage());
        return baseResponse ;
    }

}