package com.znv.fss.es.FssSearchByTrail;

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

public class FssByTrailSearch extends BaseEsSearch {

    protected static final Logger LOGGER = LogManager.getLogger(FssByTrailSearch.class);
    private String esurl;
    private String templateName;

    public FssByTrailSearch(String esurl, String tempalteName) {
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
        SearchByTrailJsonIn inputParam = JSON.parseObject(params,SearchByTrailJsonIn.class);
        SearchByTrailQueryParam queryParams = inputParam.getParams();
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
        List<SearchByTrailQueryHit> outHits = new ArrayList<>();
        fc.setFeaturePoints(EsConfig.getFeaturePoints());
        for (int i = 0; i < esHits.size(); i++) {
            SearchByTrailQueryHit outHit = new SearchByTrailQueryHit();
            JSONObject hit = esHits.getJSONObject(i);
            JSONObject source = hit.getJSONObject("_source"); // 从hits数组中获取_source
            if(i>=1 && (source.getString("camera_id").equals(outHits.get(outHits.size()-1).getCameraId()))){
                outHits.get(outHits.size()-1).setStayNum(outHits.get(outHits.size()-1).getStayNum()+1);
            }else {
                outHit.setCameraId(source.getString("camera_id"));
                String gpsXy = source.getString("gps_xy");
                if(gpsXy != null){
                    String[] gps = gpsXy.split(",");
                    outHit.setGpsy(gps[0]);
                    outHit.setGpsx(gps[1]);
                }
                outHit.setEnterTime(formatTime(source.getString("enter_time")));
                outHit.setStayNum(1);
                outHits.add(outHit);
            }

        }
        SearchByTrailJsonOut outputResult = new SearchByTrailJsonOut();
        int total = jsonEsResult.getJSONObject("hits").getInteger("total");
        int took = jsonEsResult.getInteger("took");
        outputResult.setErrorcode(FssErrorCodeEnum.SUCCESS.getCode());
        outputResult.setTotal(total);
        outputResult.setHits(outHits);
        outputResult.setTook(took);

        return (JSONObject) JSONObject.toJSON(outputResult);

    }
    private int paramCheck(SearchByTrailQueryParam inParam) {
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
        return FssErrorCodeEnum.SUCCESS.getCode();
    }

    private JSONObject getTemplateParams(SearchByTrailQueryParam inParam) {
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
        paramsT.put("from", 0);
        paramsT.put("size", 10000);
        paramsT.put("sortField", inParam.getSortField());
        paramsT.put("sortOrder", inParam.getSortOrder());
        paramsT.put("is_excludes",true);
        paramsT.put("excludes",excludes);
        if (inParam.getOfficeId() != null && !inParam.getOfficeId().isEmpty()) {
            paramsT.put("office_id", inParam.getOfficeId());
            paramsT.put("is_office",true);
        }
        if (inParam.getCameraId() != null && !inParam.getCameraId().isEmpty()) {
            paramsT.put("camera_id", inParam.getCameraId());
            paramsT.put("is_camera",true);
        }
        JSONObject obj = new JSONObject();
        obj.put("id", templateName);
        obj.put("params", paramsT);

        return obj;
    }

}
