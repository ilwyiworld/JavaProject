package com.znv.fss.es.LogToEs;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.znv.fss.es.BaseEsSearch;
import com.znv.fss.es.EsManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by User on 2018/5/24.
 */
public class WriteLogToEsBulk extends BaseEsSearch {
    private static String esurl;

    public WriteLogToEsBulk(String esurl) {
        this.esurl = esurl;
    }

    public JSONObject initConnectParams() {
        JSONObject httpCon = super.httpConnection.esHttpConnect(esurl);
        return httpCon;
    }

    //API的方式写log到es
//    public static void writeLogToEs(JSONObject object) {
//        TransportClient esTClient = ESUtils.getESTransportClient(clusterName, transportHosts); // 连接ES
//        BulkRequestBuilder bulkRequest = esTClient.prepareBulk();
//        Map<String, Object> ret = createMapData(object);
//        IndexRequestBuilder indexerbuilder = esTClient.prepareIndex(indexName, type);
//        bulkRequest.add(indexerbuilder.setSource(ret));
//        bulkRequest.execute().actionGet();
//    }
//
//    public static Map createMapData(JSONObject object) {
//        Map<String, Object> ret = new HashMap<String, Object>();
//        ret.put("opt_time", object.get("op_time"));
//        ret.put("user_name", object.get("user_name"));
//        ret.put("opt_type", object.get("opt_type"));
//        ret.put("opt_describe", object.get("opt_describe"));
//        return ret;
//    }


//    //http请求的方式写log到es   -->单条写
//    public JSONObject requestSearch(String params) {
//        String result = "";
//        //HTTP连接
//        JSONObject jsonEsResult = initConnectParams(); //es连接
//        if (jsonEsResult == null) {
//            StringBuffer stringBuffer = super.getSearchResult(JSONObject.parseObject(params).getJSONObject("params"));
//            result = stringBuffer.toString();
//        }
//        return JSONObject.parseObject(result);
//    }

    //http请求的方式写log到es   -->批量写
    public JSONObject requestSearch(String params) {
        //System.out.println("params：" + params);
        List<JSONObject> list = new ArrayList<>();
        //单条
//        list.add(JSONObject.parseObject(params).getJSONObject("params"));

        //批量
        JSONArray jsonArray = JSONObject.parseObject(params).getJSONArray("params");
        for (int i = 0; i < jsonArray.size(); i++) {
            list.add((JSONObject) jsonArray.get(i));
        }

        String str = EsManager.toBatchJSon(list);
        String result = "";
        //HTTP连接
        JSONObject jsonEsResult = initConnectParams(); //es连接
        if (jsonEsResult == null) {
            StringBuffer stringBuffer = super.getSearchResultString(str);
            result = stringBuffer.toString();
        }
        return JSONObject.parseObject(result);
    }
}
