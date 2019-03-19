package com.znv.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.coprocessor.AggregationClient;
import org.apache.hadoop.hbase.client.coprocessor.LongColumnInterpreter;
import org.apache.hadoop.hbase.security.User;
import org.apache.hadoop.hbase.security.UserProvider;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;

/**
 * Created by Administrator on 2016/6/14.
 */
public class HBaseConfig {
    private static Connection connection = null;
    private static AggregationClient aggregationClient = null;
    private static Configuration config = null;

    public static void initConnection(String zkQuorum) throws IOException {
        config = HBaseConfiguration.create();
        // 以下配置也可通过classpath目录下的hbase-site.xml和hbase-core.xml文件设置
        // 优先检查hbase-site.xml，如果没有，检查hbase-core.xml文件
        // config.set("hbase.master", "10.72.12.163:60000");
        // 与hbase/conf/hbase-site.xml中hbase.zookeeper.quorum配置的值相同
        // config.set("hbase.zookeeper.quorum", "spark0.znv-dct.com,spark1.znv-dct.com,spark2.znv-dct.com");
        // config.set("hbase.zookeeper.quorum", "znv.bfw01,znv.bfw02,znv.bfw03");
        config.set("hbase.zookeeper.quorum", zkQuorum);
        // 与hbase/conf/hbase-site.xml中hbase.zookeeper.property.clientPort配置的值相同
        config.set("hbase.zookeeper.property.clientPort", "2181");
        // 与hbase/conf/hbase-site.xml中zookeeper.znode.parent配置的值相同
        config.set("zookeeper.znode.parent", "/hbase");

        config.set("hbase.client.retries.number", "10");
        config.set("hbase.client.pause", "30000");// hy 防止split同时写入异常
        config.setInt("hbase.rpc.timeout", 216000000);
        config.setInt("hbase.client.operation.timeout", 216000000);
        // config.setInt("hbase.client.scanner.timeout.period",20000);

        // remote debug
        config.set("zookeeper.session.timeout", "216000000");
        config.set("hbase.zookeeper.property.tickTime", "216000000");

        // user 设置用户，否则split时会由于无权限访问hdfs，导致regionserver抛异常
        UserProvider userProvider = UserProvider.instantiate(config);
        UserGroupInformation ugi = UserGroupInformation.createRemoteUser("hbase");
        User user = userProvider.create(ugi);

        connection = ConnectionFactory.createConnection(config, user);// .createConnection(config);
        aggregationClient = new AggregationClient(config);
    }

    public synchronized static HTable getTable(String tableName) throws IOException {
        HTable table = (HTable) connection.getTable(TableName.valueOf(tableName));
        table.setAutoFlush(false, true);
        return table;
    }

    public static Connection getConnection() {
        return connection;
    }

    public static long aggregationOperation(String tableName, Scan scan) throws Throwable {
        // long t1 = System.currentTimeMillis();
        long rowCount = aggregationClient.rowCount(TableName.valueOf(tableName), new LongColumnInterpreter(), scan);

        // long ts = System.currentTimeMillis() - t1;
        // Logger.debug(String.format("查到统计记录总数为%s, 耗时%s毫秒", rowCount, ts));
        return rowCount;
    }

    public static long aggregationSum(String tableName, Scan scan) throws Throwable {
        // long t1 = System.currentTimeMillis();
        long sum = aggregationClient.sum(TableName.valueOf(tableName), new LongColumnInterpreter(), scan);

        // long ts = System.currentTimeMillis() - t1;
        // Logger.debug(String.format("查到统计记录总数为%s, 耗时%s毫秒", rowCount, ts));
        return sum;
    }

    public static void closeConnection() throws IOException {
        connection.close();
        aggregationClient.close();
    }

}
