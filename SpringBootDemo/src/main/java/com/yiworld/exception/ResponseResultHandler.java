package com.yiworld.exception;

import com.yiworld.annotation.ResponseResult;
import com.yiworld.common.ServerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
public class ResponseResultHandler implements ResponseBodyAdvice<Object> {
    // 标记名称
    public static final String RESPONSE_RESULT = "RESPONSE_RESULT";

    // 请求是否包含了“包装注解” 标志，没有直接返回，不需要重写返回体
    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        // 使用SpringBoot RequestContextHolder 提供的方法来获取 Request 对象，来获取请求参数判断需要返回的对应类型
        ServletRequestAttributes sqa = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = sqa.getRequest();
        // 判断请求是否有包装标志
        ResponseResult responseResult = (ResponseResult) request.getAttribute(RESPONSE_RESULT);
        return ! (responseResult == null);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        log.info("进入返回体，重写格式进行中...");
        return ServerResponse.success(body);
    }
}
