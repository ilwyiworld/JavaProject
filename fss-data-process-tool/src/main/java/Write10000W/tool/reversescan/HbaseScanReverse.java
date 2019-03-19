package Write10000W.tool.reversescan;

import Write10000W.util.HBaseConfig1;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.phoenix.schema.SaltingUtil;
import java.io.IOException;
import java.util.Arrays;
/**
 * Created by zhuhx on 2018/4/23.
 */
public class HbaseScanReverse {
    public static String[] hbaseScanReverse(String zkQuorum, String tableName, int targetSaltBuckets, int sourceSaltBuckets, int threadNum) {
        try {
            HBaseConfig1.initConnection(zkQuorum);
        } catch (IOException e) {
            System.out.println("init failed !" + e);
        }
        String[] saltUuid = new String[threadNum];
        int interval = sourceSaltBuckets / threadNum;
        for (int saltKey = 0; saltKey < targetSaltBuckets; saltKey++) {
            Scan scan = new Scan();
            scan.setReversed(true);
            scan.setMaxVersions(1);

            byte[] start = new byte[1];
            start[0] = (byte) (saltKey + 1);
            byte[] stop = new byte[1];
            stop[0] = (byte) saltKey;

            scan.setStartRow(start);
            scan.setStopRow(stop);

            PageFilter pageFilter = new PageFilter(1);
            scan.setFilter(pageFilter);

            saltReverse(tableName, scan, saltUuid, interval, sourceSaltBuckets);
        }

        try {
            HBaseConfig1.closeConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("reverse success!");
        return saltUuid;
    }


    private static void saltReverse(String tableName, Scan scan, String[] saltUuid, int interval, int sourceSaltBuckets) {
        Table table = null;
        ResultScanner rs = null;
        byte[] salt = new byte[1];
        try {
            table = HBaseConfig1.getTable(tableName);
            rs = table.getScanner(scan);
            for (Result r : rs) {
                byte[] sourceRowkey = r.getRow();
                byte[] rowPrepare = Bytes.copy(sourceRowkey, 1, sourceRowkey.length - 1);
                String uuid = Bytes.toString(rowPrepare);
                salt[0] = SaltingUtil.getSaltingByte(rowPrepare, 0, rowPrepare.length, sourceSaltBuckets);

                int index = salt[0] / interval;
                if (saltUuid[index] == null || saltUuid[index].equals("")) {
                    saltUuid[index] = uuid;
                } else if (saltUuid[index].compareTo(uuid) < 0) {
                    saltUuid[index] = uuid;
                }

                System.out.println(String.format("region %d, %s, %s, %d", sourceRowkey[0], Thread.currentThread().getName(), uuid, salt[0]));
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

    public static void main(String[] args) {
        String[] saltUUid = hbaseScanReverse("10.45.157.120", "ZHX:FSS_BIG_PICTURE_126", 126, 63, 21);
        System.out.println("" + saltUUid.length + Arrays.asList(saltUUid));
    }
}
