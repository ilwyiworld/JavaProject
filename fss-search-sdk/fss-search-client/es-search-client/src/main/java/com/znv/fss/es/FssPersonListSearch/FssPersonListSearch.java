package com.znv.fss.es.FssPersonListSearch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.znv.fss.es.BaseEsSearch;
import com.znv.fss.es.EsConfig;
import com.znv.fss.es.EsManager;
import com.znv.fss.es.FssArbitrarySearch.FsArSeRequest;
import com.znv.fss.common.errorcode.FssErrorCodeEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.znv.fss.common.utils.FeatureCompUtil;

import static com.znv.fss.es.FormatObject.formatTime;

/**
 * Created by User on 2017/11/20.
 */
public class FssPersonListSearch extends BaseEsSearch {
    protected static final Logger LOGGER = LogManager.getLogger(FssPersonListSearch.class);
    private String esurl;
    private String templateName;

    public FssPersonListSearch(String esurl, String tempalteName) {
        this.esurl = esurl;
        this.templateName = tempalteName;
    }

    public JSONObject initConnectParams() {
        JSONObject httpCon = super.httpConnection.esHttpConnect(esurl);
        return httpCon;

    }
/*
    public JSONObject paramCheck(String inParam) {
        JSONObject params = JSON.parseObject(inParam);
        JSONObject checkResult = null;
        if (params.containsKey("from") && params.containsKey("size")) {
            int from = params.getIntValue("from");
            int size = params.getIntValue("size");
            //如果返回结果中大于10000条，只能看到10000条结果，点击10000条以后的数据，会报错误码130006
            if ((from + size) >= 10000) {
                checkResult = new JSONObject();
                checkResult.put("errorCode", FssErrorCodeEnum.ES_SIZE_OUT_OF_RANGE.getCode());
            }
        }
        return checkResult;
    }*/

    public JSONObject paramCheck(JSONObject params) {
        JSONObject checkResult = null;
        if (params.containsKey("from") && params.containsKey("size")) {
            int from = params.getIntValue("from");
            int size = params.getIntValue("size");
            //如果返回结果中大于10000条，只能看到10000条结果，点击10000条以后的数据，会报错误码130006
            if ((from + size) >= 10000) {
                checkResult = new JSONObject();
                checkResult.put("errorCode", FssErrorCodeEnum.ES_SIZE_OUT_OF_RANGE.getCode());
            }
        }
        return checkResult;
    }

    public JSONObject requestSearch(String params) {
        long timeStart = System.currentTimeMillis();
        JSONObject aggregation = null;
        String inParam = JSON.parseObject(params).getString("params");

        //添加is_lib字段
        JSONObject queryParam = JSON.parseObject(inParam);
        JSONObject checkResult = paramCheck(queryParam);
        if(queryParam.getString("lib_id") != null  && !queryParam.getString("lib_id").isEmpty()){
            queryParam.put("is_lib",true);
        }
        inParam = queryParam.toString();

        //HTTP连接
        JSONObject jsonEsResult = initConnectParams();
        FeatureCompUtil fc = new FeatureCompUtil();
        fc.setFeaturePoints(EsConfig.getFeaturePoints());
        if (jsonEsResult == null && checkResult == null) {
            //paramCheck(inParam);
            String idParam = "{'id':'" + templateName + "'}";
            JSONObject obj = FsArSeRequest.manualInput(idParam, inParam);
            StringBuffer sb = super.getSearchResult(obj);
            // [lq-add]
            if (sb.toString().equals(new StringBuffer(FssErrorCodeEnum.SENSETIME_FEATURE_POINTS_ERROR.getCode()).toString())) {
                return getErrorResult(FssErrorCodeEnum.SENSETIME_FEATURE_POINTS_ERROR.getCode());
            }
            if (sb.toString().equals(new StringBuffer(FssErrorCodeEnum.ES_GET_EXCEPTION.getCode()).toString())) {
                return getErrorResult(FssErrorCodeEnum.ES_GET_EXCEPTION.getCode());
            } else {
                String esResults = sb.toString();
                jsonEsResult = JSONObject.parseObject(esResults);
                if (jsonEsResult.containsKey("aggregations")) {
                    aggregation = jsonEsResult.getJSONObject("aggregations");
                }
                //从es查询结果中获取hits
                JSONArray esHits = jsonEsResult.getJSONObject("hits").getJSONArray("hits");
                JSONArray hits = new JSONArray();
                JSONObject outputResult = new JSONObject();
                if (null != esHits && esHits.size() != 0) {
                    for (int i = 0; i < esHits.size(); i++) {
                       // String score = esHits.getJSONObject(i).getString("_score");
                        float score = esHits.getJSONObject(i).getFloatValue("_score");
                        JSONObject source = esHits.getJSONObject(i).getJSONObject("_source"); //从hits数组中获取_source
                        // String uuid = source.getString("uuid");
                        if (source.containsKey("control_end_time")) {
                            String controlEndTime = source.getString("control_end_time");
                            controlEndTime = formatTime(controlEndTime);
                            source.put("control_end_time", controlEndTime);
                        }
                        if (source.containsKey("control_start_time")) {
                            String controlStartTime = source.getString("control_start_time");
                            controlStartTime = formatTime(controlStartTime);
                            source.put("control_start_time", controlStartTime);
                        }
                        if (source.containsKey("create_time")) {
                            String createTime = source.getString("create_time");
                            createTime = formatTime(createTime);
                            source.put("create_time", createTime);
                        }
                        if (source.containsKey("modify_time")) {
                            String modifyTime = source.getString("modify_time");
                            modifyTime = formatTime(modifyTime);
                            source.put("modify_time", modifyTime);
                        }
                       // source.put("score", score);
                        source.put("score", fc.Normalize(score));
                        hits.add(i, source);
                    }
                }
                String total = jsonEsResult.getJSONObject("hits").getString("total");
                outputResult.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
                outputResult.put("hits", hits);
                if (aggregation != null) {
                    outputResult.put("aggregations", aggregation);
                }
                outputResult.put("total", total);
                long timeCost = System.currentTimeMillis() - timeStart;
                outputResult.put("time", String.valueOf(timeCost));
                return outputResult;
            }
        } else if (checkResult != null) {
            return checkResult;
        }
        return jsonEsResult;
    }

}
