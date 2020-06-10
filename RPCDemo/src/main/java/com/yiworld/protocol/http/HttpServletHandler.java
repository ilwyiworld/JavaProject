package com.yiworld.protocol.http;

import com.yiworld.framework.Invocation;
import com.yiworld.register.LocalRegister;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;

public class HttpServletHandler {
    public void handler(HttpServletRequest req, HttpServletResponse resp) {
        //处理请求，返回结果
        try {
            InputStream inputStream = req.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(inputStream);
            Invocation invocation = (Invocation) ois.readObject();
            //实现类
            Class implClass = LocalRegister.get(invocation.getInterfaceName());
            Method method = implClass.getMethod(invocation.getMethodName(), invocation.getParamTypes());
            String result = (String) method.invoke(implClass.newInstance(), invocation.getParams());
            IOUtils.write(result, resp.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
