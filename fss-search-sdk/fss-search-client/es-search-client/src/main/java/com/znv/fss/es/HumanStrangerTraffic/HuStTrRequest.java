package com.znv.fss.es.HumanStrangerTraffic;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by User on 2017/8/3.
 */
public class HuStTrRequest {
    protected static final Logger LOGGER = LogManager.getLogger(HumanStrangerTraffic.class);

    public static JSONObject manualInput(String idParam, String inParam) {
        JSONObject obj = null, obj2 = null;
        System.out.println("start to search...");
        try {
            obj = JSON.parseObject(idParam);
            try {
                obj2 = JSON.parseObject(inParam);
            } catch (Exception e) {
                LOGGER.error("invalid parameters"+e);
                //  e.printStackTrace();
            }
          /*  obj2.put("child_start_age", 0);
            obj2.put("child_end_age", 8);
            obj2.put("child_start_age", 0);
            obj2.put("teenage_start_age", 8);
            obj2.put("teenage_end_age", 18);
            obj2.put("youth_start_age", 18);
            obj2.put("youth_end_age", 41);
            obj2.put("midlife_start_age", 41);
            obj2.put("midlife_end_age", 66);
            obj2.put("old_start_age", 66);
            obj2.put("old_end_age", 200);
            obj2.put("others_start_age", 200);*/
            //obj2.put("camera_numbers",150);
            obj.put("params", obj2);
          //  System.out.println("the parameters are:");
          //  System.out.println(obj.toString());
        } catch (Exception e) {
            LOGGER.error("Exception: \n" + e);
            //e.printStackTrace();
        }
        return obj;
    }

}
