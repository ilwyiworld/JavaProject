import com.alibaba.fastjson.JSONObject;
import com.znv.kafka.ProducerBase;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * Created by ct on 2016-08-22.
 */
public class ProducerJsonTest {

    public class PerfCallback implements Callback {
        private long cout = 0;

        public void onCompletion(RecordMetadata metadata, Exception exception) {
            if (metadata != null) {
                cout++;
                System.out.format("[%d] send data ok\n", cout);
            } else {
                System.out.println("Exception occurred during message send");
            }
        }
    }

    public void send() {
        ProducerBase alarmProducer = new ProducerBase();
        ProducerBase meteProducer = new ProducerBase();
        ProducerBase excpProducer = new ProducerBase();

        try {
            alarmProducer.init();
            meteProducer.init();
            excpProducer.init();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 10; i++)// i++
        {
            try {
                String KEY_1 = "data_type";
                String KEY_2 = "precent_id";
                String KEY_3 = "station_id";
                String KEY_4 = "mete_id";
                JSONObject alarm = new JSONObject();
                alarm.put(KEY_1, "alarm");
                alarm.put(KEY_2, "precent_1");
                alarm.put(KEY_3, "station_1");
                alarm.put(KEY_4, "mete_1");

                String KEY_1_1 = "data_type";
                String KEY_2_1 = "precent_id";
                String KEY_3_1 = "station_id";
                String KEY_4_1 = "device_id";
                JSONObject meteData = new JSONObject();
                meteData.put(KEY_1_1, "meteData");
                meteData.put(KEY_2_1, "precent_2");
                meteData.put(KEY_3_1, "station_2");
                meteData.put(KEY_4_1, "device_2");

                // alarmProducer.sendData(alarm,Cb);
                meteProducer.sendData(meteData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            alarmProducer.close();
            // meteProducer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        ProducerJsonTest test = new ProducerJsonTest();
        test.send();
    }
}
