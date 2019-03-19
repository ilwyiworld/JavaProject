
package com.znv.utils;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hdfs工具类，从hdfs上读取项目配置文件
 */
public class HdfsUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HdfsUtil.class);

    private static final Pattern PATTERN = Pattern.compile("\\$\\{([^\\}]+)\\}");

    /**
     * 根据hdfs路径和文件路径读取properties文件
     * @param hdfsUrl
     * @param filePath
     * @param fileName
     * @return
     */
    public static Properties getPropFromHdfs(String hdfsUrl, String filePath, String fileName) {
        Properties prop = new Properties();
        if (StringUtils.isNotBlank(hdfsUrl) && StringUtils.isNotBlank(filePath) && StringUtils.isNotBlank(fileName)) {
            if (!filePath.startsWith("/")) {
                filePath = "/" + filePath;
            }
            if (!filePath.endsWith("/")) {
                filePath = filePath + "/";
            }
            String hdfsFilePath = hdfsUrl.concat(filePath).concat(fileName);
            Configuration conf = new Configuration();
            FSDataInputStream in = null;
            try {
                FileSystem fs = FileSystem.get(URI.create(hdfsFilePath), conf);
                in = fs.open(new Path(hdfsFilePath));
                prop.load(in);
            } catch (IOException e) {
                LOGGER.error("读取hdfs配置文件IO异常，hdfsFilepath：{}", hdfsFilePath, e);
            } finally {
                if (null != in) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        LOGGER.error("读取hdfs配置文件，流关闭异常", e);
                    }
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

}
