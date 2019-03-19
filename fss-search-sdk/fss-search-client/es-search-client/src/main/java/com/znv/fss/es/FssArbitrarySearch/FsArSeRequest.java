package com.znv.fss.es.FssArbitrarySearch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.znv.fss.common.utils.FeatureCompUtil;
import com.znv.fss.es.EsConfig;
import com.znv.fss.es.EsManager;
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
        FeatureCompUtil fc = new FeatureCompUtil();
        fc.setFeaturePoints(EsConfig.getFeaturePoints());
        LOG.info("start to search...");
        try {

            obj = JSON.parseObject(idParam);
            try {
                obj2 = JSON.parseObject(inParam);
            } catch (Exception e) {
                obj.put("errorCode", FssErrorCodeEnum.ES_INVALID_PARAM.getCode());
                LOG.error("invalid parameters" + e);
                return null;
            }
            // 判断是否包含"feature_value"，如果包含就将其路径转成图片数据并重新赋值给"feature_value"
            if (obj2.containsKey("is_calcSim")) {
                String isCalcSim = obj2.get("is_calcSim").toString();
                if (isCalcSim.equals("true")) {
                    if (obj2.containsKey("feature_value") && obj2.containsKey("feature_name")) {
                        //Object pathValue = obj2.get("feature_value");
                       // String imagePath = pathValue.toString();
                        String featureName = obj2.getString("feature_name");
                        featureName += "." + FEATURE_HIGH;
                        obj2.put("feature_name", featureName);
                       // String featureValue = GetFeatureValue.getFeatureValue(imagePath);
                        //obj2.put("feature_value", featureValue);//图片是float类型
                        //obj2.put("feature_value", imagePath );//图片是string类型
                        if(obj2.containsKey("sim_threshold")) {
                            float sim = obj2.getFloatValue("sim_threshold");
                            obj2.put("sim_threshold", fc.reversalNormalize(sim));
                        }else{
                            LOG.error("Invalid parameters:Lost the sim_threshold parameter");
                        }
                      /*  if (EsManager.searchId.equals("13001")) {
                            String featureName = obj2.getString("feature_name");
                            featureName += "." + FEATURE_HIGH;
                            obj2.put("feature_name", featureName);
                        }*/
                    }
                } else {
                    if (isCalcSim.equals("false")) {
                        // 将false转成布尔值
                        obj2.put("is_calcSim", false);
                    }
                }

            }
            obj.put("params", obj2);
          //  System.out.println("params:"+obj2);
            LOG.info("your parameters are：" + obj.toString());
        } catch (NullPointerException pe) {
            LOG.error(pe);

        } catch (Exception e) {
            LOG.error(e);
        }
        return obj;
    }

}
