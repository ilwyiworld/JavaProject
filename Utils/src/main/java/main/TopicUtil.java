package main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Properties;

public class TopicUtil {

    private static final Logger LOGGER = LogManager.getLogger(TopicUtil.class);

    public static ArrayList<String> getTopicList(String topicType){
        ArrayList<String> topicList=new ArrayList<>();
        Properties props = PropertiesUtil.getProperties("topics.properties");
        try {
            for (String topic : props.getProperty(topicType).split(",")) {
                topicList.add(topic);
            }
        }catch(Exception e){
            LOGGER.error("读取topic ["+topicType+"] 属性出错", e);
        }
        return topicList;
    }
}