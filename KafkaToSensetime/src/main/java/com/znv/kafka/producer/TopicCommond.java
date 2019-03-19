package com.znv.kafka.producer;

import kafka.admin.AdminUtils;
import kafka.common.TopicExistsException;
import kafka.utils.ZkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by ct on 2016-09-28.
 */
class TopicCommond {
    /** 日志 **/
    private static final Logger L = LoggerFactory.getLogger(TopicCommond.class);

    private final String topic;
    private final String zk;
    private final int partitionNum;
    private final int replicationNum;
    private final Properties topicConfig = new Properties();
    private boolean topicExists = false;

    TopicCommond(String topic, String zk, int partitionNum, int replicationNum) {
        this.topic = topic;
        this.zk = zk;
        this.partitionNum = partitionNum;
        this.replicationNum = replicationNum;
    }

    public void createTopic() throws Exception {
        if (!topicExists) {
            L.info("apply zk: {}, {}, {}, {}", zk, 30000, 30000, false);
            ZkUtils zkUtils = ZkUtils.apply(zk, 30000, 30000, false);
            try {
                if (!AdminUtils.topicExists(zkUtils, this.topic)) {
                    L.info("create topic: {}, {}, {}, {}", topic, partitionNum, replicationNum, topicConfig);
                    AdminUtils.createTopic(zkUtils, topic, partitionNum, replicationNum, topicConfig); // 10版本需要,
                                                                                                       // AdminUtils.createTopic$default$6()
                }
                topicExists = true;
            } catch (TopicExistsException e) {
                L.info("Topic {} already exists.", topic);
            } catch (Exception e) {
                throw e;
            } finally {
                zkUtils.close();
            }
        }
    }
}
