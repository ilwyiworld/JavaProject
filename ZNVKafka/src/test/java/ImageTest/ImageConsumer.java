package ImageTest;

import org.apache.avro.util.Utf8;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.BufferedOutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

/**
 * Created by ct on 2016-10-11.
 */
public class ImageConsumer {

    private KafkaConsumer<String, Map<String, Object>> consumer;
    private ArrayList<String> topic;

    public ImageConsumer(String brokers, String groupId, ArrayList<String> topic) {
        Properties props = new Properties();
        props.put("bootstrap.servers", brokers);
        props.put("group.id", groupId);
        props.put("auto.commit.interval.ms", "5000");
        props.put("auto.offset.reset", "earliest");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "com.znv.iCapProducerApi.common.KafkaAvroDeSerializer");
        consumer = new KafkaConsumer(props);
        this.topic = topic;
    }

    public void consumer() {
        consumer.subscribe(this.topic);
        System.out.println("ImageTest.ImageConsumer");
        Path path = Paths.get("E:\\IdeaProject\\iCapSDK\\consumer");
        String currentKey = "";
        BufferedOutputStream out = null;
        try {
            while (true) {
                int count = 0;
                ConsumerRecords<String, Map<String, Object>> records = consumer.poll(1000);
                for (ConsumerRecord<String, Map<String, Object>> record : records) {
                    Path filePath = Paths.get(path.toString() + "\\" + record.key()); // + (".jpg"));
                    // System.out.println("filepath " + filePath.toString());

                    count++;
                    // System.out.printf("[%d]---%s\n",count,record.key().toString());
                    if (!record.key().equals(currentKey)) {
                        if (out != null) {
                            out.close();
                        }
                        currentKey = record.key();
                        out = new BufferedOutputStream(
                            Files.newOutputStream(filePath, StandardOpenOption.APPEND, StandardOpenOption.CREATE));
                    }
                    Map<String, Object> map = record.value();
                    Object data = map.get(new Utf8("data"));
                    String type = map.get(new Utf8("type")).toString();
                    // System.out.println(map);

                    if (data instanceof ByteBuffer) {
                        out.write(((ByteBuffer) data).array());
                        out.flush();
                        if (type.equals("true")) {
                            System.out.printf("%s count = %d\n", record.key().toString(), count);
                            out.close();
                            count = 0;
                        }
                    }
                    // System.out.println(record.value());
                    // System.out.printf("topic:%s partition:%d
                    // offset:%d\n",record.topic(),record.partition(),record.offset());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String topic1 = "image-production";// icap_exceptionAlarm icap_meteData icap_devAlarm ICAP_Mete-20160919
                                           // ICAP_Mete-20160915
        // String topic2 = "MeteDataJsonStage2-ct";

        ArrayList<String> topic = new ArrayList<String>();
        topic.add(topic1);
        // topic.add(topic2);

        ImageConsumer Consumer = new ImageConsumer("10.45.157.96:6667", "cttestgroup", topic);// 10.45.149.103:2181
                                                                                              // 10.45.149.252:2181
                                                                                              // cttestgroup-2
        Consumer.consumer();
    }
}
