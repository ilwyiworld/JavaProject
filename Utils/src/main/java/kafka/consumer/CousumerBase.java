package kafka.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import kafka.producer.PropertiesUtil;
import kafka.producer.TopicConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

public class CousumerBase {
    private KafkaConsumer<String, String> consumer;
    private ArrayList<String> topics;
    private List<receiveData> picturesDatas = new CopyOnWriteArrayList();
    //private boolean beChanged = false;
    private int tmpIdx = 0;
    private Properties props;

    private final Logger logger = LogManager.getLogger(CousumerBase.class);

    public CousumerBase() {
        if (props == null) {
            props = PropertiesUtil.getProperties("consumer.properties");
        }
        consumer = new KafkaConsumer(props);
        logger.info(String.format("Create kafka.consumer, groupid:%s", new Object[]{props.getProperty("group.id")}));
        topics = TopicConfig.getTopicList("receive.topics");
    }

    public void startConsumer() throws Exception {
        try {
            this.consumer.subscribe(this.topics);
            /*if (this.beChanged) {
                this.picturesDatas.clear();
                this.kafka.consumer.seekToBeginning(new TopicPartition[0]);
                this.beChanged = false;
            }*/
            ConsumerRecords<String, String> records = this.consumer.poll(1L);
            for (ConsumerRecord record:records) {
                String value = (String) record.value();
                receiveData reData = new receiveData();
                JSONObject obj= JSON.parseObject(value);
                reData.setUserData(obj.getJSONObject("user_data").toJSONString());
                reData.setPicData(obj.getString("pic_data"));
                this.picturesDatas.add(reData);
            }
            consumer.commitSync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopConsumer() {
        this.logger.info(String.format("stop kafka.consumer, groupid:%5d", new Object[]{Integer.valueOf(this.tmpIdx)}));
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