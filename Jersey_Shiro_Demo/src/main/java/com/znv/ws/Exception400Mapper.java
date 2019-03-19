package com.znv.ws;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Created by Administrator on 2017/7/14.
 * 处理json数据格式解析错误的400请求
 */
@Provider
public class Exception400Mapper implements ExceptionMapper<JSONException> {
    @Override
    public Response toResponse(JSONException e) {
        JSONObject exception=new JSONObject();
        exception.put("error",Status.BAD_REQUEST);
        exception.put("message",e.getMessage());
        return Response.status(Status.BAD_REQUEST).entity(exception.toJSONString()).type(MediaType.APPLICATION_JSON).build();
    }
}
