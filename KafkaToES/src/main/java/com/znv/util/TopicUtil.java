package com.znv.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import kafka.javaapi.TopicMetadata;
import kafka.javaapi.TopicMetadataRequest;
import kafka.javaapi.TopicMetadataResponse;
import kafka.javaapi.consumer.SimpleConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TopicUtil {

    private static final Logger LOGGER = LogManager.getLogger(TopicUtil.class);

    public static int getTopicPartitionNum(List<String> seedBrokers, int port, String topic) {
        int topicPartitionNum=0;
        for (String seed : seedBrokers) {
            SimpleConsumer consumer = null;
            try {
                consumer = new SimpleConsumer(seed, port, 100000, 64 * 1024, "0");
                List<String> topics = Collections.singletonList(topic);
                TopicMetadataRequest req = new TopicMetadataRequest(topics);
                TopicMetadataResponse resp = consumer.send(req);

                List<TopicMetadata> metaData = resp.topicsMetadata();
                for (TopicMetadata item : metaData) {
                    topicPartitionNum+=item.partitionsMetadata().size();
                }
            } catch (Exception e) {
                LOGGER.error("Get topic:"+topic+":PartitionNum Error",e.getMessage());
            } finally {
                if (consumer != null)
                    consumer.close();
            }
        }
        return topicPartitionNum;
    }

    public static ArrayList<String> getTopicList(String topicType){
        ArrayList<String> topicList=new ArrayList<>();
        Properties props = PropertiesUtil.getProperties("topics.properties");
        try {
            for (String topic : props.getProperty(topicType).split(",")) {
                topicList.add(topic);
            }
        }catch(Exception e){
            LOGGER.error("读取topic ["+topicType+"] 属性出错", e);
        }
        return topicList;
    }
}
