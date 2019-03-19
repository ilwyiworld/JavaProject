package Write10000W.rewritetable.HbaseHbaseSameCopy;

import Write10000W.util.HBaseConfig1;
import Write10000W.util.PropertyTest2;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.phoenix.schema.SaltingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static Write10000W.tool.reversescan.HbaseScanReverseByTable.hbaseScanReverseHistory;


/**
 * Created by User on 2018/5/2.
 */
public class CopyData2HbaseSame {
    private static Logger L = LoggerFactory.getLogger(CopyData2HbaseSame.class);

    private static final String sourceZkQuorum = PropertyTest2.getProperties().getProperty("sourceZkQuorum");
    private static final String targetZkQuorum = PropertyTest2.getProperties().getProperty("targetZkQuorum");
    private static final String tableType = PropertyTest2.getProperties().getProperty("tableType");

    private static final String sourceTableName = PropertyTest2.getProperties().getProperty("sourceTableName");
    private static final String targetTableName = PropertyTest2.getProperties().getProperty("targetTableName");
    private static final int sourceSaltBuckets = Integer.parseInt(PropertyTest2.getProperties().getProperty("sourceSaltBuckets"));
    private static final int targetSaltBuckets = Integer.parseInt(PropertyTest2.getProperties().getProperty("targetSaltBuckets"));
    private static final int threadNum = Integer.parseInt(PropertyTest2.getProperties().getProperty("threadNum"));
    private static final int catchSize = Integer.parseInt(PropertyTest2.getProperties().getProperty("catchSize"));

    public static AtomicInteger Count = new AtomicInteger(0);
    public static AtomicInteger SuccessCount = new AtomicInteger(0);

    private static void rewrite() {
        long timeStart = System.currentTimeMillis();
        try {
            L.info("start init hbase! ");
            HBaseConfig1.initConnection(sourceZkQuorum, targetZkQuorum); //初始化原集群、目标集群
            L.info("init hbase finished！");
        } catch (IOException e) {
            e.printStackTrace();
        }

        ExecutorService fixedThreadPool = null;
        CountDownLatch threadSignal = null;

        switch (tableType) {
            case "history":
                Map<String, byte[]> rowKey = new HashMap<>();
                String[] slatEnterTime = hbaseScanReverseHistory(targetZkQuorum, targetTableName, targetSaltBuckets,
                        sourceSaltBuckets, threadNum, rowKey);
                fixedThreadPool = Executors.newFixedThreadPool(threadNum);
                threadSignal = new CountDownLatch(threadNum);

                int intervalHistory = sourceSaltBuckets / threadNum;
                for (int saltKey = 0; saltKey < sourceSaltBuckets; saltKey += intervalHistory) {
                    Scan scan = new Scan();
                    scan.setCacheBlocks(true);
                    scan.setMaxVersions(1);
                    scan.setCaching(catchSize);

                    //起始Rowkey
                    byte[] salt = new byte[1];

                    int index = saltKey / intervalHistory;
                    if (slatEnterTime[index] == null || slatEnterTime[index].equals("")) {
                        salt[0] = (byte) saltKey;
                        scan.setStartRow(salt);
                    } else {
//                          byte[] enterTime = Bytes.toBytes(slatEnterTime[index]);
                        byte[] row = rowKey.get(slatEnterTime[index]);
                        salt[0] = SaltingUtil.getSaltingByte(row, 0, row.length, sourceSaltBuckets);
                        scan.setStartRow(Bytes.add(salt, row));
                    }

                    byte[] saltStop = new byte[1];
                    saltStop[0] = (byte) (saltKey + intervalHistory);
                    scan.setStopRow(saltStop);
                    scan.addColumn(Bytes.toBytes("FEATURE"), Bytes.toBytes("RT_FEATURE"));
                    scan.setFilter(new PageFilter(1));
                    Thread td = new Hbase2HbaseCopyThread(threadSignal, scan, sourceTableName, targetTableName,
                            targetSaltBuckets);
                    fixedThreadPool.execute(td);
                }
                break;
            default:
                L.info("cannot find suit tableType...");
                break;
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
            L.error("close hbase error!");
            e.printStackTrace();
        }

        long timeEnd = System.currentTimeMillis();
        System.out.println("total time：" + (double) (timeEnd - timeStart) / (double) (1000 * 3600) + "h");
        System.out.println("total num：" + Count);
        System.out.println("success count：" + SuccessCount);
    }

    public static void main(String[] args) {
        try {
            rewrite();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
