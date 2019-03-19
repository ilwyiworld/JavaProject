package com.znv.fss.es;


import com.znv.fss.common.utils.PropertiesUtil;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by User on 2017/8/3.
 */
public class EsConfig {
    private static Properties prop = null;
    private static Properties propSDK = null;
    private static Map<String, float[]> points = new ConcurrentHashMap<String, float[]>(2);

    public static void getProp(String hostUrl) throws Exception {
        // 读取HDFS文件得到配置参数和HBase表名
        prop = PropertiesUtil.loadFromHdfs(hostUrl + "/config/fss.properties");
        //prop = PropertiesUtil.loadFromResource("fss.properties");
        propSDK = PropertiesUtil.loadFromResource("esClientConfig.properties");
        points = PropertiesUtil.getFeaturePoints(prop);
    }

    public static String getProperty(String key) {
        return PropertiesUtil.get(prop, key);
    }

    public static Properties getProperty() {
        return prop;
    }

    public static Properties getPropertySDK() {
        return propSDK;
    }

    public static Map<String, float[]> getFeaturePoints() {
        return points;
    }


}
