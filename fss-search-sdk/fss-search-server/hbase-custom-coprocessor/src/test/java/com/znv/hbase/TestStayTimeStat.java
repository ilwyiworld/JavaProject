package com.znv.hbase;

import com.znv.hbase.client.staytimestat.StayTimeStatParam;
import com.znv.hbase.client.coprocessor.StayTimeStatClient;
import com.znv.hbase.coprocessor.endpoint.StayTimeStatImplementation;
import com.znv.hbase.coprocessor.endpoint.staytimestat.StayTimeSearchOutData;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZNV on 2017/6/8.
 */
public class TestStayTimeStat {
    private static String attrFamily = "ATTR";
    private static String officeIdColumn = "OFFICE_ID";
    private static String cameraIdColumn = "CAMERA_ID";

    public static void addCoprocessor(String tableName) throws IOException {
        String coprocessorClassName = StayTimeStatImplementation.class.getName();
        HBaseAdmin hbaseAdmin = new HBaseAdmin(HBaseConfig.getConnection());
        try {
            HTableDescriptor htd = hbaseAdmin.getTableDescriptor(TableName.valueOf(tableName));
            if ((false == htd.hasCoprocessor(coprocessorClassName))) {
                if (hbaseAdmin.isTableEnabled(tableName)) {
                    hbaseAdmin.disableTable(tableName);
                }
                htd.addCoprocessor(coprocessorClassName);// , jarpath, 1001, (Map)null);
                hbaseAdmin.modifyTable(Bytes.toBytes(tableName), htd);
                hbaseAdmin.enableTable(tableName);
            }
        } catch (TableNotFoundException e) {
            e.printStackTrace();
        } catch (MasterNotRunningException e) {
            e.printStackTrace();
        } catch (ZooKeeperConnectionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<StayTimeSearchOutData> getStatResult(String tableName) {
        List<StayTimeSearchOutData> statData = new ArrayList<StayTimeSearchOutData>();

        String officeIds[] = {"010100000"};
        String cameraIds[] = {""};
        Scan newScan = new Scan();
        FilterList filterList = new FilterList(org.apache.hadoop.hbase.filter.FilterList.Operator.MUST_PASS_ONE);
        if (officeIds != null && officeIds.length > 0) {
            for (String officeId : officeIds) {
                if (officeId != null && !officeId.equals("")) {
                    BinaryComparator comp = new BinaryComparator(Bytes.toBytes(officeId));
                    SingleColumnValueFilter filter = new SingleColumnValueFilter(Bytes.toBytes(attrFamily),
                            Bytes.toBytes(officeIdColumn), CompareFilter.CompareOp.EQUAL, comp);
                    filterList.addFilter(filter);
                }
            }
        }
        if (cameraIds != null && cameraIds.length > 0) {
            for (String cameraId : cameraIds) {
                if (cameraId != null && !cameraId.equals("")) {
                    BinaryComparator comp = new BinaryComparator(Bytes.toBytes(cameraId));
                    SingleColumnValueFilter filter = new SingleColumnValueFilter(Bytes.toBytes(attrFamily),
                            Bytes.toBytes(cameraIdColumn), CompareFilter.CompareOp.EQUAL, comp);
                    filterList.addFilter(filter);
                }
            }
        }

        newScan.setFilter(filterList);
        newScan.addFamily(Bytes.toBytes(attrFamily));
        newScan.addColumn(Bytes.toBytes(attrFamily), Bytes.toBytes(officeIdColumn));
        newScan.addColumn(Bytes.toBytes(attrFamily), Bytes.toBytes(cameraIdColumn));
        newScan.setMaxVersions(1);
        newScan.setCacheBlocks(true);
        newScan.setCaching(100);

        StayTimeStatParam searchParam = new StayTimeStatParam();
        searchParam.setSize(100);
        searchParam.setThreshold(0.92F);
        searchParam.setStartTime("2017-11-01 00:00:00");
        searchParam.setEndTime("2017-11-01 23:59:59");

        // 以图搜图协处理器，返回疑似图片特征值、相似度
        try {
            StayTimeStatClient client = new StayTimeStatClient();
            statData = client.getStayTimeStat(HBaseConfig.getTable(tableName), searchParam, newScan); // 协处理器中已排序
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return statData;
    }

    /**
     * 获取结果，rowKey(alarm_type, sub_type, enter_time, uuid)
     */
    private void getDataFromHbase(List<StayTimeSearchOutData> statData, String tableName) {

        if (statData != null && statData.size() > 0) {
            List<Get> listGets = new ArrayList<Get>();
            for (StayTimeSearchOutData tmp : statData) {
                Get get = new Get(tmp.getRowKey());
                listGets.add(get);
            }
           /* byte[] phoenixGapChar = new byte[1];
            phoenixGapChar[0] = (byte) 0;*/
            HTable table = null;
            try {
                table = HBaseConfig.getTable(tableName);
                Result[] rs = table.get(listGets);
                int index = 0;
                for (Result r : rs) {

                    Cell[] cells = r.rawCells();
                    int len = cells.length;
                    index++;
                    for (int i = 0; i < len; i++) {
                        Cell cell = cells[i];
                        String col = Bytes.toString(CellUtil.cloneQualifier(cell));
                        String value = Bytes.toString(CellUtil.cloneValue(cell));
                        switch (col) {
                            case "PERSON_ID":
                                System.out.print("\n\nPERSON_ID=" + value);
                                break;
                            case "CAMERA_ID":
                                System.out.print("\nCAMERA_ID=" + value);
                                break;
                            case "CAMERA_NAME":
                                System.out.print("\nCAMERA_NAME=" + value);
                                break;
                            case "CAMERA_TYPE":
                                System.out.print("\nCAMERA_TYPE=" + Bytes.toInt(CellUtil.cloneValue(cell)));
                                break;
                            case "GPSX":
                                System.out.print("\nGPSX=" + Bytes.toFloat(CellUtil.cloneValue(cell)));
                                break;
                            case "GPSY":
                                System.out.print("\nGPSY=" + Bytes.toFloat(CellUtil.cloneValue(cell)));
                                break;
                            case "OFFICE_ID":
                                System.out.print("\nOFFICEID=" + value);
                                break;
                            case "OFFICE_NAME":
                                System.out.print("\nOFFICEID=" + value + "\n");
                                break;
                            case "RT_IMAGE_DATA":
                                String imagePath = "picture/" + index + ".jpg";
                                byte[] imageData = CellUtil.cloneValue(cell);
                                savePictureData(imagePath, imageData);
                                break;
                            default:
                                break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (Error e) {
                e.printStackTrace();
            } finally {
                try {
                    if (table != null) {
                        table.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return;
    }

    private void savePictureData(String imagePath, byte[] picture) {
        if (picture == null || picture.length == 0) {
            return;
        }
        File file = null;
        OutputStream out = null;
        try {
            file = new File(imagePath);
            out = new FileOutputStream(file);
            out.write(picture);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != out) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        long t1 = System.currentTimeMillis();
        String zkServer = "10.45.157.113"; // znv.bfw01,znv.bfw02,znv.bfw03
        String tableName = "ZML_FSS_HISTORY_V113_1102";
        try {
            HBaseConfig.initConnection(zkServer);
            addCoprocessor(tableName);

            TestStayTimeStat client = new TestStayTimeStat();
            List<StayTimeSearchOutData> statData = client.getStatResult(tableName);
            client.getDataFromHbase(statData, tableName);

            long t2 = System.currentTimeMillis() - t1;
            System.out.println("\n\n查询耗时：" + (t2) + " 毫秒");
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            HBaseConfig.closeConnection();
        }
    }

}
