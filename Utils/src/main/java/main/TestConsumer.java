package main;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;

/**
 * Created by Administrator on 2018/9/28.
 */
public class TestConsumer {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "10.45.157.104:9092");
        props.put("group.id", "mygroupwewew");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.offset.reset", "earliest");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<String, String>(props);
        kafkaConsumer.subscribe(Arrays.asList("peim-mete-4-production"));

        while(true) {
            System.out.println("nothing available...");
            ConsumerRecords<String, String> records = kafkaConsumer.poll(100);
            for(ConsumerRecord<String, String> record : records) {
                System.out.printf("offset = %d, value = %s", record.offset(), record.value());
                System.out.println();
            }
        }
    }
}
