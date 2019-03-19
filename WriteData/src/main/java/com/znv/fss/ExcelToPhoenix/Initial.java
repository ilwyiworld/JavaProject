package com.znv.fss.ExcelToPhoenix;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.znv.fss.ExcelToPhoenix.conf.ConfigManager;
import com.znv.fss.ExcelToPhoenix.constant.Constant;
import com.znv.fss.ExcelToPhoenix.utils.ConnectionPool;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by User on 2017/9/9.
 */
public class Initial {
    private static final Logger L = LoggerFactory.getLogger(Initial.class);
    public static ConnectionPool connectionPool = null;
    public static Map<String, String> xmlMap1 = new HashMap<String, String>();
    public static String tableName = null;
    public static int libId = 0;
    public static int personlibType = 0;
    public static int flag = 0;
    public static String picPath = null;
    public static String xlsxPath = null;
    public static List<Element> elements = null;

    public static void start(String fssPropPath,String kafkaPropPath) throws Exception {
        L.info("#################### batch import start ####################");

        // 加载配置文件
        ConfigManager cm = new ConfigManager();
        cm.init(fssPropPath);
        cm.producerInit(kafkaPropPath);
        tableName = ConfigManager.getTableName(Constant.FSS_PERSONLIST_V113_TABLE_NAME);

        // 创建phoenix连接池
        try {
            String url = "jdbc:phoenix:" + ConfigManager.getString(Constant.ZOOKEEPER_ADDR) + ":/hbase";
            L.info("##### load phoenix: {}", url);
//            if (args.length == 0) {
                connectionPool = new ConnectionPool(ConfigManager.getString(Constant.PHOENIX_DRIVER), url, "", "");
                connectionPool.createPool();
//            }
            L.info("\tload phoenix OK!");
        } catch (Exception e) {
            L.error(e.getMessage(), e);
            return;
        }

        // 解析xml文件
        try {
            SAXReader reader = new SAXReader();
            File file = new File(ConfigManager.getString(Constant.IMPORT_XML_PATH));
            L.info("##### load xml file: {}", file);
            if (!file.exists()) {
                L.error("\tcan't find xml file: {}", file);
                throw new FileNotFoundException(file.toString());
            }

            Document doc = reader.read(file);
            Element root = doc.getRootElement();

//            libId = Integer.parseInt(ConfigManager.getString(Constant.LIBID));
//            personlibType = Integer.parseInt(ConfigManager.getString(Constant.PERSONLIB_TYPE));
//            picPath = ConfigManager.getString(Constant.IMPORT_PIC_PATH);
//            xlsxPath = ConfigManager.getString(Constant.IMPORT_XLSX_PATH);
            libId = Integer.parseInt(root.attributeValue(Constant.LIBID));
            personlibType = Integer.parseInt(root.attributeValue(Constant.PERSONLIB_TYPE));
            flag = Integer.parseInt(root.attributeValue(Constant.FLAG));
            picPath = root.attributeValue(Constant.IMPORT_PIC_PATH);
            xlsxPath = root.attributeValue(Constant.IMPORT_XLSX_PATH);

            L.info("\ttable name: {}", tableName);
            L.info("\tlib id: {}", libId);
            L.info("\tperson lib type: {}", personlibType);
            L.info("\tpic path: {}", picPath);
            if (!new File(picPath).exists()) {
                throw new FileNotFoundException(picPath);
            }
            L.info("\txlsx path: {}", xlsxPath);
            if (!new File(xlsxPath).exists()) {
                throw new FileNotFoundException(xlsxPath);
            }

            // 遍历xml文件
            elements = root.elements("mapping");
            for (Element element : elements) {
                xmlMap1.put(element.attribute("xls_index").getValue(), element.attribute("hcolname").getValue());
            }
            L.info("\tmapping: {}", xmlMap1);

            L.info("\tload xml file OK!");
        } catch (Exception e) {
            L.error(e.getMessage(), e);
            System.exit(1);
        }

        // excel表格读数据写进hbase
        ReadExcelUtilsGet.start();

        //数据写完关闭phoenix连接池
        if (connectionPool != null) {
            try {
                L.info("close connection pool...");
                connectionPool.closeConnectionPool();
            } catch (SQLException e) {
                L.error(e.getMessage(), e);
            }
        }

        // 数据写完通知kafka
        //TODO flag == 1?
        if (ReadExcelUtilsGet.phoenixCount > 0 && personlibType == 1 && flag == 1) {
            L.info("##### notify kafka...");
            SendToKafka.initKafka();
            SendToKafka.sendToKafka(tableName, libId);
        }
//
//        L.info("##### record count: {}, succ count: {}, error count: {}", ReadExcelUtilsGet.recordCount,
//                ReadExcelUtilsGet.succCount.get(), ReadExcelUtilsGet.errorCount.get());
//        L.info("#################### batch import end ####################");
    }
}
