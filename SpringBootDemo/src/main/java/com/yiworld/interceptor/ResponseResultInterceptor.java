package com.yiworld.interceptor;

import com.yiworld.annotation.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 拦截请求，是否此请求返回的值需要包装，其实就是运行的时候，解析@ResponseResult注解
 */
@Slf4j
public class ResponseResultInterceptor implements HandlerInterceptor {

    // 标记名称
    public static final String RESPONSE_RESULT = "RESPONSE_RESULT";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            final HandlerMethod handlerMethod = (HandlerMethod) handler;
            final Class<?> clazz = handlerMethod.getBeanType();
            final Method method = handlerMethod.getMethod();
            // 判断是否在类上加了注解
            if (clazz.isAnnotationPresent(ResponseResult.class)) {
                // 设置此请求返回体需要包装，向下传递，在 ResponseBodyAdvice 接口进行判断
                request.setAttribute(RESPONSE_RESULT, clazz.getAnnotation(ResponseResult.class));
            } else if (method.isAnnotationPresent(ResponseResult.class)) {
                request.setAttribute(RESPONSE_RESULT, method.getAnnotation(ResponseResult.class));
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }

}
