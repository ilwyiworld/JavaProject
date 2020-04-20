package kafka.producer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {

    private static final Logger logger = LogManager.getLogger(PropertiesUtil.class);

    //读取配置文件
    public static Properties getProperties(String propertiesPath){
        Properties props=new Properties();
        ClassLoader classLoader = PropertiesUtil.class.getClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(propertiesPath)) {
        //try (InputStream is=new FileInputStream(new File(System.getProperty("user.dir")+File.separator+"conf"+File.separator+propertiesPath))) {
            props.load(is);
        } catch (IOException e) {
            logger.error("读取 ["+propertiesPath+"] 配置文件出错", e);
        }
        return props;
    }
}
