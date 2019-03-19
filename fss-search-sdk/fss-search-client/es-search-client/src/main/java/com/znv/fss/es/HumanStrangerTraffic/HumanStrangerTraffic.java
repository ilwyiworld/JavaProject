package com.znv.fss.es.HumanStrangerTraffic;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.znv.fss.es.BaseEsSearch;
import com.znv.fss.es.EsManager;
import com.znv.fss.common.errorcode.FssErrorCodeEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by User on 2017/8/3.
 */

public class HumanStrangerTraffic extends BaseEsSearch {
    protected static final Logger LOGGER = LogManager.getLogger(HumanStrangerTraffic.class);
    private String esurl;
    private String templateName;

    public HumanStrangerTraffic(String esurl, String tempalteName) {
        this.esurl = esurl;
        this.templateName = tempalteName;
    }

    public JSONObject initConnectParams() {

        JSONObject httpCon = super.httpConnection.esHttpConnect(esurl);
        return httpCon;

    }

    public JSONObject requestSearch(String params) {
        long timeStart = System.currentTimeMillis();
        String inParam = JSON.parseObject(params).getString("params");
        //添加is_lib字段
        JSONObject queryParam = JSON.parseObject(inParam);
        if(queryParam.getString("camera_id") != null  && !queryParam.getString("camera_id").isEmpty()){
            queryParam.put("is_camera",true);
        }
        if(queryParam.getString("office_id") != null  && !queryParam.getString("office_id").isEmpty()){
            queryParam.put("is_office",true);
        }
        inParam = queryParam.toString();
        initConnectParams();
        JSONObject jsonEsResult = initConnectParams();
        if (jsonEsResult == null) {
            String idParam = "{'id':'" + templateName + "'}";
            JSONObject obj = HuStTrRequest.manualInput(idParam, inParam);
            StringBuffer sb = super.getSearchResult(obj);
            // [lq-add]
            if (sb.toString().equals(new StringBuffer(FssErrorCodeEnum.SENSETIME_FEATURE_POINTS_ERROR.getCode()).toString())) {
                return getErrorResult(FssErrorCodeEnum.SENSETIME_FEATURE_POINTS_ERROR.getCode());
            }
            if (sb.toString().equals(new StringBuffer(FssErrorCodeEnum.ES_GET_EXCEPTION.getCode()).toString())) {
                return getErrorResult(FssErrorCodeEnum.ES_GET_EXCEPTION.getCode());
            } else {
                JSONObject esSearchResult = JSONObject.parseObject(sb.toString());
                // 从es查询结果中获取aggregations
                JSONObject aggregations = esSearchResult.getJSONObject("aggregations");
                JSONObject searchResult = new JSONObject();
                String total = esSearchResult.getJSONObject("hits").getString("total");
                searchResult.put("aggregations", aggregations);
                searchResult.put("total", total);
                searchResult.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
                long timeCost = System.currentTimeMillis() - timeStart;
                searchResult.put("time", String.valueOf(timeCost));
                return searchResult;
            }
        }
        return jsonEsResult;
    }
}
