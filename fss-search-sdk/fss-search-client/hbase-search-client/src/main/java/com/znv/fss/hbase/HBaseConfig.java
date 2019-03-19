package com.znv.fss.hbase;

import com.znv.fss.common.VConstants;
import com.znv.fss.common.utils.PropertiesUtil;
import com.znv.hbase.coprocessor.endpoint.ImageSearchImplementation;
import com.znv.hbase.coprocessor.endpoint.StayTimeStatImplementation;
import com.znv.hbase.coprocessor.endpoint.StayTimeStatNewImplementation;
import com.znv.fss.hbase.utils.HbaseConnectionPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.coprocessor.AggregationClient;
import org.apache.hadoop.hbase.client.coprocessor.DoubleColumnInterpreter;
import org.apache.hadoop.hbase.client.coprocessor.LongColumnInterpreter;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by Administrator on 2016/6/14.
 */
public class HBaseConfig {
    private static final Log log = LogFactory.getLog(HBaseConfig.class);
    private static Connection connection = null;
    private static AggregationClient aggregationClient = null;
    private static ExecutorService searchPool = null;
    private static ExecutorService writePool = null;
    private static ExecutorService readPool = null;
    private static Properties prop = new Properties();
    private static HbaseConnectionPool pool = null;
    private static Map<String,float[]> points  = new ConcurrentHashMap<String,float[]>(2);

    public static void initConnection(String hostUrl) throws Exception {
        // 读取HDFS文件得到配置参数和HBase表名
        prop = PropertiesUtil.loadFromHdfs(hostUrl + "/config/fss.properties");
       //prop = PropertiesUtil.loadFromResource("fss.properties");
        Configuration config = HBaseConfiguration.create();
        // 以下配置也可通过classpath目录下的hbase-site.xml和hbase-core.xml文件设置
        // 优先检查hbase-site.xml，如果没有，检查hbase-core.xml文件
        // 与hbase/conf/hbase-site.xml中hbase.zookeeper.quorum配置的值相同
        config.set("hbase.zookeeper.quorum", PropertiesUtil.get(prop, VConstants.ZOOKEEPER_QUORUM));
        // 与hbase/conf/hbase-site.xml中hbase.zookeeper.property.clientPort配置的值相同
        config.set("hbase.zookeeper.property.clientPort", PropertiesUtil.get(prop, VConstants.ZOOKEEPER_CLIENTPORT));
        // 与hbase/conf/hbase-site.xml中zookeeper.znode.parent配置的值相同
        config.set("zookeeper.znode.parent", PropertiesUtil.get(prop, VConstants.HBASE_ZNODE_PARENT));

        config.set("hbase.client.retries.number", PropertiesUtil.get(prop, VConstants.HBASE_CLIENT_RETRIES_NUMBER));
        //#hy, 用于统计时可适当提高超时时间
        config.set("hbase.rpc.timeout", PropertiesUtil.get(prop, VConstants.HBASE_RPC_TIMEOUT));
        config.set("hbase.client.operation.timeout", PropertiesUtil.get(prop, VConstants.HBASE_CLIENT_OPERATION_TIMEOUT));
        config.set("hbase.client.scanner.timeout.period", PropertiesUtil.get(prop, VConstants.HBASE_CLIENT_SCANNER_TIMEOUT));

        connection = ConnectionFactory.createConnection(config);
        aggregationClient = new AggregationClient(config);
        pool = new HbaseConnectionPool(config); //? 是否必要需确认 #hy @20171123

        // 创建线程池，控制并发数
        final String n = Thread.currentThread().getName();
        Properties propSDK = PropertiesUtil.loadFromResource("hbaseSearch.properties");
        searchPool = Executors.newFixedThreadPool(Integer.parseInt(propSDK.getProperty(VConstants.HBASE_SEARCH_POOL_NUM, "20")), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName(n + "-search-" + System.currentTimeMillis());
                return t;
            }
        });
        writePool = Executors.newFixedThreadPool(Integer.parseInt(propSDK.getProperty(VConstants.WRITE_POOL_NUM, "20")), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName(n + "-write-" + System.currentTimeMillis());
                return t;
            }
        });
        readPool = Executors.newFixedThreadPool(Integer.parseInt(propSDK.getProperty(VConstants.READ_POOL_NUM, "20")), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName(n + "-read-" + System.currentTimeMillis());
                return t;
            }
        });

        // 添加协处理器判断
        checkOrAddCoprocessors();

        // [lq-add] 2018-05-21 sensetime points read from hdfs,Normalize/reversalNormalize do in SDK
        points = PropertiesUtil.getFeaturePoints(prop);
    }

    private static void checkOrAddCoprocessors() throws IOException {
        // 以图搜图报表
        String historyTablename = PropertiesUtil.get(prop, VConstants.FSS_PHOENIX_SCHEMA_NAME) + ":" + PropertiesUtil.get(prop, VConstants.FSS_HISTORY_V113_TABLE_NAME);
        addCoprocessor(ImageSearchImplementation.class, historyTablename);
        addCoprocessor(StayTimeStatImplementation.class, historyTablename);
        addCoprocessor(StayTimeStatNewImplementation.class, historyTablename);
    }

    /*
     * @param coprocessorName coprocessor class, sunch as AggregateImplementation.class
     * @param tableName
     */
    private static void addCoprocessor(Class coprocessorClass, String tablename) throws IOException {
        String coprocessorClassName = coprocessorClass.getName();
        TableName tableName = TableName.valueOf(tablename);
        HBaseAdmin admin = new HBaseAdmin(connection);

        HTableDescriptor htd = admin.getTableDescriptor(tableName);
        // step1. 判断是否有加协处理器, 未加协处理器，则添加
        if (!htd.hasCoprocessor(coprocessorClassName)) {
            try {
                if (admin.isTableEnabled(tableName)) {
                    admin.disableTable(tableName);
                }
                htd.addCoprocessor(coprocessorClassName);
                admin.modifyTable(tableName, htd);
            } catch (IOException e) {
                log.error(e);
            } finally {
                admin.enableTable(tableName);
                admin.close();
            }
        }
    }

    public static String getProperty(String key) {
        return PropertiesUtil.get(prop, key);
    }

    public static ExecutorService getExecutor(String type) {
        switch (type) {
            case "write":
                return writePool;
            case "read":
                return readPool;
            case "search":
                return searchPool;
        }
        return searchPool;
    }

    public static Connection getConnectionFromPool() {
        return pool.getConnection();
    }

    public static void returnPoolConnection(Connection conn) {
        pool.returnConnection(conn);
    }

    public static HTable getWriteHTable(String tableName) throws IOException {
        HTable table = (HTable) connection.getTable(TableName.valueOf(tableName));
        table.setAutoFlushTo(true);
        return table;
    }

    public static synchronized HTable getTable(String tableName) throws IOException {
        HTable table = (HTable) connection.getTable(TableName.valueOf(tableName));
        return table;
    }

    public static long aggregationRowCount(String tableName, Scan scan) throws Throwable {
        // long t1 = System.currentTimeMillis();
        long rowCount = aggregationClient.rowCount(TableName.valueOf(tableName), new LongColumnInterpreter(), scan);

        // long ts = System.currentTimeMillis() - t1;
        // Logger.debug(String.format("查到统计记录总数为%s, 耗时%s毫秒", rowCount, ts));
        return rowCount;
    }

    public static long aggregationSum(String tableName, Scan scan) throws Throwable {
        long rowSum = 0L;
        try {
            rowSum = aggregationClient.sum(TableName.valueOf(tableName), new LongColumnInterpreter(), scan);
        } catch (NullPointerException e) {
            rowSum = 0L;
        }
        return rowSum;
    }

    public static double aggregationEnergySum(String tableName, Scan scan) throws Throwable {
        double rowSum = 0.0;
        try {
            rowSum = aggregationClient.sum(TableName.valueOf(tableName), new DoubleColumnInterpreter(), scan);
        } catch (NullPointerException e) {
            rowSum = 0.0;
        }
        return rowSum;

    }

    public static void closeConnection() throws IOException {
        if (connection != null) {
            connection.close();
        }
        if (aggregationClient != null) {
            aggregationClient.close();
        }
        if (searchPool != null) {
            searchPool.shutdown();
        }
        if (writePool != null) {
            writePool.shutdown();
        }
        if (readPool != null) {
            readPool.shutdown();
        }

        if (pool != null) {
            pool.close();
        }
    }

    //[lq-add] 2018-05-21 sensetime points read from hdfs,Normalize/reversalNormalize do in SDK
    public static Map<String,float[]> getFeaturePoints() throws IllegalArgumentException{
        return  points;
    }

}
