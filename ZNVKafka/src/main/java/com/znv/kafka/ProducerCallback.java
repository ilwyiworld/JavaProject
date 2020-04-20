package com.znv.kafka;

import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 小V数据生产者回调.
 */
public class ProducerCallback implements Callback {

    private static final Logger L = LoggerFactory.getLogger(ProducerCallback.class);

    /** 数据发送失败时，默认的重发次数. */
    public static final int MAX_RESEND_COUNT = 10;

    /** 数据发送失败时，默认的重发次数. */
    public static final int DEFAULT_RESEND_COUNT = 1;

    /** 剩余的重发次数，重发1次减1. */
    private int resendTimes = DEFAULT_RESEND_COUNT;

    /** 要发送的记录. */
    private JSONObject record = null;

    /** 生产者. */
    private ProducerBase parentProducer = null;

    /**
     * 构造函数，默认不重发.
     * @param json 要发送的记录
     */
    public ProducerCallback(ProducerBase producer, JSONObject json) {
        this(producer, json, DEFAULT_RESEND_COUNT);
    }

    /**
     * 构造函数.
     * @param producer 生产者实例
     * @param resendCount 重发次数
     * @param json 要发送的记录
     */
    public ProducerCallback(ProducerBase producer, JSONObject json, int resendCount) {
        parentProducer = producer;
        record = json;
        resendTimes = Math.min(resendCount, MAX_RESEND_COUNT);
    }

    /*
     * (non-Javadoc)
     * @see org.apache.kafka.clients.kafka.producer.Callback#onCompletion(org.apache.kafka.clients.kafka.producer.RecordMetadata,
     * java.lang.Exception)
     */
    @Override
    public void onCompletion(RecordMetadata data, Exception ex) {
        // 如果返回消息为空
        if (data == null) {
            if (ex != null) { // 如果有异常消息，打印显示
                L.error(ex.getMessage(), ex);
            }

            // 重发，并且重发次数大于0
            if (resendTimes > 0) {
                try {
                    ProducerState.addResendCount();
                    --resendTimes;
                    // 开始重发
                    parentProducer.sendData(record, this);
                } catch (Exception e) {
                    L.error(e.getMessage(), e);
                }
            } else {
                // 增加发送失败次数
                ProducerState.addFaildCount();
            }
        } else {
            // 增加发送成功次数
            ProducerState.addSuccCount();
        }
    }
}
