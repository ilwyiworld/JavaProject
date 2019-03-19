package com.znv.fss.es.FssHomePageCount;

/**
 * Created by User on 2018/3/16.
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.znv.fss.es.BaseEsSearch;
import com.znv.fss.es.FssArbitrarySearch.FsArSeRequest;
import com.znv.fss.common.errorcode.FssErrorCodeEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Created by User on 2017/11/20.
 */
public class FssPersonListCount extends BaseEsSearch {
    protected static final Logger LOGGER = LogManager.getLogger(FssPersonListCount.class);
    private String esurl;
    private String templateName;

    public FssPersonListCount(String esurl, String tempalteName) {
        this.esurl = esurl;
        this.templateName = tempalteName;
    }

    public JSONObject initConnectParams() {
        JSONObject httpCon = super.httpConnection.esHttpConnect(esurl);
        return httpCon;

    }


    public JSONObject requestSearch(String params){
        LOGGER.debug(params);
        JSONObject outputResult = new JSONObject();
        JSONObject jsonEsResult1 = subRequestSearch( params);
        //System.out.println(JSON.toJSONString(jsonEsResult1,true));

        String total = jsonEsResult1.getJSONObject("hits").getString("total");
        double took = jsonEsResult1.getDouble("took");

        JSONArray aggByTypeBuckets = new JSONArray();
        JSONObject aggs = jsonEsResult1.getJSONObject("aggregations");
        JSONArray agg_by_type = aggs.getJSONObject("agg_by_type").getJSONArray("buckets");
        for (int i =0; i < agg_by_type.size(); i++){
            JSONObject in = agg_by_type.getJSONObject(i);
            JSONObject out = new JSONObject();
            out.put("key",in.getString("key"));
            out.put("doc_count",in.getIntValue("doc_count"));
            out.put("lib_count",in.getJSONObject("lib_count").getIntValue("value"));
            out.put("daily_add",in.getJSONObject("daily_add").getJSONObject("buckets").getJSONObject("query").getIntValue("doc_count"));
            out.put("control_count",in.getJSONObject("control_count").getIntValue("doc_count"));
            aggByTypeBuckets.add(out);

        }

        outputResult.put("took", took);
        outputResult.put("total", total);
        outputResult.put("agg_by_type_buckets",aggByTypeBuckets);
        outputResult.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        LOGGER.debug(outputResult.toJSONString());
        return  outputResult;

    }


    public JSONObject subRequestSearch(String params){

        String inParam = JSON.parseObject(params).getString("params");
        JSONObject jsonEsResult = initConnectParams();
        if (jsonEsResult == null ) {
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

            }
        }
        return jsonEsResult;
    }

}
