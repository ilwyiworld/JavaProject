package Write10000W.rewritetable.HbaseHbaseSameCopy;

import Write10000W.util.HBaseConfig1;
import Write10000W.util.LOPQFamily;
import Write10000W.util.PropertyTest2;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.phoenix.schema.SaltingUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by User on 2018/4/20.
 */
public class Hbase2HbaseCopyThread extends Thread {
    private CountDownLatch threadsSignal;
    private Scan scan;
    private String sourceTableName;
    private String targetTableName;
    private int targetSaltBuckets;
    private static final int putSize = Integer.parseInt(PropertyTest2.getProperties().getProperty("putSize"));
    private static final int outNum = Integer.parseInt(PropertyTest2.getProperties().getProperty("outNum"));

    public Hbase2HbaseCopyThread(CountDownLatch threadsSignal, Scan scan,
                                 String sourceTableName, String targetTableName, int targetSaltBuckets) {
        this.threadsSignal = threadsSignal;
        this.scan = scan;
        this.sourceTableName = sourceTableName;
        this.targetTableName = targetTableName;
        this.targetSaltBuckets = targetSaltBuckets;
    }

    public void run() {
        // 获取hbase表中的数据
        copyData();
        threadsSignal.countDown(); // 线程结束时计数器减1
    }

    public void copyData() {
        ResultScanner rs = null;
        HTable table = null;
        HTable targetTable = null;

        //分隔符
        byte[] x00 = new byte[1];
        x00[0] = (byte) 0x00;

        //盐值
        byte[] salt = new byte[1];

        try {
            table = HBaseConfig1.getTable(sourceTableName);
            targetTable = HBaseConfig1.getTableTarget(targetTableName);
            // targetTable.setAutoFlush(false, false);

            rs = table.getScanner(scan);
            List<Put> puts = new ArrayList<>();
            for (Result r : rs) {
                byte[] featureByte = r.getValue(Bytes.toBytes("FEATURE"), Bytes.toBytes("RT_FEATURE"));
                byte[] LOPQ = Bytes.toBytes(LOPQFamily.getLOPQ(featureByte));

                byte[] sourceRowkey = r.getRow();
                byte[] rowWithoutSalt = Bytes.add(Bytes.copy(sourceRowkey, 1, 20), LOPQ,
                        Bytes.copy(sourceRowkey, 21, (sourceRowkey.length - 21)));

                //生成盐值的方式
                salt[0] = SaltingUtil.getSaltingByte(rowWithoutSalt, 0, rowWithoutSalt.length, targetSaltBuckets);
                byte[] newRowkey = Bytes.add(salt, rowWithoutSalt);
                Put put = new Put(newRowkey);

                for (Cell cell : r.rawCells()) {
                    Cell newCell = CellUtil.createCell(newRowkey, cell.getFamily(), cell.getQualifier(), cell.getTimestamp(), cell.getTypeByte(), CellUtil.cloneValue(cell));
                    put.add(newCell);
                }
                put.add(Bytes.toBytes("FEATURE"), Bytes.toBytes("_0"), Bytes.toBytes("x"));
                puts.add(put);
                // put.add(Bytes.toBytes("PICS"), Bytes.toBytes("_0"), Bytes.toBytes("x"));
                // put.add(Bytes.toBytes("ATTR"), Bytes.toBytes("_0"), Bytes.toBytes("x"));
                Thread.currentThread().setName("region-" + salt[0]);
                if (puts.size() == putSize) {
                    CopyData2HbaseSame.Count.addAndGet(putSize);
                    targetTable.put(puts);
                    targetTable.flushCommits();
                    int sucessNum = CopyData2HbaseSame.SuccessCount.addAndGet(putSize);
                    if (sucessNum % outNum == 0) {
                        System.out.println("data number: " + sucessNum);
//                        System.out.println("data number: " + sucessNum + "当前数据写入："
//                                + Thread.currentThread().getName() + "  " + Bytes.toString(rowWithoutSalt));
                    }
                    puts.clear();
                }
            }

            if (puts.size() > 0) {
                CopyData2HbaseSame.Count.addAndGet(puts.size());
                targetTable.put(puts);
                targetTable.flushCommits();
                puts.clear();
                CopyData2HbaseSame.SuccessCount.addAndGet(puts.size());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            rs.close();
            try {
                table.close();
                targetTable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
