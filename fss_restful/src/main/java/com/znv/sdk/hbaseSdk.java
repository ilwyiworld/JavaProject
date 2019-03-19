package com.znv.sdk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.znv.hbase.HBaseManager;
import com.znv.hbase.MultiHBaseSearch;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2017/7/12.
 */
public class hbaseSdk {

    private static MultiHBaseSearch search = null;
    private static ConcurrentHashMap<String, Object> ch = new ConcurrentHashMap<String, Object>();
    private static final Logger LOGGER = LogManager.getLogger(hbaseSdk.class.getName());
    private static String resultErrorStr="{'errorCode':'-1'}";

    /**
     * @param data 查询协议
     * @return json格式的数据
     */
    public static String query(String data){
        JSONObject objData = JSON.parseObject(data);
        String result="";
        //后台分页
        if(!StringUtils.isEmpty(objData.getString("isReload"))) {
            String isReload = objData.getString("isReload");
            if ("true".equals(isReload)) { // 是否重新查询
                search = HBaseManager.createSearch(data);
                ch.put("", search);
                try {
                    result = search.getJsonResult(data);
                } catch (Exception e) {
                    result = resultErrorStr;
                    LOGGER.error("请求hbase异常:" + e);
                }
            }else{
                search = (MultiHBaseSearch) ch.get("");
                try {
                    result = search.getJsonResult(data);
                } catch (Exception e) {
                    result = resultErrorStr;
                    LOGGER.error("请求hbase异常:" + e);
                }
            }
        }else{
            //前台分页
            search = HBaseManager.createSearch(data);
            try {
                result = search.getJsonResult(data);
            } catch (Exception e) {
                result = resultErrorStr;
                LOGGER.error("请求hbase异常:" + e);
            }
        }
        return result;
    }
}
