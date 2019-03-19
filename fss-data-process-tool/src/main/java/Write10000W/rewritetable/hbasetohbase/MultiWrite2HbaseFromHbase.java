package Write10000W.rewritetable.hbasetohbase;

import Write10000W.rewritetable.hbasetohbase.RewriteBigPicFromHBase2HBase;
import Write10000W.rewritetable.hbasetohbase.RewritePersonListFromHBase2HBase;
import Write10000W.tool.reversescan.HbaseScanReverse;
import Write10000W.util.ConnectionPool;
import Write10000W.util.HBaseConfig1;
import Write10000W.util.PropertyTest2;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.phoenix.schema.SaltingUtil;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2017/10/12.
 */

//从Hbase表考入另一集群的phoenix表，1亿数据的拷贝
public class MultiWrite2HbaseFromHbase {
    //private static final String tableNmae = "TEST:FSS_HISTORY_V1_1_3_20170727_18W_YC_1";
    //private static final String zkQuorum = "10.45.157.120";
    //public static final String zkQuorumtarget = "10.45.157.94";
    //private static final String targetTableName = "TEST:FSS_HISTORY_SNOWFLAKE_ID6";
    //public static String time = "2017-08-05 08:00:00";
    private static int track = 0;
    public static int totalNum = 0;
    public static int timeKey = 0;
    public static int perMinuteNum = 0; //摄像头每分钟的数据
    public static int curCamera = 0; //当前取的第几个camera_id
    public static AtomicInteger Count = new AtomicInteger(0);
    public static AtomicInteger SuccessCount = new AtomicInteger(0);
    public static AtomicInteger FailCount = new AtomicInteger(0);
    private static final String table = PropertyTest2.getProperties().getProperty("table");
    private static final String zkQuorum = PropertyTest2.getProperties().getProperty("zkQuorum");
    private static final String zkQuorumtarget = PropertyTest2.getProperties().getProperty("zkQuorumtarget");
    private static final String sourceTableName = PropertyTest2.getProperties().getProperty("sourceTableName");
    private static final String targetTableName = PropertyTest2.getProperties().getProperty("targetTableName");
    private static final int sourceSaltBuckets = Integer.parseInt(PropertyTest2.getProperties().getProperty("sourceSaltBuckets"));
    private static final int targetSaltBuckets = Integer.parseInt(PropertyTest2.getProperties().getProperty("targetSaltBuckets"));
    private static final int threadNum = Integer.parseInt(PropertyTest2.getProperties().getProperty("threadNum"));
    private static final String faceUrl = PropertyTest2.getProperties().getProperty("faceUrl");
    private static final String faceUrlSingle = PropertyTest2.getProperties().getProperty("faceUrlSingle");
    private static final int reTry = Integer.parseInt(PropertyTest2.getProperties().getProperty("reTry"));


    private static void rewrite(int sourceSaltBuckets, String table) {
        long timeStart = System.currentTimeMillis();
        String[] saltUuid = HbaseScanReverse.hbaseScanReverse(zkQuorumtarget, targetTableName, targetSaltBuckets, sourceSaltBuckets, threadNum);

        try {
            HBaseConfig1.initConnection(zkQuorum, zkQuorumtarget);//初始化原集群、目标集群
            System.out.println("集群初始化完成！");

        } catch (IOException e) {
            e.printStackTrace();
        }
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(threadNum);
        CountDownLatch threadSignal = new CountDownLatch(threadNum);
        int interval = sourceSaltBuckets / threadNum;

        for (int saltKey = 0; saltKey < sourceSaltBuckets; saltKey += interval) {
            Scan scan = new Scan();
            scan.setCacheBlocks(true);
            scan.setMaxVersions(1);
            scan.setCaching(50);

            //起始Rowkey
            byte[] salt = new byte[1];

            int index = saltKey / interval;
            if (saltUuid[index] == null || saltUuid[index].equals("")) {
                salt[0] = (byte) saltKey;
                scan.setStartRow(salt);
            } else {
                byte[] uuid = Bytes.toBytes(saltUuid[index]);
                salt[0] = SaltingUtil.getSaltingByte(uuid, 0, uuid.length, sourceSaltBuckets);
                scan.setStartRow(Bytes.add(salt, uuid));
            }

            byte[] saltStop = new byte[1];
            saltStop[0] = (byte) (saltKey + interval);
            scan.setStopRow(saltStop);

            PageFilter filter = new PageFilter(1);
            //scan.setFilter(filter);
            Thread td = null;
            switch (table) {
                case "personList":
                    td = new RewritePersonListFromHBase2HBase(threadSignal, scan, sourceTableName, targetTableName, targetSaltBuckets, faceUrl, faceUrlSingle, reTry);
                    break;

                case "bigPicture":
                    td = new RewriteBigPicFromHBase2HBase(threadSignal, scan, sourceTableName, targetTableName, targetSaltBuckets);
                    break;
                //               case "alarm":td = new RewriteAlarmFromHBase2HBase(threadSignal, scan, sourceTableName, targetTableName, targetSaltBuckets, faceUrl, faceUrlSingle, reTry);break;
                //               case "history":td = new RewriteHistoryFromHBase2HBase(threadSignal, scan, sourceTableName, targetTableName, targetSaltBuckets, faceUrl, faceUrlSingle, reTry);break;
                default:
                    break;
            }
            fixedThreadPool.execute(td);
        }

        //等待所有线程执行完
        try {
            threadSignal.await();
        } catch (InterruptedException e) {
            fixedThreadPool.shutdown();
            e.printStackTrace();
        }
        fixedThreadPool.shutdown();

        try {
            HBaseConfig1.closeConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        long timeEnd = System.currentTimeMillis();
        System.out.println("导入耗时：" + (timeEnd - timeStart) + "ms");
        System.out.println("导入总数：" + Count);
        System.out.println("successCount：" + SuccessCount);
        System.out.println("FailCount：" + FailCount);
    }

    public static void main(String[] args) {
        //重写名单库
        try {
            rewrite(sourceSaltBuckets, table);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
