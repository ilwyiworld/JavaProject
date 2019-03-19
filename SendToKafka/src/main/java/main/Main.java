package main;

import com.alibaba.fastjson.JSONObject;
import producer.FeatureUtil;
import producer.PictureUtil;
import producer.ProducerBase;
import producer.PropertiesUtil;

import java.util.Iterator;
import java.util.Properties;

/**
 * Created by Administrator on 2018/2/27.
 */
public class Main {
    public static void main(String[] args) throws Exception{
        ProducerBase producer = new ProducerBase();
        producer.initWithConfig("producer.properties");
        Properties prop= PropertiesUtil.getProperties("pic.properties");
        String ip=PropertiesUtil.getProperties("fss.properties").getProperty("fss.server");
        String port=PropertiesUtil.getProperties("fss.properties").getProperty("fss.port");

        if(prop == null)return;
        JSONObject obj=new JSONObject();
        Iterator keyIt = prop.keySet().iterator();
        while(keyIt.hasNext()){
            try{
                String key = (String)keyIt.next();
                String value = prop.getProperty(key);
                if(key.equals("pic_addr")){
                    String picBase64= PictureUtil.getImgStr(value);
                    String feature= FeatureUtil.getImageFeature(ip,port,picBase64);
                    obj.put("feature",feature);
                }else{
                    obj.put(key,value);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        producer.sendData(obj.toJSONString());
    }
}
