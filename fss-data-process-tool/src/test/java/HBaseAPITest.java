import Write10000W.util.HBaseConfig1;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.client.coprocessor.Batch;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.regionserver.Region;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhuhx on 2018/5/14.
 */
public class HBaseAPITest {

    public static void main(String[] args) {
        try {
            HBaseConfig1.initConnection("10.45.157.120");
            try (HTable table = HBaseConfig1.getTable("ZHX:TEST1")) {

                table.setAutoFlush(false, false);


                table.getConfiguration();
                table.getStartKeys();
                table.getEndKeys();
                //添加、删除列族
                HBaseAdmin admin = new HBaseAdmin(HBaseConfig1.getConnection());
//                admin.disableTable("ZHX:TEST1");
//                HColumnDescriptor hColumnDescriptor = new HColumnDescriptor("sex");
//                hColumnDescriptor.setMaxVersions(1);
//                admin.addColumn("ZHX:TEST1", hColumnDescriptor );
//                admin.modifyColumn("ZHX:TEST1", hColumnDescriptor);
//                admin.enableTable("ZHX:TEST1");

                //put
                byte[] rowkey = new byte[1];
                rowkey[0] = 1;
                rowkey = Bytes.toBytes("2row020013");
                Put put = new Put(rowkey);
                put.addColumn(Bytes.toBytes("ATTR"), Bytes.toBytes("NAME"), Bytes.toBytes("xx"));
                put.addColumn(Bytes.toBytes("ATTR"), Bytes.toBytes("age"), Bytes.toBytes("11"));
                table.put(put);

                //put
                List<Cell> putList = put.get(Bytes.toBytes("ATTR"), Bytes.toBytes("NAME"));
                Map<byte[], List<Cell>> putMap = put.getFamilyCellMap();

                for (Cell cell : putList) {
                    System.out.println(cell.toString());
                    System.out.println(Bytes.toString(cell.getFamily()));
                    System.out.println(Bytes.toString(cell.getQualifier()));
                    System.out.println(Bytes.toString(cell.getValue()));
                }

                for (Map.Entry<byte[], List<Cell>> map : putMap.entrySet()) {
                    System.out.println(Bytes.toString(map.getValue().get(0).getFamily()));
                }

                //get
                Get get = new Get(rowkey);
                Result r = table.get(get);
                r.containsColumn(Bytes.toBytes("ATTR"), Bytes.toBytes("age"));
                System.out.println(r + "r.value:" + Bytes.toString(r.value()));//字典序排在第一列的值
                for (Cell c : r.rawCells()) {
                    System.out.println("cell.value:" + Bytes.toString(c.getValue()));
                }

                //获取缓存区数据
                table.getWriteBuffer();

                //putlist
                List<Put> puts = new ArrayList<>(100);
                puts.add(put);//每个put加入puts
                table.put(puts);

                //getList

                //删除数据
                Delete delete = new Delete(rowkey);
                table.delete(delete);

                //批量操作 batch
                List<Row> rows = new ArrayList<>();
                rows.add(put);
                rows.add(get);
                rows.add(delete);
                rows.add(get);
                Object[] results = new Object[rows.size()];
                try {
                    table.batch(rows, results);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (Object o : results) {
                    System.out.println("result[i]" + o);
                }

                //锁

                //scan [startRow, stopRow)
                Scan scan = new Scan();
                scan.setBatch(10);//rs一次RPC请求10列
                scan.setCaching(20);//rs一次RPC请求20行
                scan.setMaxVersions(1);

                //filter
//                CompareFilter cpFilter = new CompareFilter() {
//                    @Override
//                    public ReturnCode filterKeyValue(Cell cell) throws IOException {
//                        return null;
//                    }
//                };
//                scan.setFilter(cpFilter);

                ResultScanner rs = table.getScanner(scan);
                for (Result result : rs) {
                    byte[] row = result.getRow();
                    Cell[] cells = result.rawCells();
                }

                rs.close();


                //关闭表
                table.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                HBaseConfig1.closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
