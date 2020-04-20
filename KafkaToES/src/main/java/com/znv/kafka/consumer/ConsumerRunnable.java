package com.znv.kafka.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.znv.util.HandleDataRunnable;
import com.znv.util.PropertiesUtil;
import com.znv.util.TopicUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;

public class ConsumerRunnable implements Runnable {

    private final Logger logger = LogManager.getLogger(ConsumerRunnable.class);
    private ArrayList<String> topics;
    private Properties props;
    //kafka拉取超时时间
    private long timeout;
    // 每个线程维护私有的KafkaConsumer实例
    private final KafkaConsumer<String, String> consumer;
    private ArrayList<JSONObject> datas;
    private int handleNum;

    public ConsumerRunnable() {
        if (props == null) {
            props = PropertiesUtil.getProperties("consumer.properties");
        }
        consumer = new KafkaConsumer(props);
        topics = TopicUtil.getTopicList("receive.topics");
        logger.info(String.format("Create kafka.consumer, groupid:%s", new Object[]{props.getProperty("group.id")}));
        consumer.subscribe(topics);   // 本例使用分区副本自动分配策略
        timeout=Long.parseLong(PropertiesUtil.getProperties("config.properties").getProperty("kafka.timeout"));
        handleNum=Integer.parseInt(PropertiesUtil.getProperties("config.properties").getProperty("handleNum"));
        datas=new ArrayList<>();
    }

    @Override
    public void run() {
        int dataSum=0;
        while (true) {
            try {
                ConsumerRecords<String, String> records = consumer.poll(timeout);
                for (ConsumerRecord<String, String> record : records) {
                    try{
                        String value = record.value();
                        JSONObject obj = JSON.parseObject(value);
                        datas.add(obj);
                        dataSum++;
                        System.out.println("dataSum:"+dataSum);
                        if(dataSum>=handleNum){
                            //处理数据
                            try{
                                new Thread(new HandleDataRunnable(datas)).start();
                                dataSum=0;
                                datas.clear();
                            }catch(Exception e){
                                logger.error("处理topic数据失败");
                            }
                        }
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                consumer.commitSync();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}