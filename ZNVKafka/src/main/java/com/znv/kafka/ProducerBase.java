/**
 * <p>文件名称: 题目名称</p>
 * <p>文件描述: 本类描述</p>
 * <p>版权所有: 版权所有(C)2001-2015</p>
 * <p>公    司: 深圳市中兴力维软件有限公司</p>
 * <p>内容摘要: // 简要描述本文件的内容，包括主要模块、函数及能的说明</p>
 * <p>其他说明: // 其它内容的说明</p>
 * <p>完成日期：// 输入完成日期，例：2000年2月25日</p>
 * <p>修改记录1: // 修改历史记录，包括修改日期、修改者及修改内容</p>
 * <p>
 *    修改日期：
 *    版 本 号：
 *    修 改 人：
 *    修改内容：
 * </p>
 * <p>修改记录2：…</p>
 * @version 1.0
 * @author 0049000401
 */
package com.znv.kafka;

import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 生产者.
 */
public class ProducerBase {

    /** 日志. */
    private static final Logger L = LoggerFactory.getLogger(ProducerBase.class);

    /** 消息类型字段名. */
    public static final String MSG_TYPE = "msg_type";

    /** 生产者. */
    protected KafkaProducer<String, JSONObject> producer = null;

    /** 是否初始化. */
    protected boolean isInit = false;

    /** 默认的topic后缀. */
    private String topicSub = "production";

    /** 是否重发，kafka默认会重发，所有该参数可以为false. */
    private boolean isResend = false;

    /** topic相关的参数. */
    private Map<String, TopicCommond> topicMap = new HashMap<String, TopicCommond>();

    /**
     * Instantiates a new kafka.producer base.
     */
    public ProducerBase() {
    }

    public void init() throws Exception {
        init(null, false);
    }

    public void initWithConfig(Properties props) throws Exception {
        init(null, false, props);
    }

    /**
     * 初始化.
     * @param topicSub topic后缀
     * @throws Exception the exception
     */
    public void init(String topicSub) throws Exception {
        init(topicSub, false);
    }

    /**
     * 初始化.
     * @param topicSub topic后缀
     * @param isResend 是否重发，kafka默认会重发，所有该参数可以为false
     * @throws Exception the exception
     */
    void init(String topicSub, boolean isResend) throws Exception {
        init(topicSub, isResend, null);
    }

    void init(String topicSub, boolean isResend, Properties props) throws Exception {
        L.info("Producer init(topicSub: {}, isResend: {})... ", topicSub, isResend);
        if (!isInit) {
            File configFile = null;
            if (props == null) {
                props = new Properties();
                configFile = new File("kafka.producer.properties");
                L.info("config file path: {}", configFile.getAbsolutePath());

                try (InputStream in = new BufferedInputStream(new FileInputStream(configFile))) {
                    props.load(in);
                } catch (Exception e) {
                    throw e;
                }
            }
            if (topicSub != null && !topicSub.equals("")) {
                this.topicSub = topicSub;
            }
            L.info("update topicSub to: {}", this.topicSub);

            this.isResend = isResend;
            producer = new KafkaProducer<String, JSONObject>(props);
            isInit = true;
        }
        L.info("Producer init end.");
    }

    /**
     * 指定的消息类型存储到Kafaka的参数，不清楚参数，请不要随意设置。不设置会使用Kafaka默认的参数.
     * @param msgType 消息类型
     * @param zk zookeeper域名
     * @param partitionNum 分区数
     * @param replicationNum 复制数
     */
    public void setMsgTypeParam(String msgType, String zk, int partitionNum, int replicationNum) {
        if (!isInit) {
            L.error("");
            return;
        }

        String topic = null;
        if (msgType.endsWith(topicSub)) {
            topic = msgType;
        } else {
            topic = getTopic(msgType);
        }
        L.info("set msgType [{}] param: {}, {}, {}, {}", msgType, topic, zk, partitionNum, replicationNum);
        topicMap.put(topic, new TopicCommond(topic, zk, partitionNum, replicationNum));
    }

    /**
     * 根据消息类型生成topic.
     * @param msgType the msg type
     * @return the topic
     */
    String getTopic(String msgType) {
        return String.format("%s-%s", msgType, topicSub);
    }

    /**
     * 发送数据.
     * @param data Map格式的数据
     * @return true, if successful
     */
    public boolean sendData(Map<String, Object> data) {
        return sendData(new JSONObject(data));
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
     * @param key 计算分区的key
     * @param data Map格式的数据
     * @return true, if successful
     */
    public boolean sendData(String key, Map<String, Object> data) {
        return sendData(key, new JSONObject(data));
    }

    /**
     * 发送数据.
     * @param data the data
     * @return true, if successful
     */
    public boolean sendData(String key, JSONObject data) {
        // 增加方法调用次数
        ProducerState.addCallCount();

        // 仅处理有msg_type字段的数据
        if (!data.containsKey(MSG_TYPE)) {
            L.warn("msg_type is null, skip!!!");
            ProducerState.addSkipCount();
            return false;
        } else {
            String msgType = data.getString(MSG_TYPE);
            if (msgType == null || msgType.trim().equals("")) {
                L.warn("msg_type is null, skip!!!");
                ProducerState.addSkipCount();
                return false;
            }
        }

        // 生成回调类
        ProducerCallback callback = null;
        if (isResend) {
            callback = new ProducerCallback(this, data);
        }

        try {
            // 发送数据
            return sendData(key, data, callback);
        } catch (Exception e) {
            // 发送异常调用回调
            if (isResend) {
                callback.onCompletion(null, e);
            } else {
                L.error(e.getMessage(), e);
            }
        }

        return false;
    }

    /**
     * 发送数据.
     * @param record 数据
     * @param callback 回调
     * @return true, if successful
     * @throws Exception 异常
     */
    boolean sendData(JSONObject record, Callback callback) throws Exception {
        return sendData(null, null, record, callback);
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
        return sendData(null, key, record, callback);
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
            L.warn("init Producer on send data!!!");
            init(topicSub, false);
        }

        if (producer == null) {
            L.error("Producer is null, skip!!!");
            ProducerState.addSkipCount();
            return false;
        }

        // 如果未设置主题，则根据msg_type字段获取主题
        if (topic == null || topic.equals("")) {
            topic = getTopic(record.getString(MSG_TYPE));
            ProducerState.addTopicCount(topic);
        }

        if (topicMap.containsKey(topic)) {
            topicMap.get(topic).createTopic();
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
            L.info("Producer close...");
            producer.close();
            isInit = false;
            L.info("Producer close end.");
        }
    }
}
