
/**
 * Created by ct on 2016-05-24.
 */

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.apache.avro.Schema;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.util.Utf8;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConsumerJsonTest {
    private final ConsumerConnector consumer;
    private final String topic;

    public ConsumerJsonTest(String zookeeper, String groupId, String topic) {
        Properties props = new Properties();
        props.put("zookeeper.connect", zookeeper);
        props.put("group.id", groupId);
        props.put("zookeeper.session.timeout.ms", "500");
        props.put("zookeeper.sync.time.ms", "250");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.offset.reset", "smallest");
        consumer = Consumer.createJavaConsumerConnector(new ConsumerConfig(props));
        this.topic = topic;
    }

    public void testConsumer() {
        Schema schema = new Schema.Parser().parse(
            "{\"type\":\"map\", \"values\": [\"null\", \"int\", \"long\", \"float\", \"double\", \"string\", \"boolean\", \"bytes\"]}");

        Map<String, Integer> topicCount = new HashMap<String, Integer>();
        // Define single thread for topic
        topicCount.put(topic, new Integer(1));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerStreams = consumer.createMessageStreams(topicCount);
        List<KafkaStream<byte[], byte[]>> streams = consumerStreams.get(topic);
        System.out.println("iCap kafka.consumer");
        int count = 0;
        for (final KafkaStream stream : streams) {
            ConsumerIterator<byte[], byte[]> consumerIte = stream.iterator();
            while (consumerIte.hasNext()) {
                try {
                    SpecificDatumReader<Map<String, Object>> reader = new SpecificDatumReader<Map<String, Object>>(
                        schema);
                    Decoder decoder = DecoderFactory.get().binaryDecoder(consumerIte.next().message(), null);
                    Map<String, Object> map = reader.read(null, decoder);

                    String msgType = map.get(new Utf8("msg_type")).toString();
                    // System.out.println("dataType :" + dataType);

                    if (msgType.equals("alarm")) {
                        System.out.println("Alarm message");
                        for (Object key : map.keySet()) {
                            Object value = map.get(key);
                            // System.out.format("%s=%s(%s)\n", key, value, value != null ? value.getClass() : "null");
                            System.out.format("%s=%s ", key, value);
                        }
                    } else if (msgType.equals("mete")) {
                        System.out.println("meteData message");
                        for (Object key : map.keySet()) {
                            Object value = map.get(key);
                            // System.out.format("%s=%s(%s)\n", key, value, value != null ? value.getClass() : "null");
                            System.out.format("%s=%s ", key, value);
                            // if (key.toString().equals("station_name"))
                            // {
                            // System.out.println(value);
                            // }
                        }
                    } else if (msgType.equals("exception")) {
                        System.out.println("exception message");
                        for (Object key : map.keySet()) {
                            Object value = map.get(key);
                            // System.out.format("%s=%s(%s)\n", key, value, value != null ? value.getClass() : "null");
                            System.out.format("%s=%s ", key, value);
                        }
                    } else {
                        System.out.println("message error");
                    }
                    count++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (consumer != null)
            consumer.shutdown();
    }

    public static void main(String[] args) {
        String topic = "AlarmJsonStage2-ct";// icap_exceptionAlarm icap_meteData icap_devAlarm ICAP_Mete-20160919
                                            // ICAP_Mete-20160915
        ConsumerJsonTest Consumer = new ConsumerJsonTest("10.45.149.75:2181", "cttestgroup-4", topic);// 10.45.149.103:2181
                                                                                                      // 10.45.149.252:2181
                                                                                                      // cttestgroup-2
        Consumer.testConsumer();
    }
}