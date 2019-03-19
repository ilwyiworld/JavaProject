package com.znv.fss.es.MultiIndexFastSearch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.znv.fss.common.errorcode.FssErrorCodeEnum;
import com.znv.fss.common.utils.FeatureCompUtil;
import com.znv.fss.es.BaseEsSearch;
import com.znv.fss.es.EsConfig;
import com.znv.fss.es.EsManager;
import com.znv.fss.es.FastFeatureSearch.FastFeatureJsonIn;
import com.znv.fss.es.FastFeatureSearch.FastFeatureJsonOut;
import com.znv.fss.es.FastFeatureSearch.FastFeatureQueryHit;
import com.znv.fss.es.FastFeatureSearch.FastFeatureQueryParam;
import com.znv.fss.es.FastFeatureSearch.FastFeatureSearch;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.znv.fss.es.FormatObject.formatTime;
import static org.elasticsearch.lopq.LOPQModel.predictCoarseOrder;


/**
 * Created by Administrator on 2017/12/5.
 */
public class MultiIndexFastSearch extends BaseEsSearch {
    protected static final Logger LOGGER = LogManager.getLogger(MultiIndexFastSearch.class);
    private String esurl;
    private String templateName;

    public MultiIndexFastSearch(String esurl, String tempalteName) {
        this.esurl = esurl;
        this.templateName = tempalteName;
    }

    public JSONObject initConnectParams() {
        JSONObject httpCon = super.httpConnection.esHttpConnect(esurl);
        return httpCon;
    }
    public JSONObject initConnectParams(String esurl) {
        JSONObject httpCon = super.httpConnection.esHttpConnect(esurl);
        return httpCon;
    }
    // 重新封装返回结果
    @Override
    public JSONObject requestSearch(String params) {
        FeatureCompUtil fc = new FeatureCompUtil();
        FastSearchJsonIn inputParam = JSON.parseObject(params, FastSearchJsonIn.class);
        FastSearchQueryParam queryParams = inputParam.getParams();
        int errCode = paramCheck(queryParams);
        if (errCode != FssErrorCodeEnum.SUCCESS.getCode()) {
            return getErrorResult(errCode);
        }
        //重新封装查询参数
        JSONObject obj = getTemplateParams(queryParams);
        //获取跨类搜索的跨类个数，默认是1
        int coarseCentersNum = 1;
        if(queryParams.getCoarseCodeNum() != 0){
            coarseCentersNum =queryParams.getCoarseCodeNum();
        }
        //计算粗分类的标签coarse_id
        int[][] coarseCodeOrder;
        String indexNamePrepix = EsManager.fssEsIndexHistoryPrefix;
        String indexName = "";
        List<String> featureValue = queryParams.getFeatureValue();
        for(int i=0;i<featureValue.size();i++) {
            try {
                coarseCodeOrder = predictCoarseOrder(fc.getFloatArray(new org.apache.commons.codec.binary.Base64().decode(featureValue.get(i))), coarseCentersNum);
            } catch (Exception e) {
                LOGGER.info("Get Coarse Code Error: " + e);
                return getErrorResult(FssErrorCodeEnum.ES_GET_COARSE_CODE_ERROR.getCode());
            }
            if(i < featureValue.size()-1){
                for (int j = 0; j < coarseCodeOrder.length; j++) {
                    indexName += indexNamePrepix +"-"+ coarseCodeOrder[j][0] + ",";
                }
            }else{
                for (int j = 0; j < coarseCodeOrder.length-1; j++) {
                    indexName += indexNamePrepix +"-"+ coarseCodeOrder[j][0] + ",";
                }
                indexName += indexNamePrepix + "-"+coarseCodeOrder[coarseCodeOrder.length - 1][0];
            }
        }
        String esurl = EsManager.concatenateURL(indexName,EsManager.fssEsIndexHistoryType);
        // HTTP连接
        JSONObject httpConResult = initConnectParams(esurl);
        if (httpConResult != null) {
            return httpConResult;
        }

        StringBuffer sb = super.getSearchResult(obj);
        // [lq-add]
        if (sb.toString().equals(new StringBuffer(FssErrorCodeEnum.SENSETIME_FEATURE_POINTS_ERROR.getCode()).toString())) {
            return getErrorResult(FssErrorCodeEnum.SENSETIME_FEATURE_POINTS_ERROR.getCode());
        }
        if (sb.toString().equals(new StringBuffer(FssErrorCodeEnum.ES_GET_EXCEPTION.getCode()).toString())) {
            return getErrorResult(FssErrorCodeEnum.ES_GET_EXCEPTION.getCode());
        }
        String esResults = sb.toString();
        JSONObject jsonEsResult = JSONObject.parseObject(esResults);
        if (jsonEsResult.get("error") != null) {
            LOGGER.info("Query es error!! params:" + obj.toJSONString() + ".\terror:" + jsonEsResult.toJSONString());
            return getErrorResult(FssErrorCodeEnum.ES_GET_EXCEPTION.getCode());
        }
        if (jsonEsResult.getBoolean("timed_out")) {
            return getErrorResult(FssErrorCodeEnum.ES_TIMEOUT_EXCEPTION.getCode());
        }
        //System.out.println("读取es查询结果如下：");
        //System.out.println(JSON.toJSONString(jsonEsResult));
        // 从es查询结果中获取hits
        JSONArray esHits = jsonEsResult.getJSONObject("hits").getJSONArray("hits");
        List<FastFeatureQueryHit> outHits = new ArrayList<>(esHits.size());
        fc.setFeaturePoints(EsConfig.getFeaturePoints());
        for (int i = 0; i < esHits.size(); i++) {
            FastFeatureQueryHit outHit = new FastFeatureQueryHit();
            JSONObject hit = esHits.getJSONObject(i);
            float score = hit.getFloatValue("_score");
            outHit.setScore(fc.Normalize(score)); // 归一化
            JSONObject source = hit.getJSONObject("_source"); // 从hits数组中获取_source
            outHit.setBigPictureUuid(source.getString("big_picture_uuid"));
            outHit.setImgUrl(source.getString("img_url"));
            outHit.setCameraId(source.getString("camera_id"));
            outHit.setCameraName(source.getString("camera_name"));
            outHit.setOfficeId(source.getString("office_id"));
            outHit.setOfficeName(source.getString("office_name"));
            outHit.setEnterTime(formatTime(source.getString("enter_time")));
            outHit.setLeaveTime(formatTime(source.getString("leave_time")));
            outHit.setOpTime(formatTime(source.getString("op_time")));
            outHit.setLibId(source.getInteger("lib_id"));
            outHit.setPersonId(source.getString("person_id"));
            outHit.setIsAlarm(source.getString("is_alarm"));
            outHit.setSimilarity(source.getFloatValue("similarity"));
            if (source.containsKey("img_width")){
                outHit.setImgWidth(source.getIntValue("img_width"));
            }
            if (source.containsKey("img_height")){
                outHit.setImgWidth(source.getIntValue("img_height"));
            }
            if (source.containsKey("left_pos")){
                outHit.setLeftPos(source.getIntValue("left_pos"));
            }
            if (source.containsKey("top")){
                outHit.setTop(source.getIntValue("top"));
            }

            outHits.add(outHit);
        }

        FastFeatureJsonOut outputResult = new FastFeatureJsonOut();
        int total = jsonEsResult.getJSONObject("hits").getInteger("total");
        int took = jsonEsResult.getInteger("took");
        outputResult.setErrorcode(FssErrorCodeEnum.SUCCESS.getCode());
        outputResult.setTotal(total);
        outputResult.setHits(outHits);
        outputResult.setTook(took);

        return (JSONObject) JSONObject.toJSON(outputResult);

    }

    private int paramCheck(FastSearchQueryParam inParam) {
        if (StringUtils.isEmpty(inParam.getEnterTimeStart())) {
            LOGGER.info("StartTime can't be empty ！");
            return FssErrorCodeEnum.ES_INVALID_PARAM.getCode();
        }
        if (StringUtils.isEmpty(inParam.getEnterTimeEnd())) {
            LOGGER.info("EndTime can't be empty ！");
            return FssErrorCodeEnum.ES_INVALID_PARAM.getCode();
        }
        if (inParam.getEnterTimeStart().compareTo(inParam.getEnterTimeEnd()) > 0) {
            LOGGER.info("EndTime can't larger than startTime ！");
            return FssErrorCodeEnum.ES_INVALID_PARAM.getCode();
        }
        if (inParam.getFeatureValue().size()==0) {
            LOGGER.info("FeatureValue can't be empty ！");
            return FssErrorCodeEnum.ES_INVALID_PARAM.getCode();
        }
        if (inParam.getSimThreshold() < 0.5f) {
            LOGGER.info("SimThreshold is too small ！");
            return FssErrorCodeEnum.ES_INVALID_PARAM.getCode();
        }
        if (inParam.getFrom() < 0) {
            LOGGER.info("From is too small ！");
            return FssErrorCodeEnum.ES_INVALID_PARAM.getCode();
        }
        if (inParam.getSize() < 1) {
            LOGGER.info("Size is too small ！");
            return FssErrorCodeEnum.ES_INVALID_PARAM.getCode();
        }
        if ((inParam.getFrom() + inParam.getSize()) >= 10000) {
            LOGGER.info("Request result set out of range ！");
            return FssErrorCodeEnum.ES_SIZE_OUT_OF_RANGE.getCode();
        }
        return FssErrorCodeEnum.SUCCESS.getCode();
    }

//    private JSONObject getErrorResult(int errCode) {
//        JSONObject result = new JSONObject();
//        result.put("errorCode", errCode);
//        result.put("total", 0);
//        return result;
//    }

    private JSONObject getTemplateParams(FastSearchQueryParam inParam) {
        JSONObject paramsT = new JSONObject();
        String excludes[] = {"rt_feature"};
        paramsT.put("enter_time_start", inParam.getEnterTimeStart());
        paramsT.put("enter_time_end", inParam.getEnterTimeEnd());
        paramsT.put("feature_name", "rt_feature.feature_high");

        FeatureCompUtil fc = new FeatureCompUtil();
        fc.setFeaturePoints(EsConfig.getFeaturePoints());
        float sim = (float) inParam.getSimThreshold();
        paramsT.put("sim_threshold", fc.reversalNormalize(sim));//脚本中未归一化

        paramsT.put("feature_value", inParam.getFeatureValue());
        paramsT.put("filter_type", inParam.getFilterType());
       //paramsT.put("is_lopq", true/*inParam.getIsLopq()*/);
        paramsT.put("from", inParam.getFrom());
        paramsT.put("size", inParam.getSize());
        paramsT.put("sortField", inParam.getSortField());
        paramsT.put("sortOrder", inParam.getSortOrder());
        paramsT.put("is_excludes",true);
        paramsT.put("excludes",excludes);
      //  int minShouldMatch = 0;
        if (inParam.getOfficeId() != null && !inParam.getOfficeId().isEmpty()) {
            paramsT.put("office_id", inParam.getOfficeId());
            paramsT.put("is_office",true);
           // minShouldMatch = 1;
        }
        if (inParam.getCameraId() != null && !inParam.getCameraId().isEmpty()) {
            paramsT.put("camera_id", inParam.getCameraId());
            paramsT.put("is_camera",true);
           // minShouldMatch = 1;
        }
     /*   if (minShouldMatch != 0) {
            paramsT.put("minimum_should_match", minShouldMatch);
        }*/

        JSONObject obj = new JSONObject();
        obj.put("id", templateName);
        obj.put("params", paramsT);

        return obj;
    }

}
