package com.znv.fss.common.utils;

import com.znv.fss.common.VConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/3/14.
 */
public class PropertiesUtil {
    private static final Log LOG = LogFactory.getLog(PropertiesUtil.class);
    private static final Pattern PATTERN = Pattern.compile("\\$\\{([^\\}]+)\\}");

    //读取HDFS上的配置文件
    public static Properties loadFromHdfs(String fileHdfsPath) throws IOException{
        Properties prop = new Properties();
        Configuration conf = new Configuration();
        FSDataInputStream in = null;
        try {
            FileSystem fs = FileSystem.get(URI.create(fileHdfsPath), conf);
            in = fs.open(new Path(fileHdfsPath));
            prop.load(in);
        } catch (IOException e) {
            LOG.error("read properties error!! filepath:" + fileHdfsPath + "." + e);
            throw e;
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOG.error("读取hdfs配置文件，流关闭异常", e);
                }
            }
        }
        return prop;
    }

    //读取resources下的配置文件
    public static Properties loadFromResource(String filename) {
        Properties prop = new Properties();
        InputStream in = null;
        try {
            in = PropertiesUtil.class.getClassLoader().getResourceAsStream(filename);
            prop.load(in);
        } catch (IOException e) {
            LOG.info("read properties error!! filename:" + filename + "." + e);
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOG.error("读取本地配置文件，流关闭异常", e);
                }
            }
        }
        return prop;
    }

    /**
     * 获取Properties对象中key对应value, 并替换value中引用变量的部分为实际的值
     *
     * @param properties
     * @param key
     * @return value
     */
    public static String get(Properties properties, String key) {
        String value = properties.getProperty(key);
        Matcher matcher = PATTERN.matcher(value);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String matcherKey = matcher.group(1);
            String matchervalue = properties.getProperty(matcherKey);
            if (matchervalue != null) {
                matcher.appendReplacement(buffer, matchervalue);
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    //获取config.properties配置文件中的值
    public static String getConfig(String key) {
        Properties prop = new Properties();
        String value = "";
        try {
            prop.load(new InputStreamReader(PropertiesUtil.class.getClassLoader().getResourceAsStream("config.properties"),"UTF-8"));
            value = prop.get(key).toString();
        } catch (IOException e) {
            LOG.error(e);
        }
        return value;
    }


    /**
     * @param properties
     * @return
     */
//    sencetime.feature.srcPoints = {-1.0f, 0.4f, 0.42f, 0.44f, 0.48f, 0.53f, 0.58f, 1.0f}
//    sencetime.feature.dstPoints = {0.0f, 0.4f, 0.5f, 0.6f, 0.7f, 0.85f, 0.95f, 1.0f}
    public static Map<String, float[]> getFeaturePoints(Properties properties) throws Exception {
        Map<String, float[]> featurePoints = new ConcurrentHashMap<String, float[]>(2);
        String srcStr = get(properties, VConstants.SENSETIME_FEATURE_SRC);
        String dstStr = get(properties, VConstants.SENSETIME_FEATURE_DST);
        try {
            if (null != srcStr && !"".equals(srcStr) && null != dstStr && !"".equals(dstStr)) {
                String srcStrTemp = srcStr.replaceAll("\\{", "").replaceAll(" ", "");
                String srcStrLast = srcStrTemp.replaceAll("\\}", ""); // "-1.0f, 0.4f, 0.42f, 0.44f, 0.48f, 0.53f, 0.58f, 1.0f"
                String[] srcPointsStrArray = srcStrLast.split(","); // 分裂成数组:[-1.0f, 0.4f, 0.42f, 0.44f, 0.48f, 0.53f, 0.58f, 1.0f]

                String dstStrTemp = dstStr.replaceAll("\\{", "").replaceAll(" ", "");
                String dstStrLast = dstStrTemp.replaceAll("\\}", "");
                String[] dstPointsStrArray = dstStrLast.split(",");

                if (srcPointsStrArray.length > 0 && dstPointsStrArray.length > 0 && dstPointsStrArray.length == srcPointsStrArray.length) {
                    int arrayLen = dstPointsStrArray.length;
                    float[] srcPointsF = new float[arrayLen];
                    float[] dstPointsF = new float[arrayLen];

                    // “-1.0f”转-1.0
                    for (int i = 0; i < arrayLen; i++) {
                        srcPointsF[i] = Float.parseFloat(srcPointsStrArray[i]);
                        dstPointsF[i] = Float.parseFloat(dstPointsStrArray[i]);
                    }
                    featurePoints.put(VConstants.SENSETIME_FEATURE_SRC, srcPointsF);
                    featurePoints.put(VConstants.SENSETIME_FEATURE_DST, dstPointsF);
                    return featurePoints;

                } else {
                    LOG.error("配置文件中商汤归一化数组长度不一致！");
                    // System.out.println("配置文件中商汤归一化数组长度不一致！");
                    throw new IllegalArgumentException(" HDFS sensetime srt_points.len != drt_points.len !");
                }
            } else {
                LOG.error("配置文件中商汤归一化数组异常！");
                // System.out.println("配置文件中商汤归一化数组异常！");
                throw new IllegalArgumentException(" exception in HDFS sensetime srt_points and drt_points!");
            }
        } catch (Exception e) {
            LOG.error("配置文件中商汤归一化数组异常！");
            //return featurePoints;
            throw new IllegalArgumentException("exception in HDFS sensetime srt_points and drt_points!");
        }

        //return featurePoints;

    }

}
