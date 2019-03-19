package com.znv.fss.es.MultiIndexExactSearch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.znv.fss.common.errorcode.FssErrorCodeEnum;
import com.znv.fss.common.utils.FeatureCompUtil;
import com.znv.fss.es.BaseEsSearch;
import com.znv.fss.es.EsConfig;
import com.znv.fss.es.EsManager;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.znv.fss.es.FormatObject.formatTime;
import static org.elasticsearch.lopq.LOPQModel.predictCoarseOrder;

/**
 * Created by User on 2017/8/3.
 */
public class MultiIndexExactSearch extends BaseEsSearch {
    protected static final Logger LOGGER = LogManager.getLogger(MultiIndexExactSearch.class);
    private String esurl;
    private String templateName;

    public MultiIndexExactSearch(String esurl, String tempalteName) {
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

    public int paramCheck(ExactSearchQueryParam inParam) {
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
        if(inParam.getIsCalcSim()==true){
        if (inParam.getFeatureValue().size()==0) {
            LOGGER.info("FeatureValue can't be empty ！");
            return FssErrorCodeEnum.ES_INVALID_PARAM.getCode();
        }
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

    private JSONObject getTemplateParams(ExactSearchQueryParam inParam) {
        JSONObject paramsT = new JSONObject();
        String excludes[] = {"rt_feature"};
        if (inParam.getEnterTimeStart() != null){
            paramsT.put("enter_time_start", inParam.getEnterTimeStart());
        }
        if (inParam.getEnterTimeEnd() != null){
            paramsT.put("enter_time_end", inParam.getEnterTimeEnd());
        }
        if (inParam.getCameraName() != null){
            paramsT.put("camera_name",inParam.getCameraName());
        }
        if (inParam.getOfficeName() != null){
            paramsT.put("office_name",inParam.getOfficeName());
        }
        if(inParam.getPersonId()!=null){
            paramsT.put("person_id",inParam.getPersonId());
        }
        if(inParam.getGender()!= -1){
            paramsT.put("gender",inParam.getGender());
        }
        if(inParam.getBeard()!= -1){
            paramsT.put("beard",inParam.getBeard());
        }
        if(inParam.getGlass()!= -1){
            paramsT.put("glass",inParam.getGlass());
        }
        if(inParam.getMask()!= -1){
            paramsT.put("mask",inParam.getMask());
        }
        if(inParam.getEmotion() != -1){
            paramsT.put("emotion",inParam.getEmotion());
        }
        if(inParam.getRace() != -1){
            paramsT.put("race",inParam.getRace());
        }
        if(inParam.getEyeOpen() != -1){
            paramsT.put("eye_open",inParam.getEyeOpen());
        }
        if(inParam.getMouthOpen() != -1){
            paramsT.put("mouth_open",inParam.getMouthOpen());
        }
        if(inParam.getAgeStart() != -1){
            paramsT.put("age_start",inParam.getAgeStart());
        }
        if(inParam.getAgeEnd() != -1 ){
            paramsT.put("age_end",inParam.getAgeEnd());
        }
        paramsT.put("from", inParam.getFrom());
        paramsT.put("size", inParam.getSize());
        paramsT.put("is_excludes",true);
        paramsT.put("excludes",excludes);
        if(inParam.getIsCalcSim()==true) {
            paramsT.put("is_calcSim",true);
            paramsT.put("feature_name", "rt_feature.feature_high");
            FeatureCompUtil fc = new FeatureCompUtil();
            fc.setFeaturePoints(EsConfig.getFeaturePoints());
            float sim = (float) inParam.getSimThreshold();
            paramsT.put("sim_threshold", fc.reversalNormalize(sim));//脚本中未归一化
            paramsT.put("feature_value", inParam.getFeatureValue());
            paramsT.put("filter_type", inParam.getFilterType());
            paramsT.put("from",0);
            paramsT.put("size",10000);
        }
          if(inParam.getSortField()!=null) {
              paramsT.put("sortField", inParam.getSortField());
          }
          if(inParam.getSortOrder()!=null) {
            paramsT.put("sortOrder", inParam.getSortOrder());
          }
         // int minShouldMatch = 0;
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
         /* if (minShouldMatch != 0) {
             paramsT.put("minimum_should_match", minShouldMatch);
          }*/

           JSONObject obj = new JSONObject();
           obj.put("id", templateName);
           obj.put("params", paramsT);

           return obj;
    }

    // 重新封装返回结果
    @Override
    public JSONObject requestSearch(String params) {
        FeatureCompUtil fc = new FeatureCompUtil();
        WriteDataToES writeDataToES = new WriteDataToES();
        fc.setFeaturePoints(EsConfig.getFeaturePoints());//商汤归一化数组
        ExactSearchJsonIn exactSearchJsonIn = JSON.parseObject(params, ExactSearchJsonIn.class);
        ExactSearchQueryParam queryParams = exactSearchJsonIn.getParams();
        int totalNum = 0;
        int errCode = paramCheck(queryParams);
        if (errCode != FssErrorCodeEnum.SUCCESS.getCode()) {
            return getErrorResult(errCode);
        }
        //重新封装查询参数
        JSONObject obj = getTemplateParams(queryParams);
        //如果传图片查询，按索引分别返回，如果不按图片查询，只是特征检索，则按统一的别名查询
        if(queryParams.getIsCalcSim() == true) {
            //获取跨类搜索的跨类个数，因为要全库搜索，所以默认是36
            int coarseCodeNum = 36;
            if(queryParams.getCoarseCodeNum() != 0){
                coarseCodeNum =queryParams.getCoarseCodeNum();
            }
            //计算粗分类的标签coarse_id
            int[][] coarseCodeOrder;
            String indexNamePrepix = EsManager.fssEsIndexHistoryPrefix;
            String indexName = "";
            List<String> featureValue = queryParams.getFeatureValue();
            try {
                //featureValue.get(0):因为featureValue是一个list，查询索引的先后顺序默认按第一个featureValue的coarse_id顺序
                coarseCodeOrder = predictCoarseOrder(fc.getFloatArray(new org.apache.commons.codec.binary.Base64().decode(featureValue.get(0))), coarseCodeNum);
            } catch (Exception e) {
                LOGGER.info("Get Coarse Code Error: " + e);
                return getErrorResult(FssErrorCodeEnum.ES_GET_COARSE_CODE_ERROR.getCode());
            }
            //按计算出的coarse_id的顺序分别按索引进行查询，并将结果写到es中，全部写完之后，返回一个error_code:100000
            for (int j = 0; j < coarseCodeOrder.length; j++) {
                long t1 = System.currentTimeMillis();
                indexName = indexNamePrepix +"-"+ coarseCodeOrder[j][0];
                String esurl = EsManager.concatenateURL(indexName, EsManager.fssEsIndexHistoryType);
                // HTTP连接
                JSONObject httpConResult = initConnectParams(esurl);

                if (httpConResult != null) {
                    return httpConResult;
                }
                //从es中获取结果,获取完结果就会关闭http连接
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
                // System.out.println(FormatObject.format(jsonEsResult.toString()));
                // 从es查询结果中获取hits
                JSONArray esHits = jsonEsResult.getJSONObject("hits").getJSONArray("hits");
                //TODO write to es
                if(esHits.size()>0) {
                    totalNum +=esHits.size();
                    writeDataToES.bulkWriteToEs(EsManager.esExactSearchResult, EsManager.fssEsIndexHistoryType, esHits, queryParams.getEventId(), j, indexName);
                }
                long ts = System.currentTimeMillis() - t1;
                System.out.println("完成第" + j + "个索引的查询,index_name:"+indexName+"返回"+esHits.size()+"条结果，耗时" + ts + "ms");
            }
            JSONObject result = new JSONObject();
            result.put("total",totalNum);
            result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
            return result;
        }else {
            // HTTP连接
            JSONObject httpConResult = initConnectParams();
            if (httpConResult != null) {
                return httpConResult;
            }
            StringBuffer sb = super.getSearchResult(obj);
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
            // System.out.println(JSON.toJSONString(jsonEsResult));
            // 从es查询结果中获取hits
            JSONArray esHits = jsonEsResult.getJSONObject("hits").getJSONArray("hits");
            List<ExactSearchQueryHit> outHits = new ArrayList<>(esHits.size());
            for (int i = 0; i < esHits.size(); i++) {
                ExactSearchQueryHit outHit = new ExactSearchQueryHit();
                JSONObject hit = esHits.getJSONObject(i);
                outHit.setScore( hit.getFloatValue("_score")); //没有图片比对，不用归一化
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
            ExactSearchJsonOut outputResult = new ExactSearchJsonOut();
            int total = jsonEsResult.getJSONObject("hits").getInteger("total");
            int took = jsonEsResult.getInteger("took");
            outputResult.setErrorCode(FssErrorCodeEnum.SUCCESS.getCode());
            outputResult.setTotal(total);
            outputResult.setHits(outHits);
            outputResult.setTook(took);

            return (JSONObject) JSONObject.toJSON(outputResult);
        }
    }
}
