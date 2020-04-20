package com.znv.kafka.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.znv.kafka.producer.ProducerBase;
import com.znv.util.*;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;

public class ConsumerRunnable implements Runnable {

    private final Logger logger = LogManager.getLogger(CousumerBase.class);
    private ArrayList<String> topics;
    private Properties props;
    private String ip;
    private String port;
    private ProducerBase producer;
    private float threshold;
    //比较时间间隔
    private long maxCompareSeconds;
    //处理数据延迟时间
    private long sleepTimeout;
    //kafka拉取超时时间
    private long timeout;
    // 每个线程维护私有的KafkaConsumer实例
    private final KafkaConsumer<String, String> consumer;
    // 保存上一批发送到kafka的数据
    //private final ArrayList<Map<String,Object>> oldList=new ArrayList<>();
    // 最新一批的数据
    //private final ArrayList<Map<String,Object>> newList=new ArrayList<>();

    public ConsumerRunnable() {
        if (props == null) {
            props = PropertiesUtil.getProperties("consumer.properties");
        }
        consumer = new KafkaConsumer(props);
        topics = TopicUtil.getTopicList("receive.topics");
        threshold=Float.parseFloat(PropertiesUtil.getProperties("fss.properties").getProperty("fss.threshold"));
        maxCompareSeconds=Long.parseLong(PropertiesUtil.getProperties("fss.properties").getProperty("fss.maxCompareSeconds"));
        sleepTimeout=Long.parseLong(PropertiesUtil.getProperties("fss.properties").getProperty("fss.sleepTimeout"));
        logger.info(String.format("Create kafka.consumer, groupid:%s", new Object[]{props.getProperty("group.id")}));
        consumer.subscribe(topics);   // 本例使用分区副本自动分配策略
        timeout=Long.parseLong(PropertiesUtil.getProperties("fss.properties").getProperty("fss.timeout"));

        Properties productProps= PropertiesUtil.getProperties("fss.properties");
        ip=productProps.getProperty("fss.server");
        port=productProps.getProperty("fss.port");
        if(producer==null){
            producer=new ProducerBase();
            producer.initWithConfig("producer.properties");
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(sleepTimeout);
                ConsumerRecords<String, String> records = consumer.poll(timeout);
                for (ConsumerRecord<String, String> record : records) {
                    try{
                        String value = record.value();
                        JSONObject obj = JSON.parseObject(value);
                        //logger.warn("接受到的数据："+obj.getString("user_data"));
                        String feature = FeatureUtil.getImageFeature(ip,port,obj.getString("pic_data"));
                        String detectData =FeatureUtil.getImageDetectAndQuality(ip,port,obj.getString("pic_data"));
                        Map<String,Object> sendData=new HashMap<>();
                        sendData.put("feature",feature);
                        sendData.put("user_data",obj.getString("user_data"));
                        sendData.put("event_data",obj.getString("event_data"));
                        sendData.put("detect_data",detectData);
                        String time=JSON.parseObject(obj.getString("event_data")).getString("event_time");
                        String pics=obj.getString("pic_data");
                        Base64Util.generateImage("before",pics,time);
                        if(ConsumerGroup.picQueue.isEmpty()){
                            if(!StringUtils.isEmpty((String)sendData.get("feature"))){
                                //包含特征值
                                //图片队列为空，直接插入
                                producer.sendData(sendData);
                                Base64Util.generateImage("after",pics,time);
                                ConsumerGroup.picQueue.add(sendData);
                            }
                        }else{
                            //与图片队列中的数据比较
                            boolean isRepeatFace=false;
                            Iterator<Map<String,Object>> picIteas=ConsumerGroup.picQueue.iterator();
                            while(picIteas.hasNext()){
                                //与人脸队列中所有人脸依次进行比较
                                Map<String,Object> pic =picIteas.next();
                                //当前图片时间
                                String currentPicTime=JSON.parseObject(obj.getString("event_data")).getString("event_time");
                                //遍历到的图片的时间
                                String comparePicTime=JSON.parseObject((String)pic.get("event_data")).getString("event_time");

                                if(Math.abs(TimeCompareUtil.compareTime(currentPicTime,comparePicTime))>maxCompareSeconds) {
                                    //遍历到的时间与当前图片时间间隔超过规定时间，将队列中该图片删除，也不需要比对
                                    //时间先后不确定
                                    //ConsumerGroup.picQueue.remove(pic);
                                }else{
                                    if(StringUtils.isEmpty((String)sendData.get("feature"))){
                                        //特征值为空 直接跳过 不用发送到kafka
                                        isRepeatFace=true;
                                        logger.error("比对的图片特征值为空");
                                        //生成特征值为空的图片
                                        Base64Util.generateImage("error",pics,time);
                                    }else{
                                        float sim=0L;
                                        try{
                                            sim=FeatureUtil.Comp((String)pic.get("feature"), (String)sendData.get("feature"));
                                            logger.warn("比对结果："+currentPicTime+":"+comparePicTime+"..."+sim);
                                            if(sim >= threshold){
                                                //重复图片
                                                isRepeatFace=true;
                                                Base64Util.generateImage("repeat",pics,time);
                                                break;
                                            }
                                        }catch(Exception e){
                                            logger.error("特征值比对失败",e);
                                            //生成比对失败的图片
                                            Base64Util.generateImage("error",pics,time);
                                        }
                                    }
                                }
                            }
                            if(!isRepeatFace){
                                //不重复则发送，且保存在picQueue中
                                producer.sendData(sendData);
                                Base64Util.generateImage("after",pics,time);
                                if(ConsumerGroup.picQueue.size()>=ConsumerGroup.maxComparePicNums){
                                    //人脸图片队列已满，先进先出，插到尾部
                                    ConsumerGroup.picQueue.offer(sendData);
                                    ConsumerGroup.picQueue.poll();
                                }else{
                                    ConsumerGroup.picQueue.offer(sendData);
                                }
                            }
                        }
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                consumer.commitSync();
/*              //单线程去重
                boolean isFirstData=false;
                ConsumerRecords<String, String> records = kafka.consumer.poll(timeout);   // 本例使用1000ms作为获取超时时间
                if(oldList.size()==0){
                    isFirstData=true;
                }
                for (ConsumerRecord<String, String> record : records) {
                    try{
                        String value = record.value();
                        logger.warn("接受到的数据："+value);
                        JSONObject obj = JSON.parseObject(value);
                        String feature = FeatureUtil.getImageFeature(ip,port,obj.getString("pic_data"));
                        String detectData =FeatureUtil.getImageDetectAndQuality(ip,port,obj.getString("pic_data"));
                        Map<String,Object> sendData=new HashMap<>();
                        sendData.put("feature",feature);
                        sendData.put("user_data",obj.getString("user_data"));
                        sendData.put("detect_data",detectData);
                        if(isFirstData){
                            //第一批数据 不用比较
                            kafka.producer.sendData(sendData);
                            newList.add(sendData);
                        }else{
                            //与上一批数据比较
                            boolean isRepeatFace=false;
                            for(Map<String,Object> lastData :oldList){
                                //与上一批所有人脸依次进行比较
                                float sim=FeatureUtil.Comp((String)lastData.get("feature"), (String)sendData.get("feature"));
                                if(sim >= threshold){
                                    //出现重复
                                    isRepeatFace=true;
                                    break;
                                }
                            }
                            if(!isRepeatFace){
                                //不重复则发送，而且保存在newList中
                                kafka.producer.sendData(sendData);
                                newList.add(sendData);
                            }
                        }
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                //最后将发送到kafka的数据更新到上一批oldList中
                oldList.clear();
                for(Map<String,Object> sendData:newList){
                    oldList.add(sendData);
                }
                kafka.consumer.commitSync();
*/
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}