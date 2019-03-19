package com.znv.kafka;

//import com.znv.kafka.Thread.GetPictureThread;
import com.znv.util.PropertiesUtil;
import com.znv.util.TopicUtil;
import com.znv.kafka.consumer.ConsumerGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/3/15.
 */
public class Main {
    public static void main(String[] args) {
        /*Thread thread = new Thread(new GetPictureThread());
        thread.start();*/

        String topics = TopicUtil.getTopicList("receive.topics").get(0);
        String serverPort= PropertiesUtil.getProperties("consumer.properties").getProperty("bootstrap.servers");
        int port=Integer.parseInt(serverPort.split(":")[1]);
        List<String> seedBrokers=new ArrayList<>();
        seedBrokers.add((serverPort.split(":")[0]));
        //多线程启动kafka消费者
        ConsumerGroup consumerGroup = new ConsumerGroup(seedBrokers,port,topics);
        consumerGroup.execute();
    }
}
