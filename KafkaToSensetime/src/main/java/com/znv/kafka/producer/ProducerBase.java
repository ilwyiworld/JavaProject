package com.znv.kafka.producer;

import com.alibaba.fastjson.JSONObject;
import com.znv.util.PropertiesUtil;
import com.znv.util.TopicUtil;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Properties;

/**
 * 生产者.
 */
public class ProducerBase {

    private static final Logger LOGGER = LogManager.getLogger(ProducerBase.class);

    /** 生产者. */
    protected KafkaProducer<String, JSONObject> producer = null;

    /** 是否初始化. */
    protected boolean isInit = false;

    /** 是否重发，kafka默认会重发，所以该参数可以为false. */
    private boolean isResend = false;

    private static String topic;

    public static String getTopic(){
        return topic;
    }

    /**
     * Instantiates a new producer base.
     */
    public ProducerBase(){
        //initWithConfig("producer.properties");
    }

    public void initWithConfig(String propertiesPath){
        Properties props= PropertiesUtil.getProperties(propertiesPath);
        topic= TopicUtil.getTopicList("send.topics").get(0);
        init(false, props);
    }

    /**
     * 初始化.
     * @param isResend 是否重发，kafka默认会重发，所有该参数可以为false
     * @throws Exception the exception
     */
    void init(boolean isResend, Properties props){
        LOGGER.info("Producer init(isResend: {})... ", isResend);
        if (!isInit) {
            this.topic= TopicUtil.getTopicList("send.topics").get(0);
            this.isResend = isResend;
            producer = new KafkaProducer<String, JSONObject>(props);
            isInit = true;
        }
        LOGGER.info("Producer init end.");
    }

    /**
     * 发送数据.
     * @param data the data
     * @return true, if successful
     */
    public boolean sendData(JSONObject data) {
        return sendData(null, data);
    }

    /**
     * 发送数据.
     * @param dataMap Map格式的数据
     * @return true, if successful
     */
    public boolean sendData(Map<String, Object> dataMap) {
        return sendData(new JSONObject(dataMap));
    }

    /**
     * 发送数据.
     * @param data the data
     * @return true, if successful
     */
    public boolean sendData(String key, JSONObject data) {
        // 增加方法调用次数
        ProducerState.addCallCount();

        // 生成回调类
        ProducerCallback callback = null;
        if (isResend) {
            callback = new ProducerCallback(this, data);
        }

        try {
            // 发送数据
            //LOGGER.debug("发送到kafka的数据："+data.getString("user_data"));
            return sendData(key, data, callback);
        } catch (Exception e) {
            // 发送异常调用回调
            if (isResend) {
                callback.onCompletion(null, e);
            } else {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * 发送数据.
     * @param key 计算分区的key
     * @param record 数据
     * @param callback 回调
     * @return true, if successful
     * @throws Exception 异常
     */
    boolean sendData(String key, JSONObject record, Callback callback) throws Exception {
        return sendData(topic, key, record, callback);
    }

    /**
     * 发送数据.
     * @param topic 主题
     * @param key 索引
     * @param record 数据
     * @param callback 回调
     * @return true, if successful
     * @throws Exception 异常
     */
    boolean sendData(String topic, String key, JSONObject record, Callback callback) throws Exception {
        // 如果没有初始化，则初始化
        if (!isInit) {
            LOGGER.warn("init Producer on send data!!!");
            initWithConfig("producer.properties");
        }

        if (producer == null) {
            LOGGER.error("Producer is null, skip!!!");
            ProducerState.addSkipCount();
            return false;
        }

        // 生产者发送数据
        ProducerRecord<String, JSONObject> data = new ProducerRecord<String, JSONObject>(topic, key, record);
        if (callback != null) {
            producer.send(data, callback);
        } else {
            producer.send(data);
        }
        ProducerState.addSendCount();
        return true;
    }

    public void flush() {
        producer.flush();
    }

    /**
     * 退出程序时关闭.
     */
    public void close() {
        // 如果已经初始化
        if (isInit) {
            LOGGER.info("Producer close...");
            producer.close();
            isInit = false;
            LOGGER.info("Producer close end.");
        }
    }
}