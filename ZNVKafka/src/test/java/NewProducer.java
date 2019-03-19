import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Properties;

/**
 * Created by ct on 2016-11-22.
 */
public class NewProducer {
    Properties props = new Properties();
    KafkaProducer producer = null;

    public NewProducer() {
        props.put("bootstrap.servers", "10.45.157.94:6667");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");// org.apache.kafka.common.serialization.StringSerializer
        producer = new KafkaProducer<String, String>(props);
    }

    public void send() {
        try (BufferedReader br = new BufferedReader(new FileReader(new File("producer.properties")));) {
            String line;
            long count = 0;
            System.out.println("start send msg....");
            while ((line = br.readLine()) != null) {
                ProducerRecord<String, String> record = new ProducerRecord("test-1229-ct", line);// streams-wordcount
                producer.send(record);
                System.out.printf("[%d] send %s\n", count, line);
                count++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        NewProducer test = new NewProducer();
        test.send();
    }
}
