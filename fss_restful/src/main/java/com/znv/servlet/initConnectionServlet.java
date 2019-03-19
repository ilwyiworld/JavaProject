package com.znv.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.znv.fss.phoenix.PhoenixClient;
import com.znv.hbase.HBaseConfig;
import com.znv.utils.HdfsUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Administrator on 2017/7/12.
 */
public class initConnectionServlet extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(initConnectionServlet.class.getName());

    private static PhoenixClient phoenixClient = null;

    private static Properties prop = null;

    public static PhoenixClient getPhoenixClient() {
        return phoenixClient;
    }

    public static Properties getProp() {
        return prop;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // 从环境变量中获取hdfs的url
        String hdfsUrl = System.getenv("HDFS_URLwww");
        if (StringUtils.isNotBlank(hdfsUrl)) {
            LOGGER.info("获取到hdfs的url为：" + hdfsUrl);
            LOGGER.info("初始化hbase开始...");
            try {
                HBaseConfig.initConnection(hdfsUrl);
            } catch (IOException e) {
                LOGGER.error("初始化hbase服务异常:" + e);
            }
            LOGGER.info("初始化hbase结束...");

            prop = HdfsUtil.getPropFromHdfs(hdfsUrl, "/user/fss_V110/config", "fss.properties");
            if (prop.isEmpty()) {
                LOGGER.info("未从hdfs上获取到项目配置文件！！！");
            } else {
                LOGGER.info("获取配置文件信息开始...");

                LOGGER.info("获取Phoenix服务配置文件信息开始...");
                if (!prop.getProperty("phoenix.server").isEmpty() && prop.getProperty("phoenix.port").isEmpty()) {
                    try {
                        phoenixClient = new PhoenixClient(prop.getProperty("phoenix.server"), Integer.parseInt(prop.getProperty("phoenix.port")));
                    } catch (Exception e) {
                        LOGGER.error("请求SDK初始化Phoenix服务异常:" + e);
                    }
                } else {
                    LOGGER.info("获取到的Phoenix服务配置信息为空，Phoenix初始化失败...");
                }
                LOGGER.info("获取Phoenix配置文件信息结束...");

                LOGGER.info("获取配置文件信息结束...");
            }
        } else {
            LOGGER.info("从linux环境变量中未获取到hdfs的url信息，从项目本地配置文件初始化！");
            Properties prop = new Properties();
            ClassLoader classLoader = initConnectionServlet.class.getClassLoader();
            LOGGER.info("获取配置文件信息开始...");
            try (InputStream is = classLoader.getResourceAsStream("fss.properties")) {
                prop.load(is);
            } catch (IOException e) {
                LOGGER.error("读取配置文件出错", e);
            }
            if (!prop.isEmpty()) {
                hdfsUrl = "http://".concat(prop.getProperty("hdfs.namenode.host"))
                        .concat(":").concat("hdfs.namenode.rpc.port");
                LOGGER.info("获取到hdfs的url为：" + hdfsUrl);

                LOGGER.info("初始化hbase开始...");
                try {
                    HBaseConfig.initConnection(hdfsUrl);
                } catch (IOException e) {
                    LOGGER.error("初始化hbase服务异常:" + e);
                }
                LOGGER.info("初始化hbase结束...");
            }
            LOGGER.info("获取配置文件信息开始...");

            LOGGER.info("获取Phoenix服务配置文件信息开始...");
            if (!prop.getProperty("phoenix.server").isEmpty() && prop.getProperty("phoenix.port").isEmpty()) {
                try {
                    phoenixClient = new PhoenixClient(prop.getProperty("phoenix.server"), Integer.parseInt(prop.getProperty("phoenix.port")));
                } catch (Exception e) {
                    LOGGER.error("请求SDK初始化Phoenix服务异常:" + e);
                }
            } else {
                LOGGER.info("获取到的Phoenix服务配置信息为空，Phoenix初始化失败...");
            }
            LOGGER.info("获取Phoenix配置文件信息结束...");

            LOGGER.info("获取配置文件信息结束...");
        }
    }
}