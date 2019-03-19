package com.znv.fss.es.FssExactSearch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.znv.fss.common.errorcode.FssErrorCodeEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by User on 2017/8/4.
 */
public class FsArSeRequest {
    protected static final Logger LOG = LogManager.getLogger(FsArSeRequest.class);
    private static final String FEATURE_HIGH = "feature_high";

    public static JSONObject manualInput(String idParam, String inParam) {
        JSONObject obj = null, obj2 = null;
        LOG.info("start to search...");
        try {

            obj = JSON.parseObject(idParam);
            try {
                obj2 = JSON.parseObject(inParam);
            } catch (Exception e) {
                obj.put("errorCode", FssErrorCodeEnum.ES_INVALID_PARAM.getCode());
                LOG.error("invalid parameters" + e);
            }
            // 判断是否包含"feature_value"，如果包含就将其路径转成图片数据并重新赋值给"feature_value"
            /*if (obj2.containsKey("is_calcSim")) {
                String isCalcSim = obj2.get("is_calcSim").toString();
                if (isCalcSim.equals("true")) {
                    if (obj2.containsKey("feature_value")) {
                        if(!EsManager.searchId.equals("13005")) {
                            Object pathValue = obj2.get("feature_value");
                            String imagePath = pathValue.toString();
                            String featureValue = GetFeatureValue.getFeatureValue(imagePath);
                            obj2.put("feature_value", featureValue);
                        }
                        else {
                            Object pathValue = obj2.get("feature_value");
                            String imagePath = pathValue.toString();
                            String[] arr = imagePath.substring(1, imagePath.length() - 1).split(",");
                            StringBuilder str = new StringBuilder();
                            for(int i = 0; i < arr.length; i++) {
                                str.append(GetFeatureValue.getFeatureValue(arr[i].substring(1, arr[i].length() - 1)));
                                if(i + 1 != arr.length) {
                                    str.append(",");
                                }
                            }
                            obj2.put("feature_value", str.toString());
                        }

                        if (EsManager.searchId.equals("13001")) {
                            String featureName = obj2.getString("feature_name");
                            featureName += "." + FEATURE_HIGH;
                            obj2.put("feature_name", featureName);
                        }
                    }
                } else {
                    if (isCalcSim.equals("false")) {
                        // 将false转成布尔值
                        obj2.put("is_calcSim", false);
                    }
                }

            }*/
            obj.put("params", obj2);
            LOG.info("your parameters are：" + obj.toString());
        } catch (NullPointerException pe) {
            LOG.error(pe);

        } catch (Exception e) {
            LOG.error(e);
        }
        return obj;
    }

}
