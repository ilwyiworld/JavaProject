package com.znv.kafka.consumer;

import com.znv.util.PropertiesUtil;
import com.znv.util.TopicUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConsumerGroup {

    private List<ConsumerRunnable> consumers;
    private int consumerNum;
    //保存的需要比较的图片数量
    protected static int maxComparePicNums;
    //先进先出队列，保存需要比较的图片
    protected static ConcurrentLinkedQueue<Map<String,Object>> picQueue ;

    public ConsumerGroup(List<String> seedBrokers, int port,String topic) {
        consumerNum= TopicUtil.getTopicPartitionNum(seedBrokers,port,topic);
        //consumerNum= 1;
        consumers = new ArrayList<>(consumerNum);
        picQueue = new ConcurrentLinkedQueue<>();
        maxComparePicNums=Integer.parseInt(PropertiesUtil.getProperties("fss.properties").getProperty("fss.timeout"));
        for (int i = 0; i < consumerNum; ++i) {
            ConsumerRunnable consumerThread = new ConsumerRunnable();
            consumers.add(consumerThread);
        }
    }

    public void execute() {
        for (ConsumerRunnable task : consumers) {
            new Thread(task).start();
        }
    }
}