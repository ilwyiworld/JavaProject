package Write10000W.rewritetable.hbasetohbase;

import Write10000W.util.HBaseConfig1;
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
 * Created by zhuhx on 2018/4/20.
 */
public class RewriteBigPicFromHBase2HBase extends Thread {
    private CountDownLatch threadSignal;
    private Scan scan;
    private String sourceTableName;
    private String targetTableName;
    private int targetSaltBuckets;

    public RewriteBigPicFromHBase2HBase(CountDownLatch threadSignal, Scan scan, String sourceTableName, String targetTableName, int targetSaltBuckets) {
        this.threadSignal = threadSignal;
        this.scan = scan;
        this.sourceTableName = sourceTableName;
        this.targetTableName = targetTableName;
        this.targetSaltBuckets = targetSaltBuckets;
    }

    @Override
    public void run() {
        rewriteBigPicHBase2HBase();
        threadSignal.countDown();
    }

    private void rewriteBigPicHBase2HBase() {
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
            //targetTable.setAutoFlush(false, false);
            rs = table.getScanner(scan);
            List<Put> puts = new ArrayList<>();
            for (Result r : rs) {
                byte[] sourceRowkey = r.getRow();
                byte[] rowPrepare = Bytes.copy(sourceRowkey, 1, sourceRowkey.length - 1);
                //生成盐值的方式
                salt[0] = SaltingUtil.getSaltingByte(rowPrepare, 0, rowPrepare.length, targetSaltBuckets);
                byte[] newRowkoy = Bytes.add(salt, rowPrepare);
                Put put = new Put(newRowkoy);
                Cell newCell;
                for (Cell cell : r.rawCells()) {
                    newCell = CellUtil.createCell(newRowkoy, cell.getFamily(), cell.getQualifier(), cell.getTimestamp(), cell.getTypeByte(), CellUtil.cloneValue(cell));
                    put.add(newCell);
                }
                puts.add(put);
                Thread.currentThread().setName("region-" + salt[0]);
                if (puts.size() == 50) {
                    targetTable.put(puts);
                    targetTable.flushCommits();

                    int totalNum = MultiWrite2HbaseFromHbase.Count.addAndGet(50);
                    MultiWrite2HbaseFromHbase.SuccessCount.addAndGet(50);
                    if (totalNum % 1000 == 0) {
                        System.out.println("data number: " + totalNum + "当前数据写入："
                                + Thread.currentThread().getName() + "  " + Bytes.toString(rowPrepare));
                    }
                    puts.clear();
                }
            }

            if (puts.size() > 0) {
                MultiWrite2HbaseFromHbase.Count.addAndGet(puts.size());
                MultiWrite2HbaseFromHbase.SuccessCount.addAndGet(puts.size());
                targetTable.put(puts);
                targetTable.flushCommits();
                puts.clear();
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
