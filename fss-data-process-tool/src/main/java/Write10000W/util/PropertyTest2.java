package Write10000W.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by User on 2017/7/10.
 */
public class PropertyTest2 {
    // private static String hostURL;
    //private static String adress;

    public static Properties getProperties() {
        Properties prop = new Properties();
        try {
            //读取属性文件a.properties
            InputStream in = Object.class.getResourceAsStream("/init.properties");
            //InputStream in = new BufferedInputStream(new FileInputStream("/home/zhx/init_big.properties"));
            prop.load(in);     ///加载属性列表
            /*hostURL = prop.getProperty("hostUrl");
            adress = prop.getProperty("adress");*/
            //System.out.println(hostURL);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prop;
    }


    public static void main(String[] args) {
        getProperties();
    }
}
