package com.znv.sdk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.znv.constant.RestURIConstant;
import com.znv.request.Post;
import com.znv.servlet.initConnectionServlet;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Properties;

/**
 * Created by Administrator on 2017/7/12.
 */
public class esSdk {
    private static final Logger LOGGER = LogManager.getLogger(hbaseSdk.class.getName());
    private static final Properties props = initConnectionServlet.getProp();
    private static String resultErrorStr="{'errorCode':'-1'}";

    /**
     * @param data 查询协议
     * @return json格式的数据
     */
    public static String query(String data,String tableName){
        JSONObject objData = JSON.parseObject(data);
        String queryJson=objData.getString("queryJson");
        String result="";
        String templateName="";
        switch (tableName.toUpperCase()){
            //任意条件搜索
            case RestURIConstant.ARBITRARY_CONDITION:
                templateName=props.getProperty("fss.es.search.template.facesearch.url");
                break;
            //人流量和陌生人人流量
            case RestURIConstant.VISITOR_FLOW:
            case RestURIConstant.STRANGER_FLOW:
                templateName=props.getProperty("fss.es.search.template.flowcount.url");
                break;
        }
        String url = "http://".concat(props.getProperty("es.server")).concat(":").concat(props.getProperty("es.port"))
                .concat(templateName);
        try{
            result = Post.sendPostData(url, queryJson);
        }catch(Exception e){
            result = resultErrorStr;
            LOGGER.error("查询es结果异常:" + e);
        }
        return result;
    }
}
