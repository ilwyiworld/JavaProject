import org.apache.avro.util.Utf8;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * Created by ct on 2016-09-23.
 */
public class NewConsumer {

    private KafkaConsumer<String, Map<String, Object>> consumer;
    // private KafkaConsumer<Integer, String> kafka.consumer;
    private ArrayList<String> topic;

    Logger log = LoggerFactory.getLogger(NewConsumer.class);

    public NewConsumer(String brokers, String groupId, ArrayList<String> topic) {
        Properties props = new Properties();
        props.put("bootstrap.servers", brokers);
        props.put("group.id", groupId);
        props.put("auto.commit.interval.ms", "5000");
        props.put("auto.offset.reset", "earliest");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.IntegerDeserializer");
        props.put("value.deserializer", "com.znv.iCapProducerApi.common.KafkaAvroDeSerializer");
        // kafka.consumer = new KafkaConsumer<Integer, String>(props);
        // props.put("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        consumer = new KafkaConsumer<String, Map<String, Object>>(props);
        this.topic = topic;
    }

    public void testConsumer() throws Exception {
        // file operation
        File alarm = new File("alarm.txt");
        // FileWriter fwalarm = new FileWriter(alarm);
        FileOutputStream fouts = new FileOutputStream(alarm);
        OutputStreamWriter opsw = new OutputStreamWriter(fouts, "GBK");

        File info4 = new File("info4.txt");
        FileWriter fwinfo = new FileWriter(info4);

        try (BufferedWriter bwalarm = new BufferedWriter(opsw); BufferedWriter bwinfo = new BufferedWriter(fwinfo)) {
            consumer.subscribe(this.topic);
            System.out.println("NewConsumer");
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date1 = df.parse("2016-08-29 20:01:59");
            long cout = 0;
            long sum = 0;
            BASE64Decoder base64Decoder = new BASE64Decoder();

            while (true) {
                ConsumerRecords<String, Map<String, Object>> records = consumer.poll(1000);
                // ConsumerRecords<Integer, String> records = kafka.consumer.poll(1000);
                for (ConsumerRecord<String, Map<String, Object>> record : records) {

                    // for (ConsumerRecord<Integer, String> record : records) {
                    // System.out.println(record.key());
                    Map<String, Object> value = record.value();
                    // System.out.println(value);
                    // Object feature = value.get(new Utf8("feature"));
                    // byte[] byteFeature = ((ByteBuffer) feature).array();
                    // System.out.println("======feature.length: " + byteFeature.length);

                    String fileName = "kafka.consumer/" + value.get(new Utf8("resultIdx")).toString();
                    File file = new File(fileName + ".jpg");
                    OutputStream out = new FileOutputStream(file);

                    String image = value.get(new Utf8("imageData")).toString();
                    // byte[] btyeImage = ((ByteBuffer) image).array();
                    byte[] btyeImage = base64Decoder.decodeBuffer(image);

                    out.write(btyeImage);
                    out.flush();
                    out.close();
                    cout++;
                    if (cout == 100) {
                        break;
                    }

                    // String value = record.value();
                    // System.out.println(value.toString());
                    // System.out.printf("key: %s, value:%s\n",record.key().toString(),value.toString());
                    // System.out.printf("%s\n",value);
                    // System.out.printf("key: %s ,value: %s\n",record.key().toString(),value);
                    // String time = value.get(new Utf8("OccurTime")).toString();
                    // String mete_id = value.get(new Utf8("MeteId")).toString();
                    // String dev_id = value.get(new Utf8("DevId")).toString();
                    // String meteType = value.get(new Utf8("mete_type")).toString();
                    // Date date2 = df.parse(time);
                    // if (cout == 0){
                    // date1 = date2;
                    // cout = 1;
                    // }

                    // String station_id = value.get(new Utf8("station_id")).toString();
                    // String device_type = value.get(new Utf8("device_type")).toString();
                    // String mete_type = value.get(new Utf8("mete_type")).toString();
                    // String alarm_level = value.get(new Utf8("alarm_level")).toString();
                    // String alarm_id = value.get(new Utf8("alarm_id")).toString();
                    // String alarm_time = value.get(new Utf8("alarm_time")).toString().replace(" ","");
                    //// bwalarm.write(record.value().toString());
                    //// bwalarm.newLine();
                    //
                    // String infoall = "station_id" + station_id + "device_type" + device_type + "mete_type" +
                    // mete_type
                    // + "alarm_level" + alarm_level + "alarm_time" + alarm_time + "alarm_id" + alarm_id;
                    // bwinfo.write(infoall);
                    // bwinfo.write(",");
                    // cout++;
                    // sum++;
                    // if (cout > 100){
                    // bwinfo.newLine();
                    // cout = 0;
                    // }
                    // System.out.println("sum: " + sum);

                    // if (date1.getTime() >= dateNew.getTime())
                    /*
                     * if(meteType.equals("39")) {
                     * System.out.printf("[count= %d][offset= %d][partition= %d] %s\n",cout,record.offset(),record.
                     * partition(),record.value().toString()); bw.write("[" + cout + "]" + "[" + record.offset() + "]" +
                     * "[" + record.partition() + "]" + record.value().toString()); bw.newLine(); cout++; //
                     * System.out.printf("[%d][%d][offset=%d] OccurTime= %s\n",cout,record.partition(),record.offset(),
                     * time);
                     * //System.out.printf("topic:%s partition:%d offset:%d\n",record.topic(),record.partition(),record.
                     * offset()); }
                     */
                    // if (date1.getTime() - date2.getTime() > 60*60*1000){
                    // System.out.println("!!!!!!time error!!!!!");
                    // System.out.println("date1:" + df.format(date1) + " date2:" + df.format(date2));
                    // }
                    // //System.out.println("date1:" + df.format(date1) + " date2:" + df.format(date2));
                    // date1 = date2;

                }
            }
        } catch (Exception e) {
            // System.out.println(e.getMessage());
            e.printStackTrace();

        } finally {
            // bw.close();
        }

    }

    public static void main(String[] args) {
        try {
            // FSSAnalysis-six-production fss_image-ct-1229-image
            String topic1 = "FSSAnalysis-ct-production";// ICAP-vehicle_fact-20161011 ICAP-vehicle_break_rule-20161011
            // String topic2 = "MeteDataJsonStage2-ct";

            ArrayList<String> topic = new ArrayList<String>();
            topic.add(topic1);
            // topic.add(topic2);

            NewConsumer Consumer = new NewConsumer("10.45.157.94:6667", "cttestgroup2", topic);// 10.45.149.103:2181
                                                                                               // 10.45.149.252:2181
                                                                                               // cttestgroup-2
            Consumer.testConsumer();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

}
