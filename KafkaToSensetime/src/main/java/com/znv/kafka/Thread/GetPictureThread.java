package com.znv.kafka.Thread;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.znv.util.FeatureUtil;
import com.znv.util.PropertiesUtil;
import com.znv.kafka.consumer.CousumerBase;
import com.znv.kafka.producer.ProducerBase;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 单线程启动kafka消费者
 */
public class GetPictureThread implements Runnable {

    private String ip;
    private String port;
    private ProducerBase producer;

    public GetPictureThread(){
        Properties props= PropertiesUtil.getProperties("fss.properties");
        ip=props.getProperty("fss.server");
        port=props.getProperty("fss.port");
        if(producer==null){
            producer=new ProducerBase();
            producer.initWithConfig("producer.properties");
        }
    }
    public void run(){
        CousumerBase consumer=new CousumerBase();
        while(true){
            JSONArray pictureArray=consumer.getJsonResult();
            if(pictureArray.size()>0){
                for(int i=0;i<pictureArray.size();i++){
                    JSONObject obj = pictureArray.getJSONObject(i);
                    String picData=obj.getString("pic_data");
                    String userData=obj.getString("user_data");
                    String feature =FeatureUtil.getImageFeature(ip,port,picData);
                    String detectData =FeatureUtil.getImageDetectAndQuality(ip,port,picData);
                    Map<String,Object> sendData=new HashMap<>();
                    sendData.put("feature",feature);
                    sendData.put("user_data",userData);
                    sendData.put("detect_data",detectData);
                    producer.sendData(sendData);
                }
            }
        }
    }
}
