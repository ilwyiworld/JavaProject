package Write10000W.tool.reversescan;

import Write10000W.util.HBaseConfig1;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.phoenix.schema.SaltingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhuhx on 2018/4/23.
 */
public class HbaseScanReverseByTable {
    private static final Logger L = LoggerFactory.getLogger(HbaseScanReverse.class);
    private static String zkQuorum = "10.45.157.120";

    //历史表
    private static String tableNameHistory = "ZHX:FSS_HISTORY_TEST_2";
    private static int sourceSaltBucketsHistory = 36;
    private static int targetSaltBucketsHistory = 36;
    private static int threadNumHistory = 36;

    //大图表
    private static String tableNameBig = "ZHX:FSS_BIG_PICTURE_63_2";
    private static int sourceSaltBucketsBig = 63;
    private static int targetSaltBucketsBig = 63;
    private static int threadNumBig = 21;

    public static String[] hbaseScanReverseHistory(String zkQuorum, String tableName, int sourceSaltBucketsHistory,
                                                   int targetSaltBucketsHistory, int threadNumHistory, Map<String, byte[]> rowKey) {

        int interVal = sourceSaltBucketsHistory / threadNumHistory;
        String[] saltEnterTime = new String[threadNumHistory];

        for (int saltKey = 0; saltKey < targetSaltBucketsHistory; saltKey += interVal) {
            Scan scan = new Scan();
            scan.setMaxVersions(1);
            scan.setCacheBlocks(true);
            byte[] start = new byte[1];
            start[0] = (byte) saltKey;
            byte[] stop = new byte[1];
            stop[0] = (byte) (saltKey + interVal);
            scan.setStartRow(stop);
            scan.setStopRow(start);

            scan.setReversed(true);
            scan.setFilter(new PageFilter(1));

            saltReverseHistory(scan, tableName, interVal, sourceSaltBucketsHistory, saltEnterTime, rowKey);
        }

        L.info("reverse success!");
        return saltEnterTime;
    }

    public static String[] hbaseScanReverseHistoryLocal(String zkQuorum, String tableName, int sourceSaltBucketsHistory,
                                                        int targetSaltBucketsHistory, int threadNumHistory, Map<String, byte[]> rowKey) {
        try {
            HBaseConfig1.initConnection(zkQuorum);
        } catch (IOException e) {
            L.error("init hbase error!");
            e.printStackTrace();
        }

        int interVal = sourceSaltBucketsHistory / threadNumHistory;
        String[] saltEnterTime = new String[threadNumHistory];

        for (int saltKey = 0; saltKey < targetSaltBucketsHistory; saltKey += interVal) {
            Scan scan = new Scan();
            scan.setMaxVersions(1);
            scan.setCacheBlocks(true);
            byte[] start = new byte[1];
            start[0] = (byte) saltKey;
            byte[] stop = new byte[1];
            stop[0] = (byte) (saltKey + interVal);
            scan.setStartRow(stop);
            scan.setStopRow(start);

            scan.setReversed(true);
            scan.setFilter(new PageFilter(1));

            saltReverseHistory(scan, tableName, interVal, sourceSaltBucketsHistory, saltEnterTime, rowKey);
        }

        try {
            HBaseConfig1.closeConnection();
        } catch (IOException e) {
            L.error("close hbase error！");
            e.printStackTrace();
        }

        L.info("reverse success!");
        return saltEnterTime;
    }

    public static void saltReverseHistory(Scan scan, String tableName, int sourceSaltBucketsHistory, int interVal,
                                          String[] saltEnterTime, Map<String, byte[]> rowKey) {
        HTable tableHistory = null;
        ResultScanner rs = null;
        byte[] salt = new byte[1];
        byte[] xFF = new byte[1];
        xFF[0] = (byte) 0xff;

        try {
            tableHistory = HBaseConfig1.getTable(tableName);
            rs = tableHistory.getScanner(scan);
            for (Result r : rs) {
                byte[] sourceRowkey = r.getRow();
                byte[] enterTime = Bytes.copy(r.getRow(), 1, 19);
                String enterTimeString = Bytes.toString(convertDescField(enterTime));
                //  String uuid = Bytes.toString(r.getRow(), 21);
                byte[] rowWithoutSalt = Bytes.copy(sourceRowkey, 1, sourceRowkey.length - 1);
                salt[0] = SaltingUtil.getSaltingByte(rowWithoutSalt, 0, rowWithoutSalt.length, sourceSaltBucketsHistory);
                // byte[] rowNew = Bytes.add(enterTime, xFF, Bytes.toBytes(uuid));
                //  byte[] rowNewHistory = Bytes.add(salt, rowWithoutSalt);
                rowKey.put(enterTimeString, rowWithoutSalt);

                int idx = salt[0] / interVal;
                if (saltEnterTime[idx] == null || saltEnterTime.equals("")) {
                    saltEnterTime[idx] = enterTimeString;
                } else if (saltEnterTime[idx].compareTo(enterTimeString) < 0) {
                    saltEnterTime[idx] = enterTimeString;
                }
            }
        } catch (IOException e) {
            L.error("getTable error!");
            e.printStackTrace();
        } finally {
            rs.close();
            try {
                tableHistory.close();
            } catch (IOException e) {
                L.error("close table error!");
                e.printStackTrace();
            }
        }
    }

    public static String[] hbaseScanReverseBig(String zkQuorum, String tableName, int targetSaltBuckets,
                                               int sourceSaltBuckets, int threadNum) {

        String[] hbaseScanReverseBig = new String[threadNum];
        int interval = sourceSaltBuckets / threadNum; //每个线程管理的region数目
        for (int saltKey = 0; saltKey < targetSaltBuckets; saltKey++) {
            Scan scan = new Scan();
            scan.setCacheBlocks(true);
            scan.setMaxVersions(1);

            byte[] start = new byte[1];
            start[0] = (byte) (saltKey + 1);
            byte[] stop = new byte[1];
            stop[0] = (byte) saltKey;

            scan.setStartRow(start);
            scan.setStopRow(stop);

            scan.setReversed(true);
            scan.setFilter(new PageFilter(1));

            saltReverseBig(tableName, scan, hbaseScanReverseBig, interval, sourceSaltBuckets);
        }

        L.info("reverse success!");
        return hbaseScanReverseBig;
    }

    public static String[] hbaseScanReverseBigLocal(String zkQuorum, String tableName, int targetSaltBuckets,
                                                    int sourceSaltBuckets, int threadNum) {

        try {
            L.info("init hbase start...");
            HBaseConfig1.initConnection(zkQuorum);
            L.info("init hbase end...");
        } catch (IOException e) {
            L.info("init failed !" + e);
            e.printStackTrace();
        }

        String[] hbaseScanReverseBig = new String[threadNum];
        int interval = sourceSaltBuckets / threadNum; //每个线程管理的region数目
        for (int saltKey = 0; saltKey < targetSaltBuckets; saltKey++) {
            Scan scan = new Scan();
            scan.setCacheBlocks(true);
            scan.setMaxVersions(1);
            scan.setReversed(true);

            byte[] start = new byte[1];
            start[0] = (byte) (saltKey + 1);
            byte[] stop = new byte[1];
            stop[0] = (byte) saltKey;

            scan.setStartRow(start);
            scan.setStopRow(stop);

            scan.setFilter(new PageFilter(1));

            saltReverseBig(tableName, scan, hbaseScanReverseBig, interval, sourceSaltBuckets);
        }

        try {
            HBaseConfig1.closeConnection();
        } catch (IOException e) {
            L.error("close hbase connection failed!");
            e.printStackTrace();
        }
        L.info("reverse success!");
        return hbaseScanReverseBig;
    }


    private static void saltReverseBig(String tableName, Scan scan, String[] saltUuid, int interval, int sourceSaltBuckets) {
        HTable table = null;
        ResultScanner rs = null;
        byte[] salt = new byte[1];

        try {
            table = HBaseConfig1.getTable(tableName);
            rs = table.getScanner(scan);
            for (Result r : rs) {
                byte[] sourceRowkey = r.getRow(); //原表的rowkey
                byte[] rowPrepare = Bytes.copy(sourceRowkey, 1, sourceRowkey.length - 1); //原大图表的rowkey：uuid
                String uuid = Bytes.toString(rowPrepare);
                salt[0] = SaltingUtil.getSaltingByte(rowPrepare, 0, rowPrepare.length, sourceSaltBuckets); //原大图表的盐值

                int index = salt[0] / interval;
                if (saltUuid[index] == null || saltUuid[index].equals("")) {
                    saltUuid[index] = uuid;
                } else if (saltUuid[index].compareTo(uuid) < 0) {
                    saltUuid[index] = uuid; //存放每个线程管理的region上的最大的uuid
                }

//                System.out.println(String.format("region %d, %s, %s, %d", sourceRowkey[0],
//                        Thread.currentThread().getName(), uuid, salt[0]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            rs.close();
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String[] hbaseScanReversePersonList(String zkQuorum, String tableName, int targetSaltBuckets,
                                                      int sourceSaltBuckets, int threadNum) {
        String[] hbaseScanReversePersonList = new String[threadNum];


        return hbaseScanReversePersonList;
    }

    public static String[] hbaseScanReverseAlarm(String zkQuorum, String tableName, int targetSaltBuckets,
                                                 int sourceSaltBuckets, int threadNum) {
        String[] hbaseScanReverseAlarm = new String[threadNum];


        return hbaseScanReverseAlarm;
    }

    public static byte[] convertDescField(byte[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = (byte) (~array[i]);
        }
        return array;
    }

    public static void main(String[] args) {
        //历史表
        Map<String, byte[]> rowKey = new HashMap<>();
        String[] saltEnterTime = hbaseScanReverseHistoryLocal(zkQuorum, tableNameHistory, sourceSaltBucketsHistory, targetSaltBucketsHistory,
                threadNumHistory, rowKey);
        System.out.println("" + saltEnterTime.length + Arrays.asList(saltEnterTime));

        //大图表
//        String[] saltUuidBig = hbaseScanReverseBigLocal(zkQuorum, tableNameBig, sourceSaltBucketsBig, targetSaltBucketsBig, threadNumBig);
//        System.out.println("" + saltUuidBig.length + Arrays.asList(saltUuidBig));
    }
}
