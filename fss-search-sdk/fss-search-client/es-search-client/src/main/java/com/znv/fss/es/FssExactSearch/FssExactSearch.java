package com.znv.fss.es.FssExactSearch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.znv.fss.common.errorcode.FssErrorCodeEnum;
import com.znv.fss.es.BaseEsSearch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.znv.fss.es.FormatObject.formatTime;

/**
 * Created by User on 2017/8/3.
 */
public class FssExactSearch extends BaseEsSearch {
    protected static final Logger LOGGER = LogManager.getLogger(FssExactSearch.class);
    private String esurl;
    private String templateName;

    public FssExactSearch(String esurl, String tempalteName) {
        this.esurl = esurl;
        this.templateName = tempalteName;
    }

    public JSONObject initConnectParams() {
        JSONObject httpCon = super.httpConnection.esHttpConnect(esurl);
        return httpCon;
    }

    public JSONObject paramCheck(String inParam) {
        JSONObject params = JSON.parseObject(inParam);
        JSONObject checkResult = null;
        if (params.containsKey("camera_type")) {
            int cameraType = params.getIntValue("camera_type");
            if (cameraType != 0 && cameraType != 1) {
                LOGGER.info("the value of camera_type can be only 0 or 1");
                checkResult = new JSONObject();
                checkResult.put("errorCode", FssErrorCodeEnum.ES_INVALID_PARAM.getCode());
            }
        }
        if (params.containsKey("age_start") && params.containsKey("age_end")) {
            int ageStart = params.getIntValue("age_start");
            int ageEnd = params.getIntValue("age_end");
            if (ageStart > ageEnd) {
                LOGGER.info("age_start can't bigger than age_end");
                checkResult = new JSONObject();
                checkResult.put("errorCode", FssErrorCodeEnum.ES_INVALID_PARAM.getCode());
            }
        }
        if (params.containsKey("from") && params.containsKey("size")) {
            int from = params.getIntValue("from");
            int size = params.getIntValue("size");
            if ((from + size) >= 10000) {
                checkResult = new JSONObject();
                checkResult.put("errorCode", FssErrorCodeEnum.ES_SIZE_OUT_OF_RANGE.getCode());
            }
        }
        return checkResult;
    }

    // 重新封装返回结果
    @Override
    public JSONObject requestSearch(String params) {
        long timeStart = System.currentTimeMillis();
        String inParam = JSON.parseObject(params).getString("params");
        JSONObject checkResult = paramCheck(inParam);
        // HTTP连接
        JSONObject jsonEsResult = initConnectParams();
        if (jsonEsResult == null && checkResult == null) {
            // paramCheck(inParam);
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
                // System.out.println("读取es查询结果如下：");
                // System.out.println(FormatObject.format(jsonEsResult.toString()));
                // 从es查询结果中获取hits
                JSONArray esHits = jsonEsResult.getJSONObject("hits").getJSONArray("hits");
                JSONArray hits = new JSONArray();
                JSONObject outputResult = new JSONObject();
                if (null != esHits && esHits.size() != 0) {
                    /*String phoenixTableName = EsConfig.getProperty(VConstants.FSS_PHOENIX_TABLE_HISTORY_NAME);
                    String phoenixSchemaName = EsConfig.getProperty(VConstants.FSS_PHOENIX_SCHEMA_NAME);
                    String tableName = phoenixSchemaName + "." + phoenixTableName;
                    String sql1 = String.format("select * from %s where", tableName);
                    StringBuffer sqlBuf = new StringBuffer();
                    for (int i = 0; i < esHits.size(); i++) {
                        JSONObject source = esHits.getJSONObject(i).getJSONObject("_source");
                        String uuid = source.getString("uuid");
                        String enterTime = source.getString("enter_time");
                        enterTime = formatTime(enterTime);
                        if (i < esHits.size() - 1) {
                            sqlBuf.append(sql1 + String.format("(ENTER_TIME='%s'and uuid= '%s' )", enterTime, uuid) + "union all ");
                        } else {
                            sqlBuf.append(sql1 + String.format("(ENTER_TIME='%s'and uuid= '%s')", enterTime, uuid));
                        }
                    }
                    String sql = sqlBuf.toString();
                    HashMap<String, byte[]> map = searchImageData(sql);
                    */
                    for (int i = 0; i < esHits.size(); i++) {
                        String score = esHits.getJSONObject(i).getString("_score");
                        JSONObject source = esHits.getJSONObject(i).getJSONObject("_source"); // 从hits数组中获取_source
                        // String uuid = source.getString("uuid");
                        String enterTime = source.getString("enter_time");
                        String leaveTime = source.getString("leave_time");
                        String opTime = source.getString("op_time");
                        enterTime = formatTime(enterTime);
                        leaveTime = formatTime(leaveTime);
                        opTime = formatTime(opTime);

                        String feature = source.getString("rt_feature");
                      /*  try {
                            BASE64Decoder decode = new BASE64Decoder();
                            byte[] image = decode.decodeBuffer(feature);
                            System.out.println(image);
                        }catch(Exception e){
                            e.printStackTrace();
                        }*/
                        source.put("enter_time", enterTime);
                        source.put("leave_time", leaveTime);
                        source.put("op_time", opTime);
                        source.put("score", score);
                        /*if (EsManager.phoeCon.getInteger("errorCode") != FssErrorCodeEnum.SUCCESS.getCode()) {
                            return phoeCon;
                        } else {
                            byte[] img = map.get(uuid);
                           // source.put("rt_image_data", img);
                            //source.put("rt_image_data", "/root/FSSTestClient/FSSConditionSearch/result/"+ uuid +".jpg");
                            FeatureToPicture.featureToPicture(img,uuid);
                            hits.add(i, source);
                        }*/
                        hits.add(i, source);
                    }
                }
                String total = jsonEsResult.getJSONObject("hits").getString("total");
                outputResult.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
                outputResult.put("hits", hits);
                outputResult.put("total", total);
                long timeCost = System.currentTimeMillis() - timeStart;
                outputResult.put("time", String.valueOf(timeCost));
                // 将结果输入的文件中
                // ResultToFile.resultToFile(path,"The result:\n"+FormatObject.format(jsonEsResult.toString()));

                return outputResult;
                /*else{
                    jsonEsResult = new JSONObject(true);
                    jsonEsResult.put("errorCode", FssErrorCodeEnum.ES_GET_NULL.getCode());
                }*/
            }
        } else if (checkResult != null) {
            return checkResult;
        }
        return jsonEsResult;

    }

}
