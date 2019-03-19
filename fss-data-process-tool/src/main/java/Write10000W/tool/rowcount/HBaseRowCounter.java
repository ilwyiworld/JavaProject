package Write10000W.tool.rowcount;

import Write10000W.util.HBaseConfig1;
import Write10000W.util.PropertyTest2;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.coprocessor.AggregateImplementation;
import org.apache.hadoop.hbase.filter.KeyOnlyFilter;
import org.apache.hadoop.hbase.mapred.RowCounter;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * Created by zhuhx on 2018/4/25.
 */
public class HBaseRowCounter {
    private String zkQuorum;
    private String tableName;

    public HBaseRowCounter(String zkQuorum, String tableName) {
        this.zkQuorum = zkQuorum;
        this.tableName = tableName;
    }

    public long rowCount() {
        long count = 0;
        try {
            HBaseConfig1.initConnection(zkQuorum);
            HBaseConfig1.addCoprocessor(AggregateImplementation.class, tableName);
            Scan scan = new Scan();
            scan.addColumn(Bytes.toBytes("PICS"), Bytes.toBytes("_0"));
            scan.addColumn(Bytes.toBytes("PICSL"), Bytes.toBytes("_0"));
            KeyOnlyFilter filter = new KeyOnlyFilter();
            scan.setFilter(filter);
            count = HBaseConfig1.aggregationRowCount(tableName, scan);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return count;

    }

    public static void main(String[] args) {
        String zkQuorumtarget = PropertyTest2.getProperties().getProperty("zkQuorumtarget");
        String targetTableName = PropertyTest2.getProperties().getProperty("targetTableName");
        HBaseRowCounter hBaseRowCounter = new HBaseRowCounter(zkQuorumtarget, targetTableName);
        long startTime = System.currentTimeMillis();
        System.out.println("TotalNum:" + hBaseRowCounter.rowCount());
        long endTme = System.currentTimeMillis();
        System.out.println("Cost time :" + (endTme - startTime) + "ms");

    }
}
