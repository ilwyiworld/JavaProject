package com.znv.fss.es.FssHomePageCount;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.znv.fss.es.BaseEsSearch;
import com.znv.fss.es.FssArbitrarySearch.FsArSeRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.znv.fss.common.errorcode.FssErrorCodeEnum;
import static com.znv.fss.es.FormatObject.timeCompare;

/**
 * Created by User on 2018/3/16.
 */
public class FssHistoryPersonCount extends BaseEsSearch{

        protected static final Logger LOGGER = LogManager.getLogger(FssHistoryPersonCount.class);
        private String esurl;
        private String templateName;

        public FssHistoryPersonCount(String esurl, String tempalteName) {
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
           //System.out.println(JSON.toJSONString(jsonEsResult1,true));
            //今日人流量
            JSONObject param1 = (JSONObject) JSON.parseObject(params).clone();
            JSONObject jsonEsResult1 = subRequestSearch(param1.toString());


            //System.out.println(JSON.toJSONString(jsonEsResult1,true));
            int took = jsonEsResult1.getIntValue("took");
            JSONObject aggs = jsonEsResult1.getJSONObject("aggregations");
            String personTotal = aggs.getJSONObject("agg_total").getString("doc_count");
            String strangerTotal = jsonEsResult1.getJSONObject("hits").getString("total");
            String personDailyAdd = aggs.getJSONObject("person_daily_add").getString("doc_count");
            String strangerDailyAdd = aggs.getJSONObject("person_daily_add").getJSONObject("stranger_daily_add").getJSONObject("buckets").getJSONObject("filter").getString("doc_count");
            JSONArray personAggByOfficeBuckets = aggs.getJSONObject("agg_by_time").getJSONObject("person_agg_by_office").getJSONArray("buckets");
            JSONArray strangerAggByOfficeBuckets = aggs.getJSONObject("agg_by_time").getJSONObject("stranger").getJSONObject("stranger_agg_by_office").getJSONArray("buckets");



            outputResult.put("took", took);
            outputResult.put("person_total", personTotal);//抓拍人员总数
            outputResult.put("stranger_total", strangerTotal);//陌生人总数
            outputResult.put("person_daily_add", personDailyAdd);//规定时间段内抓拍人员新增数
            outputResult.put("stranger_daily_add", strangerDailyAdd);//规定时间段内陌生人新增数
            outputResult.put("person_agg_by_office_buckets",personAggByOfficeBuckets);//综合分析时间段内人流量按区域聚合
            outputResult.put("stranger_agg_by_office_buckets",strangerAggByOfficeBuckets);//综合分析时间段内陌生人统计按区域聚合

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

