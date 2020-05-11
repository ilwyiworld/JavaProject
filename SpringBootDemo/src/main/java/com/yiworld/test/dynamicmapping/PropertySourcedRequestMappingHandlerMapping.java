package com.yiworld.test.dynamicmapping;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.UriTemplate;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 用于在 {@link Controller} 的 {@link RequestMapping} 中动态注入请求URL
 * 添加一个自定义的 HandlerMapping，用于在 Bean 初始化的时候修改请求 URL
 */
public class PropertySourcedRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    private final Map<String, HandlerMethod> handlerMethods = new LinkedHashMap<>();
    private final Object handler;

    public PropertySourcedRequestMappingHandlerMapping(
            Object handler) {
        this.handler = handler;
    }

    @Override
    protected void initHandlerMethods() {
        logger.debug("initialising the handler methods");
        setOrder(Ordered.HIGHEST_PRECEDENCE + 1000);
        Class<?> clazz = handler.getClass();
        if (isHandler(clazz)) {
            for (Method method : clazz.getMethods()) {
                // 获取参数注解
                PropertySourcedMapping mapper = AnnotationUtils.getAnnotation(method, PropertySourcedMapping.class);
                if (mapper != null) {
                    RequestMappingInfo mapping = getMappingForMethod(method, clazz);
                    HandlerMethod handlerMethod = createHandlerMethod(handler, method);
                    String mappingPath = mappingPath(mapper);
                    if (mappingPath != null) {
                        logger.info(String.format("Mapped URL path [%s] onto method [%s]", mappingPath, handlerMethod.toString()));
                        //将 urlPath 和对应的处理 HandlerMethod 放到 LinkedHashMap
                        handlerMethods.put(mappingPath, handlerMethod);
                    } else {
                        for (String path : mapping.getPatternsCondition().getPatterns()) {
                            logger.info(String.format("Mapped URL path [%s] onto method [%s]", path, handlerMethod.toString()));
                            handlerMethods.put(path, handlerMethod);
                        }
                    }
                }
            }
        }
    }

    /**
     * 根据参数注解获取请求URL
     */
    private String mappingPath(final PropertySourcedMapping mapper) {
        final String key = mapper.propertyKey();
        if (StringUtils.isNoneBlank(key)) {
            try {
                return "";
            } catch (Exception e) {
                logger.error(MessageFormat.format("获取参数[{0}]对应的值失败：", key), e);
            }
        }
        return null;
    }

    @Override
    protected boolean isHandler(Class<?> beanType) {
        return ((AnnotationUtils.findAnnotation(beanType, Controller.class) != null) ||
                (AnnotationUtils.findAnnotation(beanType, RequestMapping.class) != null));
    }

    /**
     * 通过请求的 urlPath 查找处理的 {@link HandlerMethod}
     */
    @Override
    protected HandlerMethod lookupHandlerMethod(String urlPath, HttpServletRequest request) {
        logger.debug("looking up handler for path: " + urlPath);
        HandlerMethod handlerMethod = handlerMethods.get(urlPath);
        if (handlerMethod != null) {
            return handlerMethod;
        }
        for (String path : handlerMethods.keySet()) {
            UriTemplate template = new UriTemplate(path);
            if (template.matches(urlPath)) {
                request.setAttribute(
                        HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
                        template.match(urlPath));
                return handlerMethods.get(path);
            }
        }
        return null;
    }
}
