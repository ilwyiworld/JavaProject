package com.znv.fss.ExcelToPhoenix;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.znv.fss.ExcelToPhoenix.constant.Constant;
import com.znv.fss.ExcelToPhoenix.conf.ConfigManager;
import com.znv.kafka.ProducerBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Created by User on 2018/4/3.
 */
public class SendToKafka {
    private static ProducerBase producer = new ProducerBase();
    private static final Logger L = LoggerFactory.getLogger(SendToKafka.class);

    /**
     * 初始化kafka
     */
    public static void initKafka() {
        try {
        	L.info("init kafka...");
            Properties props = ConfigManager.getProducerProps();
            producer.initWithConfig(props);
            producer.setMsgTypeParam(ConfigManager.getString(Constant.NOTIFY_TOPIC_MSGTYPE),
                    ConfigManager.getString(Constant.ZOOKEEPER_ADDR), ConfigManager.getInt(Constant.NOTIFY_PARTITION_NUM),
                    ConfigManager.getInt(Constant.NOTIFY_REPLICATION_NUM));
            L.info("finished init kafka...");
        } catch (Exception e) {
            L.error("init kafka... error ", e);
        }
    }

    /**
     * 发送到kafka
     */
    public static void sendToKafka(String tableName, int primaryId) {
    	L.info("send notify to kafka...");
        JSONObject notifyMsg = new JSONObject();
        notifyMsg.put("msg_type", ConfigManager.getString(Constant.NOTIFY_TOPIC_MSGTYPE));
        notifyMsg.put("table_name", tableName);
        notifyMsg.put("primary_id", primaryId);
        notifyMsg.put("reference_id", null);
        long currentTime = System.currentTimeMillis();
        Date timeDate = new Date(currentTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = sdf.format(timeDate);
        notifyMsg.put("send_time", timeStr);
        boolean ret = producer.sendData(notifyMsg);
        producer.close();
        L.info("\tret: {}, send time:{}", ret, timeStr);
        L.info("\tret msg: {}", JSON.toJSONString(notifyMsg, true));
    }
}
