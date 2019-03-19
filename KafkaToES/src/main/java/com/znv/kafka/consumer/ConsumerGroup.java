package com.znv.kafka.consumer;

import java.util.ArrayList;
import java.util.List;

public class ConsumerGroup {

    private List<ConsumerRunnable> consumers;
    private int consumerNum;

    public ConsumerGroup(List<String> seedBrokers, int port,String topic) {
        //consumerNum= TopicUtil.getTopicPartitionNum(seedBrokers,port,topic);
        consumerNum= 1;
        consumers = new ArrayList<>(consumerNum);
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