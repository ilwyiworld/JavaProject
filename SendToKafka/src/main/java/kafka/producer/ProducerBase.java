package kafka.producer;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

/**
 * 生产者.
 */
public class ProducerBase {

    private static final Logger LOGGER = LogManager.getLogger(ProducerBase.class);

    /** 生产者. */
    protected KafkaProducer<String, String> producer = null;

    /** 是否初始化. */
    protected boolean isInit = false;

    /** 是否重发，kafka默认会重发，所以该参数可以为false. */
    private boolean isResend = false;

    private static String topic;

    public static String getTopic(){
        return topic;
    }

    /**
     * Instantiates a new kafka.producer base.
     */
    public ProducerBase(){
        //initWithConfig("kafka.producer.properties");
    }

    public void initWithConfig(String propertiesPath){
        Properties props= PropertiesUtil.getProperties(propertiesPath);
        topic=TopicConfig.getTopicList("send.topics").get(0);
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
            this.topic= TopicConfig.getTopicList("send.topics").get(0);
            this.isResend = isResend;
            producer = new KafkaProducer<String, String>(props);
            isInit = true;
        }
        LOGGER.info("Producer init end.");
    }

    /**
     * 发送数据.
     * @param data the data
     * @return true, if successful
     */
    public boolean sendData(String data) {
        return sendData(null, data);
    }

    /**
     * 发送数据.
     * @param data the data
     * @return true, if successful
     */
    public boolean sendData(String key, String data) {
        // 增加方法调用次数
        ProducerState.addCallCount();

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
    boolean sendData(String key, String record, Callback callback) throws Exception {
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
    boolean sendData(String topic, String key, String record, Callback callback) throws Exception {
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
        ProducerRecord<String, String> data = new ProducerRecord<String, String>(topic, key, record);
        if (callback != null) {
            producer.send(data, callback);
        } else {
            producer.send(data);
        }
        ProducerState.addSendCount();
        return true;
    }

    /*public void flush() {
        kafka.producer.flush();
    }*/

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
