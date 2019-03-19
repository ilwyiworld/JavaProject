package com.znv.sdk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.znv.fss.conf.ConfigManager;
import com.znv.fss.phoenix.PhoenixClient;
import com.znv.servlet.initConnectionServlet;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Created by Administrator on 2017/7/12.
 */
public class phoenixSdk {
    private static final Logger LOGGER = LogManager.getLogger(phoenixSdk.class.getName());
    private static String resultErrorStr="{'errorCode':'-1'}";
    private static PhoenixClient phoenixClient=initConnectionServlet.getPhoenixClient();

    /**
     * @param data 查询协议
     * @return json格式的数据
     */
    public static String query(String data){
        JSONObject queryJson = JSON.parseObject(data);
        String result="";
        try{
            result=phoenixClient.query(queryJson).toJSONString();
        }catch(Exception e){
            result = resultErrorStr;
            LOGGER.error("查询phoenix异常:" + e);
        }
        return result;
    }

    /**
     * @param data 新增或修改协议
     * @param tableName 表名
     * @return json格式的数据
     */
    public static String addOrInsert(String data,String tableName){
        JSONObject queryJson = JSON.parseObject(data);
        String result="";
        try{
            result=phoenixClient.insert(queryJson, ConfigManager.getString(tableName)).toJSONString();
        }catch(Exception e){
            result = resultErrorStr;
            LOGGER.error("phoenix新增异常:" + e);
        }
        return result;
    }

    /**
     * @param data 删除协议
     * @param tableName 表名
     * @return json格式的数据
     */
    public static String delete(String data,String tableName){
        JSONObject queryJson = JSON.parseObject(data);
        String result="";
        try{
            result=phoenixClient.delete(queryJson, ConfigManager.getString(tableName)).toJSONString();
        }catch(Exception e){
            result = resultErrorStr;
            LOGGER.error("phoenix新增异常:" + e);
        }
        return result;
    }
}
