package com.znv.kafka;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * ProducerState
 */
public class ProducerState {
    private static final Logger L = LoggerFactory.getLogger(ProducerState.class);

    /** 统计调用发送方法的次数. */
    private static int callCount = 0;

    /** 统计调用底层发送方法的次数. */
    private static int sendCount = 0;

    /** 统计成功的次数. */
    private static int succCount = 0;

    /** 统计失败的次数. */
    private static int faildCount = 0;

    /** 统计发送数据参数不正确的次数. */
    private static int skipCount = 0;

    /** 统计重发次数. */
    private static int resendCount = 0;

    /** 统计发送的时间. */
    private static long startTime = System.currentTimeMillis();

    private static Map<String, Integer> topicMap = new HashMap<String, Integer>();

    static void addTopicCount(String topic) {
        if (topicMap.containsKey(topic)) {
            topicMap.put(topic, topicMap.get(topic) + 1);
        } else {
            topicMap.put(topic, 1);
        }
    }

    static void addCallCount() {
        ++callCount;
    }

    static void addSendCount() {
        ++sendCount;
    }

    static void addSuccCount() {
        ++succCount;
    }

    static void addFaildCount() {
        ++faildCount;
    }

    static void addSkipCount() {
        ++skipCount;
    }

    static void addResendCount() {
        ++resendCount;
    }

    /**
     * 显示当前的状态
     */
    public static String getState() {
        long times = (System.currentTimeMillis() - startTime) / 1000;
        long hrs = 0, min = 0, sec = 0;
        times = times <= 0 ? 1 : times;
        sec = times;
        if (sec >= 60) {
            min = sec / 60;
            sec = sec % 60;
        }
        if (min > 60) {
            hrs = min / 60;
            min = min % 60;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Producer call count: ").append(callCount);
        sb.append(", send count: ").append(sendCount);
        sb.append(", succ count: ").append(succCount);
        sb.append(", faild count: ").append(faildCount);
        sb.append(", skip count: ").append(skipCount);
        sb.append(", resend count: ").append(resendCount);
        sb.append("\nProducer call time: ").append(hrs).append("h").append(min).append("m").append(sec);
        sb.append("s, speed: ").append(callCount / times).append("'/s");
        sb.append("\nProducer used topic: ").append(JSON.toJSONString(topicMap));
        String ret = sb.toString();
        L.info(ret);
        return ret;
    }
}
