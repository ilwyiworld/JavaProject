package com.znv.kafka.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.znv.util.PropertiesUtil;
import com.znv.util.TopicUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import com.znv.kafka.entity.receiveData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CousumerBase {
    private KafkaConsumer<String, String> consumer;
    private ArrayList<String> topics;
    private List<receiveData> picturesDatas = new CopyOnWriteArrayList();
    private int tmpIdx = 0;
    private Properties props;

    private final Logger logger = LogManager.getLogger(CousumerBase.class);

    public CousumerBase() {
        if (props == null) {
            props = PropertiesUtil.getProperties("consumer.properties");
        }
        consumer = new KafkaConsumer(props);
        logger.info(String.format("Create consumer, groupid:%s", new Object[]{props.getProperty("group.id")}));
        topics = TopicUtil.getTopicList("receive.topics");
    }

    public void startConsumer() throws Exception {
        try {
            this.consumer.subscribe(this.topics);
            ConsumerRecords<String, String> records = this.consumer.poll(1L);
            for (ConsumerRecord record:records) {
                String value = (String) record.value();
                receiveData reData = new receiveData();
                JSONObject obj= JSON.parseObject(value);
                reData.setUserData(obj.getString("user_data"));
                reData.setPicData(obj.getString("pic_data"));
                this.picturesDatas.add(reData);
            }
            consumer.commitSync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopConsumer() {
        this.logger.info(String.format("stop consumer, groupid:%5d", new Object[]{Integer.valueOf(this.tmpIdx)}));
        this.consumer.close();
    }

    public JSONArray getJsonResult() {
        try {
            startConsumer();
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONArray resultArray=new JSONArray();
        int resSize=this.picturesDatas.size();
        for(int i=0;i<resSize;i++){
            JSONObject obj=new JSONObject();
            obj.put("pic_data",picturesDatas.get(i).getPicData());
            obj.put("user_data",picturesDatas.get(i).getUserData());
            resultArray.add(obj);
        }
        this.picturesDatas.clear();
        return resultArray;
    }
}