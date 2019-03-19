package com.znv.fss.conf;

import com.znv.fss.common.VConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;


/**
 * Created by ct on 2016-12-14.
 */
public class ConfigManager {
    private static Properties props = new Properties();
    private static Properties producerProps = new Properties();
    private static final Logger L = LoggerFactory.getLogger(ConfigManager.class);
    private static final Pattern PATTERN = Pattern.compile("\\$\\{([^\\}]+)\\}"); // 正则匹配
    private static Properties fssPoolProps = new Properties(); // [lq add] read phoenix Pool config from program

    public ConfigManager() {

    }

    /**
     * 读取连接池配置
     */
    // [lq add]
    private void poolInit() {
        InputStream poolConfIn = null;
        String poolConfigPath = "/fssPhoenixPool.properties";
        try {
            poolConfIn = Object.class.getResourceAsStream(poolConfigPath);
            fssPoolProps.load(poolConfIn);
        } catch (IOException e) {
            L.error("read phoenix pool config error {}", e);
        } finally {
            if (null != poolConfIn) {
                try {
                    poolConfIn.close();
                } catch (IOException e) {
                    L.error("close HDFS error {}", e);
                }
            }
        }
    }

    /**
     * @param key
     * @return
     */
    public static String getPoolString(String key) {
        String value = fssPoolProps.getProperty(key);
        Matcher matcher = PATTERN.matcher(value);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String matcherKey = matcher.group(1);
            String matchervalue = fssPoolProps.getProperty(matcherKey);
            if (matchervalue != null) {
                matcher.appendReplacement(buffer, matchervalue);
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * @param key
     * @return
     */
    public static int getPoolInt(String key) {
        return Integer.parseInt(getPoolString(key));
    }

    // 读取HDFS上的配置文件
    public void init(String fileHdfsPath) {
        Configuration conf = new Configuration();
        FSDataInputStream in = null;
        String configPath = fileHdfsPath + "/fss.properties";
        try {
            FileSystem fs = FileSystem.get(URI.create(configPath), conf);
            in = fs.open(new Path(configPath));
            props.load(in);
        } catch (IOException e) {
            L.error("read HDFS config error {}", e);
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    L.error("close HDFS error {}", e);
                }
            }
        }

        poolInit(); // [lq add]
    }

    /**
     * @param key 获取Properties对象中key对应value, 并替换value中引用变量的部分为实际的值
     * @return
     */
    public static String getString(String key) {
        String value = props.getProperty(key);
        Matcher matcher = PATTERN.matcher(value);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String matcherKey = matcher.group(1);
            String matchervalue = props.getProperty(matcherKey);
            if (matchervalue != null) {
                matcher.appendReplacement(buffer, matchervalue);
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    public static int getInt(String key) {
        return Integer.parseInt(getString(key));
    }

    public static long getLong(String key) {
        return Long.parseLong(getString(key));
    }

    public static String getTableName(String key) {
        String schemaKey = VConstants.FSS_PHOENIX_SCHEMA_NAME;
        String schemaName = getString(schemaKey);
        String tableName = getString(key);

        String fullTableName = schemaName + "." + tableName;
        return fullTableName;

    }

    /**
     * @param fileHdfsPath
     */
    public void producerInit(String fileHdfsPath) {
        String producerPath = fileHdfsPath + "/producerBasic.properties";
        Configuration conf = new Configuration();
        FSDataInputStream in = null;
        try {
            FileSystem fs = FileSystem.get(URI.create(producerPath), conf);
            in = fs.open(new Path(producerPath));
            producerProps.load(in);
        } catch (IOException e) {
            L.error("read producerBasic.properties config error {}", e);
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    L.error("close  producerBasic.properties error {}", e);
                }
            }
        }
    }

    /**
     * @return 获取生产者配置信息
     */
    public static Properties getProducerProps() {
        return producerProps;
    }

    public static Properties getProps() {
        return props;
    }

}
