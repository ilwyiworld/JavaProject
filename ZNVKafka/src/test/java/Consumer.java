import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.avro.util.Utf8;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

/**
 * Created by ct on 2016-09-23.
 */
public class Consumer {

    private KafkaConsumer<String, Map<String, Object>> consumer;
    private ArrayList<String> topic;

    public static void main(String[] args) {
        try {
            ArrayList<String> topic = new ArrayList<String>();
            topic.add("mete-20161106-gf-ok");
            Consumer Consumer = new Consumer("10.45.157.98:6667", "xf-group-2", topic);
            Consumer.findRepeat();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public Consumer(String brokers, String groupId, ArrayList<String> topic) {
        Properties props = new Properties();
        props.put("bootstrap.servers", brokers);
        props.put("group.id", groupId);
        props.put("auto.commit.interval.ms", "5000");
        props.put("auto.offset.reset", "earliest");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "com.znv.iCapProducerApi.common.KafkaAvroDeSerializer");
        consumer = new KafkaConsumer<String, Map<String, Object>>(props);
        this.topic = topic;
    }

    public void filter() throws Exception {
        try {
            consumer.subscribe(this.topic);
            Utf8 meteType = new Utf8("mete_type");
            int filterCount = 0;
            while (true) {
                ConsumerRecords<String, Map<String, Object>> records = consumer.poll(1);
                for (ConsumerRecord<String, Map<String, Object>> record : records) {
                    if (record.value().get(meteType).toString().equals("39")) {
                        ++filterCount;
                        System.out.println(filterCount);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void findRepeat() throws Exception {
        final JSONObject countJson = new JSONObject();
        final JSONObject repJson = new JSONObject();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        if (line.equalsIgnoreCase("show")) {
                            System.out.format("map: %d, rep: %d\n", countJson.size(), repJson.size());
                        } else if (line.equalsIgnoreCase("json")) {
                            System.out.println(JSON.toJSONString(repJson, true));
                        } else if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                            System.exit(0);
                        } else {
                            System.out.println("usage: show, json, exit, quit");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        try {
            String key = null;
            consumer.subscribe(this.topic);
            Utf8 devId = new Utf8("DevId"), meteId = new Utf8("MeteId"), time = new Utf8("OccurTime"),
            meteType = new Utf8("MeteType");
            while (true) {
                ConsumerRecords<String, Map<String, Object>> records = consumer.poll(1);
                for (ConsumerRecord<String, Map<String, Object>> record : records) {
                    JSONObject json = new JSONObject(record.value());
                    key = String.format("%s_%s_%s_%s", json.get(devId), json.get(meteId), json.get(time),
                        json.get(meteType));
                    if (countJson.containsKey(key)) {
                        int val = countJson.getIntValue(key) + 1;
                        countJson.put(key, val);
                        repJson.put(key, val);
                    } else {
                        countJson.put(key, 1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.format("map: %d, rep: %d\n", countJson.size(), repJson.size());
            System.out.println(JSON.toJSONString(repJson, true));
        }
    }
}
