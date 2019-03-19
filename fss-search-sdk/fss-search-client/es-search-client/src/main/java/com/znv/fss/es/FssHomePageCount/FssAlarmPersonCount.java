package com.znv.fss.es.FssHomePageCount;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.znv.fss.es.BaseEsSearch;
import com.znv.fss.es.FssArbitrarySearch.FsArSeRequest;
import com.znv.fss.common.errorcode.FssErrorCodeEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by User on 2018/3/16.
 */
public class FssAlarmPersonCount extends BaseEsSearch {
        protected static final Logger LOGGER = LogManager.getLogger(FssAlarmPersonCount.class);
        private String esurl;
        private String templateName;

        public FssAlarmPersonCount(String esurl, String tempalteName) {
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
            // System.out.println(JSON.toJSONString(jsonEsResult1,true));

            String took = jsonEsResult1.getString("took");
            String total = jsonEsResult1.getJSONObject("hits").getString("total");
            JSONObject aggs = jsonEsResult1.getJSONObject("aggregations");

            JSONArray aggByLibBuckets = aggs.getJSONObject("agg_by_lib_id").getJSONObject("agg_by_lib_buckets").getJSONArray("buckets");
            String dailyAdd = aggs.getJSONObject("agg_by_camera_id").getString("doc_count");
            JSONArray aggByCameraBuckets = aggs.getJSONObject("agg_by_camera_id").getJSONObject("agg_by_camera_buckets").getJSONArray("buckets");

            outputResult.put("total", total);//告警数据总数
            outputResult.put("took", took);
            outputResult.put("daily_add",dailyAdd);
            outputResult.put("agg_by_lib_buckets", aggByLibBuckets);
            outputResult.put("agg_by_camera_buckets",aggByCameraBuckets);


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

