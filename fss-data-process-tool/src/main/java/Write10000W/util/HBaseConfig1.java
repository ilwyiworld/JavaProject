package Write10000W.util;

//import com.znv.hbase.log.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.client.coprocessor.AggregationClient;
import org.apache.hadoop.hbase.client.coprocessor.DoubleColumnInterpreter;
import org.apache.hadoop.hbase.client.coprocessor.LongColumnInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by Administrator on 2016/6/14.
 */
public class HBaseConfig1 {

    private static final Logger L = LoggerFactory.getLogger(HBaseConfig1.class);
    private static Connection connection = null;
    private static Connection connectionTarget = null;
    private static AggregationClient aggregationClient = null;
    private static ExecutorService searchPool = null;
    // private static String zkQuorum = null;
    // private static Properties pop = new Properties();

    // 后期WEB更新去掉zkQuorum参数
    public static void initConnection(String zkQuorum) throws IOException {
        // 读取HDFS文件得到配置参数和HBase表名
        // getConfAndTables(hostUrl);
        // zkQuorum = pop.getProperty(VConstants.ZOOKEEPER_QUORUM);
        Configuration config = HBaseConfiguration.create();
        // 以下配置也可通过classpath目录下的hbase-site.xml和hbase-core.xml文件设置
        // 优先检查hbase-site.xml，如果没有，检查hbase-core.xml文件
        // config.set("hbase.master", "10.72.12.163:60000");
        // 与hbase/conf/hbase-site.xml中hbase.zookeeper.quorum配置的值相同
        // config.set("hbase.zookeeper.quorum", "spark0.znv-dct.com,spark1.znv-dct.com,spark2.znv-dct.com");
        // config.set("hbase.zookeeper.quorum", "znv.bfw01,znv.bfw02,znv.bfw03");
        config.set("hbase.zookeeper.quorum", zkQuorum); // 2016-09-13
        // 与hbase/conf/hbase-site.xml中hbase.zookeeper.property.clientPort配置的值相同
        config.set("hbase.zookeeper.property.clientPort", "2181");
        // 与hbase/conf/hbase-site.xml中zookeeper.znode.parent配置的值相同
        // config.set("zookeeper.znode.parent", "/hbase-unsecure");
        config.set("zookeeper.znode.parent", "/hbase");

        config.set("hbase.client.retries.number", "4");
        // tmp #hy, 用于统计表的数据量
        config.setInt("hbase.rpc.timeout", 1200000 * 3 * 24); // tmp
        config.setInt("hbase.client.operation.timeout", 1200000 * 3 * 24); // tmp
        config.setInt("hbase.client.scanner.timeout.period", 1200000 * 3 * 24); // scan 超时时间，一天

        connection = ConnectionFactory.createConnection(config);
        aggregationClient = new AggregationClient(config);

        // 创建线程池，控制并发数
        final String n = Thread.currentThread().getName();
        searchPool = Executors.newFixedThreadPool(36, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName(n + "-search-" + System.currentTimeMillis());
                return t;
            }
        });

        // 添加协处理器判断
        checkOrAddCoprocessors();
    }

    public static void initConnection(String zkQuorum, String dstZkQuorum) throws IOException {
        // 读取HDFS文件得到配置参数和HBase表名
        // getConfAndTables(hostUrl);
        // zkQuorum = pop.getProperty(VConstants.ZOOKEEPER_QUORUM);
        Configuration config = HBaseConfiguration.create();
        // 以下配置也可通过classpath目录下的hbase-site.xml和hbase-core.xml文件设置
        // 优先检查hbase-site.xml，如果没有，检查hbase-core.xml文件
        // config.set("hbase.master", "10.72.12.163:60000");
        // 与hbase/conf/hbase-site.xml中hbase.zookeeper.quorum配置的值相同
        // config.set("hbase.zookeeper.quorum", "spark0.znv-dct.com,spark1.znv-dct.com,spark2.znv-dct.com");
        // config.set("hbase.zookeeper.quorum", "znv.bfw01,znv.bfw02,znv.bfw03");
        config.set("hbase.zookeeper.quorum", zkQuorum); // 2016-09-13
        // 与hbase/conf/hbase-site.xml中hbase.zookeeper.property.clientPort配置的值相同
        config.set("hbase.zookeeper.property.clientPort", "2181");
        // 与hbase/conf/hbase-site.xml中zookeeper.znode.parent配置的值相同
        // config.set("zookeeper.znode.parent", "/hbase-unsecure");
        config.set("zookeeper.znode.parent", "/hbase");

        config.set("hbase.client.retries.number", "10"); //重试次数
        // tmp #hy, 用于统计表的数据量
        config.setInt("hbase.rpc.timeout", 1200000 * 3 * 24); // tmp  一次RPC请求的超时时间
        config.setInt("hbase.client.operation.timeout", 1200000 * 3 * 24); // tmp 一次操作的超时时间，有可能包含多个RPC请求
        config.setInt("hbase.client.scanner.timeout.period", 1200000 * 3 * 24); // scan 超时时间，一天  指scan查询时每次与RegionServer交互的超时时间

        connection = ConnectionFactory.createConnection(config);
        aggregationClient = new AggregationClient(config);

        // 创建线程池，控制并发数
        final String n = Thread.currentThread().getName();
        searchPool = Executors.newFixedThreadPool(36, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName(n + "-search-" + System.currentTimeMillis());
                return t;
            }
        });

        // 添加协处理器判断
        checkOrAddCoprocessors();

        Configuration config2 = HBaseConfiguration.create();
        // 以下配置也可通过classpath目录下的hbase-site.xml和hbase-core.xml文件设置
        // 优先检查hbase-site.xml，如果没有，检查hbase-core.xml文件
        // config.set("hbase.master", "10.72.12.163:60000");
        // 与hbase/conf/hbase-site.xml中hbase.zookeeper.quorum配置的值相同
        // config.set("hbase.zookeeper.quorum", "spark0.znv-dct.com,spark1.znv-dct.com,spark2.znv-dct.com");
        // config.set("hbase.zookeeper.quorum", "znv.bfw01,znv.bfw02,znv.bfw03");
        config2.set("hbase.zookeeper.quorum", dstZkQuorum); // 2016-09-13
        // 与hbase/conf/hbase-site.xml中hbase.zookeeper.property.clientPort配置的值相同
        config2.set("hbase.zookeeper.property.clientPort", "2181");
        // 与hbase/conf/hbase-site.xml中zookeeper.znode.parent配置的值相同
        // config.set("zookeeper.znode.parent", "/hbase-unsecure");
        config2.set("zookeeper.znode.parent", "/hbase");

        config2.set("hbase.client.retries.number", "4");
        // tmp #hy, 用于统计表的数据量
        config2.setInt("hbase.rpc.timeout", 1200000 * 3 * 24); // tmp
        config2.setInt("hbase.client.operation.timeout", 1200000 * 3 * 24); // tmp
        config2.setInt("hbase.client.scanner.timeout.period", 1200000 * 3 * 24); // scan 超时时间，一天

        connectionTarget = ConnectionFactory.createConnection(config2);
    }

    private static void checkOrAddCoprocessors() throws IOException {
        // 以图搜图报表
        // addCoprocessor(ImageSearchImplementation.class, "FSS_imageData_searchByImage"); //_lq_test_0718
        // addCoprocessor(BlackStaticSearchImplementation.class,pop.getProperty(VConstants.BLACK_STATIC_TABLE));

    }

    /*
     * @param coprocessorName coprocessor class, sunch as AggregateImplementation.class
     * @param tableName
     */
    public static void addCoprocessor(Class coprocessorClass, String tablename) throws IOException {
        String coprocessorClassName = coprocessorClass.getName();
        TableName tableName = TableName.valueOf(tablename);
        HBaseAdmin admin = new HBaseAdmin(connection);

        HTableDescriptor htd = admin.getTableDescriptor(tableName);
        // step1. 判断是否有加协处理器, 未加协处理器，则添加
        if (false == htd.hasCoprocessor(coprocessorClassName)) {
            try {
                if (admin.isTableEnabled(tableName)) {
                    admin.disableTable(tableName);
                }
                htd.addCoprocessor(coprocessorClassName);
                admin.modifyTable(tableName, htd);
            } catch (IOException e) {
                L.error(e.toString());
            } finally {
                admin.enableTable(tableName);
                admin.close();
            }
        }
    }

    // private static void getConfAndTables(String hostUrl) throws FileNotFoundException, IOException {
    // // 读取properties文件
    // String filePath = hostUrl + "/user/fss_V100/config/hbaseConfig.properties";
    // Configuration conf = new Configuration();
    // FileSystem fs = FileSystem.get(URI.create(filePath), conf);
    // FSDataInputStream hdfsInStream = fs.open(new Path(filePath));
    //
    // pop.load(hdfsInStream);
    // hdfsInStream.close();
    // }

    // 配置信息改为使用properties文件存放
    // public static String getHBaseTableName(String name) {
    // String tableName = pop.getProperty(name);
    // return tableName;
    // }

    public static ExecutorService getExecutor() throws IOException {
        return searchPool;
    }

    public synchronized static HTable getTable(String tableName) throws IOException {
        HTable table = (HTable) connection.getTable(TableName.valueOf(tableName));
        return table;
    }

    public synchronized static HTable getTableTarget(String tableName) throws IOException {
        HTable table = (HTable) connectionTarget.getTable(TableName.valueOf(tableName));
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
        // long t1 = System.currentTimeMillis();
        long rowSum = aggregationClient.sum(TableName.valueOf(tableName), new LongColumnInterpreter(), scan);
        // long ts = System.currentTimeMillis() - t1;
        // Logger.debug(String.format("查到统计记录总数为%s, 耗时%s毫秒", rowCount, ts));
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

    public static Connection getConnection() {
        return connection;
    }

    public static void closeConnection() throws IOException {
        connection.close();
        aggregationClient.close();
        searchPool.shutdown();
    }

}
