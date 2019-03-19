package com.znv.fss.es.AlarmTypeSearch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.znv.fss.common.errorcode.FssErrorCodeEnum;
import com.znv.fss.common.utils.FeatureCompUtil;
import com.znv.fss.es.BaseEsSearch;
import com.znv.fss.es.EsConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.util.ArrayList;
import java.util.List;

import static com.znv.fss.es.FormatObject.formatTime;


public class AlarmTypeSearch extends BaseEsSearch {
    protected static final Logger LOGGER = LogManager.getLogger(AlarmTypeSearch.class);
    private String esurl;
    private String templateName;

    public AlarmTypeSearch(String esurl, String tempalteName) {
        this.esurl = esurl;
        this.templateName = tempalteName;
    }

    public JSONObject initConnectParams() {
        JSONObject httpCon = super.httpConnection.esHttpConnect(esurl);
        return httpCon;
    }

    //参数检查
    private int paramCheck(AlarmTypeQueryParam inParam) {
        if (StringUtils.isEmpty(inParam.getEnterTimeStart())) {
            LOGGER.info("StartTime can't be empty ！");
            return FssErrorCodeEnum.ES_INVALID_PARAM.getCode();
        }
        if (StringUtils.isEmpty(inParam.getEnterTimeEnd())) {
            LOGGER.info("EndTime can't be empty ！");
            return FssErrorCodeEnum.ES_INVALID_PARAM.getCode();
        }
        if (inParam.getEnterTimeStart().compareTo(inParam.getEnterTimeEnd()) > 0) {
            LOGGER.error("EndTime can't larger than startTime ！");
            return FssErrorCodeEnum.ES_INVALID_PARAM.getCode();
        }
        if (inParam.getCalcSim()) {
            if (inParam.getFeatureValue().isEmpty()) {
                LOGGER.info("FeatureValue can't be empty ！");
                return FssErrorCodeEnum.ES_INVALID_PARAM.getCode();
            }
            if (inParam.getSimThreshold() < 0.5f) {
                LOGGER.info("SimThreshold is too small ！");
                return FssErrorCodeEnum.ES_INVALID_PARAM.getCode();
            }
        }
//        if (inParam.getAlarmType() < 0) {
//            LOGGER.info("Alarm type cannot less than 0, current alarmType is : " + inParam.getAlarmType());
//            return FssErrorCodeEnum.ES_INVALID_PARAM.getCode();
//        }
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

        //TODO alarmtype
        return FssErrorCodeEnum.SUCCESS.getCode();
    }

    @Override
    public JSONObject requestSearch(String inparams) {
        AlarmTypeJsonIn inputParams = JSON.parseObject(inparams, AlarmTypeJsonIn.class);
        AlarmTypeQueryParam queryParams = inputParams.getParams();
        //检查参数
        int errCode = paramCheck(queryParams);
        if (errCode != FssErrorCodeEnum.SUCCESS.getCode()) {
            return getErrorResult(errCode);
        }

        JSONObject httpConResult = initConnectParams();
        if (httpConResult != null) {
            return httpConResult;
        }

        JSONObject templateParams = getTemplateParams(queryParams);
        StringBuffer sb = super.getSearchResult(templateParams);
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
            LOGGER.info("Query es error!! params:" + templateParams.toJSONString() + ".\terror:" + jsonEsResult.toJSONString());
            return getErrorResult(FssErrorCodeEnum.ES_GET_EXCEPTION.getCode());
        }
        if (jsonEsResult.getBoolean("timed_out")) {
            return getErrorResult(FssErrorCodeEnum.ES_TIMEOUT_EXCEPTION.getCode());
        }

        // 从es查询结果中获取hits
        JSONArray esHits = jsonEsResult.getJSONObject("hits").getJSONArray("hits");

        List<AlarmTypeQueryHit> outHits = setHitParams(esHits);

        AlarmTypeJsonOut outputResult = new AlarmTypeJsonOut();
        int total = jsonEsResult.getJSONObject("hits").getInteger("total");
        int took = jsonEsResult.getInteger("took");
        outputResult.setErrorcode(FssErrorCodeEnum.SUCCESS.getCode());
        outputResult.setTotal(total);
        outputResult.setHits(outHits);
        outputResult.setTook(took);

        return (JSONObject) JSONObject.toJSON(outputResult);
    }

//    private JSONObject getErrorResult(int errCode) {
//        JSONObject result = new JSONObject();
//        result.put("errorCode", errCode);
//        result.put("total", 0);
//        return result;
//    }

    //设置查询语句的参数
    private JSONObject getTemplateParams(AlarmTypeQueryParam inParam) {
        JSONObject paramsT = new JSONObject();
        paramsT.put("enter_time_start", inParam.getEnterTimeStart());
        paramsT.put("enter_time_end", inParam.getEnterTimeEnd());

        paramsT.put("feature_name", "rt_feature.feature_high");

        FeatureCompUtil fc = new FeatureCompUtil();
        fc.setFeaturePoints(EsConfig.getFeaturePoints());
        float sim = (float) inParam.getSimThreshold();

        paramsT.put("sim_threshold", fc.reversalNormalize(sim));//脚本中未归一化

        paramsT.put("from", inParam.getFrom());
        paramsT.put("size", inParam.getSize());
        paramsT.put("sortField", inParam.getSortField());
        paramsT.put("sortOrder", inParam.getSortOrder());
        int minShouldMatch = 0;
        if (inParam.getOfficeId() != null && !inParam.getOfficeId().isEmpty()) {
            paramsT.put("office_id", inParam.getOfficeId());
            paramsT.put("is_office",true);
            //inParam.setIsOffice(true);
            minShouldMatch = 1;
        }
        if (inParam.getCameraId() != null && !inParam.getCameraId().isEmpty()) {
            paramsT.put("camera_id", inParam.getCameraId());
            paramsT.put("is_camera",true);
           // inParam.setIsCamera(true);
            minShouldMatch = 1;
        }
        //TODO
        if (inParam.getControlEventId() != null && !inParam.getControlEventId().isEmpty()) {
            paramsT.put("control_event_id", inParam.getControlEventId());
            paramsT.put("is_control_event",true);
          //  inParam.setIsControlEvent(true);
            minShouldMatch = 1;
        }

        if (minShouldMatch != 0) {
            paramsT.put("minimum_should_match", minShouldMatch);
        }

        if (inParam.getCalcSim()) {
            paramsT.put("is_calcSim", inParam.getCalcSim());
            paramsT.put("feature_value", inParam.getFeatureValue());
        }


        JSONObject obj = new JSONObject();
        obj.put("id", templateName);
        obj.put("params", paramsT);

        //System.out.println(obj.toString());

        return obj;
    }

    private List<AlarmTypeQueryHit> setHitParams(JSONArray esHits) {
        List<AlarmTypeQueryHit> outHits = new ArrayList<>(esHits.size());
        FeatureCompUtil fc = new FeatureCompUtil();
        fc.setFeaturePoints(EsConfig.getFeaturePoints());
        for (int i = 0; i < esHits.size(); i++) {
            AlarmTypeQueryHit outHit = new AlarmTypeQueryHit();
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
            outHit.setPersonName(source.getString("person_name"));
            outHit.setPersonId(source.getString("person_id"));
            outHit.setControlEventId(source.getString("control_event_id"));
            outHit.setBirth(source.getString("birth"));
            outHit.setUuid(source.getString("uuid"));

            //TODO if the key is not exist,need to add a default value?
            if (source.containsKey("lib_id")) {
                outHit.setLibId(source.getInteger("lib_id"));
            }
            if (source.containsKey("alarm_type")) {
                outHit.setAlarmType(source.getInteger("alarm_type"));
            }
            if (source.containsKey("similarity")) {
                outHit.setSimilarity(source.getFloatValue("similarity"));
            }

            if (source.containsKey("img_width")){
                outHit.setImgWidth(source.getIntValue("img_width"));
            }
            if (source.containsKey("img_height")){
                outHit.setImgHeight(source.getIntValue("img_height"));
            }
            if (source.containsKey("left_pos")){
                outHit.setLeftPos(source.getIntValue("left_pos"));
            }
            if (source.containsKey("right_pos")){
                outHit.setRightPos(source.getIntValue("right_pos"));
            }
            if (source.containsKey("top")){
                outHit.setTop(source.getIntValue("top"));
            }
            if (source.containsKey("bottom")){
                outHit.setBottom(source.getIntValue("bottom"));
            }

            outHits.add(outHit);
        }
        return outHits;
    }
}
