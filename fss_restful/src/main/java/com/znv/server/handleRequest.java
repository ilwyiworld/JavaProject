package com.znv.server;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.znv.constant.QueryIdConstant;
import com.znv.constant.QueryTypeConstant;
import com.znv.constant.RestURIConstant;
import com.znv.sdk.esSdk;
import com.znv.sdk.hbaseSdk;
import com.znv.sdk.phoenixSdk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// 这里@Path定义了类的层次路径。
// 指定了资源类提供服务的URI路径。
@Path("/")
public class handleRequest {

    private static final Logger LOGGER = LogManager.getLogger(handleRequest.class.getName());

    @GET
    // 这里@Path定义了类的层次路径。指定了资源类提供服务的URI路径。
    @Path("/test/{name}")
    // @Produces定义了资源类方法会生成的媒体类型。
    // @Produces("text/plain;charset=UTF-8")
    @Produces(MediaType.TEXT_XML)
    // @PathParam向@Path定义的表达式注入URI参数值。
    public String getName(@PathParam("name") String name) {
        String name1 = name;
        return "<User>" + "<Name>" + name1 + "</Name>" + "</User>";
    }

    //查询
    @POST
    @Path("/{tableName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)   //只接受json格式的数据
    public String query(@PathParam("tableName") String tableName,
                        @Context HttpServletRequest request,String queryString) {
        String result="";
        JSONObject queryJson=JSON.parseObject(queryString);
        LOGGER.info("处理POST请求开始...");
        String queryType="";
        JSONObject reportService=new JSONObject();
        //判断查询哪个条件
        switch (tableName.toUpperCase()){
            case RestURIConstant.ALARM_LIST:
                queryJson.put("query_id", QueryIdConstant.ALARM_LIST_QUERY_ID);
                queryType="phoenix";
                break;

            case RestURIConstant.BLACK_LIST:
                queryJson.put("query_id", QueryIdConstant.BLACK_LIST_QUERY_ID);
                queryType="phoenix";
                break;

            case RestURIConstant.PICTURE:
                queryJson.put("query_id", QueryIdConstant.PICTURE_STATIC_SEARCH_ID);
                queryType="phoenix";
                break;

            case RestURIConstant.RELATION_LIST:
                queryJson.put("query_id", QueryIdConstant.RELATION_LIST_QUERY_ID);
                queryType="phoenix";
                break;

            case RestURIConstant.RESIDENCE_TIME:
                //添加hbase查询的id
                reportService=queryJson.getJSONObject("reportService");
                reportService.put("id",QueryIdConstant.RESIDENCE_TIME_ID);
                queryJson.put("reportService",reportService);
                queryType="hbase";
                break;

            case RestURIConstant.HISTORY_RELATION:
                //添加hbase查询的id
                reportService=queryJson.getJSONObject("reportService");
                reportService.put("id",QueryIdConstant.HISTORY_RELATION_ID);
                queryJson.put("reportService",reportService);
                queryType="hbase";
                break;

            case RestURIConstant.STRANGER_FLOW:
            case RestURIConstant.VISITOR_FLOW:
                queryJson.put("id", QueryIdConstant.VISITOR_FLOW_ID);
                queryType="es";
                break;

            case RestURIConstant.ARBITRARY_CONDITION:
                queryJson.put("id", QueryIdConstant.ARBITRARY_CONDITION_ID);
                queryType="es";
                break;
            default:
                break;
        }
        switch (queryType.toUpperCase()){
            case QueryTypeConstant.PHOENIX_QUERY:
                result=phoenixSdk.query(queryJson.toJSONString());
                break;
            case QueryTypeConstant.HBASE_QUERY:
                result= hbaseSdk.query(queryJson.toJSONString());
                break;
            case QueryTypeConstant.ES_QUERY:
                result= esSdk.query(queryJson.toJSONString(),tableName);
                break;
            default:
                break;
        }
        LOGGER.info("处理POST请求结束...");
        return result;
    }

    //修改和添加
    @PUT
    @Path("/{tableName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)   //只接受json格式的数据
    public String put(@PathParam("tableName") String tableName,
                      @Context HttpServletRequest request,String queryString) {
        return phoenixSdk.addOrInsert(queryString,tableName);
    }

    //删除
    @DELETE
    @Path("/{tableName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)   //只接受json格式的数据
    public String delete(@PathParam("tableName") String tableName,
                         @Context HttpServletRequest request,String queryString) {
        return phoenixSdk.delete(queryString,tableName);
    }
}
