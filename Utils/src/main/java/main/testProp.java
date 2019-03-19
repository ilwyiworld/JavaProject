package main;

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Created by Administrator on 2018-11-15.
 */
public class testProp {
    public static void main(String[] args) {
        testProp ttt = new testProp();
        ttt.getProp();
    }

    public void getProp() {
        try {
            Properties props = new Properties();
            InputStream ins = this.getClass().getResourceAsStream("/fss.properties");
            props.load(ins);
            InputStream ins2 = this.getClass().getResourceAsStream("/fss.properties");
            props.load(ins2);
            ins.close();
            ins2.close();

            Enumeration en = props.propertyNames();
            while (en.hasMoreElements()) {
                String key = (String) en.nextElement();
                String value = props.getProperty(key);
                System.out.println(key + " : " + value);
            }
        } catch (IOException var5) {
            var5.printStackTrace();
        }
    }
}
