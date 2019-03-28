package com.znv.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.znv.util.ConnectionPool;
import com.znv.util.PropertiesUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
/**
 * Created by Administrator on 2017/7/12.
 */
public class initConnectionServlet extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(initConnectionServlet.class.getName());
    public static String[] doorDeviceId=null;
    public static String[] captureDeviceId=null;
    public static ConnectionPool pool=null;
    private static Connection conn=null;
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String hostUrl="";
        try{
            hostUrl= PropertiesUtil.getProperties("conf.properties").getProperty("hostUrl");
            //EsManager.initConnection(hostUrl);
            doorDeviceId=PropertiesUtil.getProperties("type.properties").getProperty("door.id").split(",");
            captureDeviceId=PropertiesUtil.getProperties("type.properties").getProperty("capture.id").split(",");
            LOGGER.info("初始化es成功");
            String driverClass=PropertiesUtil.getProperties("conf.properties").getProperty("driverClass");
            String dbUrl=PropertiesUtil.getProperties("conf.properties").getProperty("dbUrl");
            String dbPassword=PropertiesUtil.getProperties("conf.properties").getProperty("dbPassword");
            String dbUsername=PropertiesUtil.getProperties("conf.properties").getProperty("dbUsername");
            pool=new ConnectionPool(driverClass,dbUrl,dbUsername,dbPassword);
            //创建连接池
            pool.createPool();
            conn=pool.getConnection();
        }catch (Exception e){

        }
    }

    public static Connection getConn() {
        return conn;
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}