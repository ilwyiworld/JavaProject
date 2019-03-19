package com.znv.fss.hbase.test;

import com.znv.fss.hbase.HBaseConfig;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2016/10/20.
 */
public class CreateElectrTest {

    private static String tableName = null;
    private Random rnd = new Random();
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final String startDate = "2015-01-01 00:00:00";

    // 通过静态方法更新静态属性
    public static void setTableNameValue(String tableName) {
        CreateElectrTest.tableName = tableName;
    }

    // findbug不支持通过实例方法更新静态属性
    // public CreateElectrTest(String tableName) {
    // this.tableName = tableName;
    // }

    private static long randomNum(long begin, long end) {
        long rtn = begin + (long) (Math.random() * (end - begin));
        if (rtn == begin || rtn == end) {
            return randomNum(begin, end);
        }
        return rtn;
    }

    public static Date randomDate(String beginDate, String endDate) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date start = format.parse(beginDate);
            Date end = format.parse(endDate);
            if (start.getTime() >= end.getTime()) {
                return null;
            }
            long date = randomNum(start.getTime(), end.getTime());
            return new Date(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Put produceElectrData() {
        String startDate = "2015-01-01 00:00:00";
        String endDate = "2016-10-20 23:59:59";
        Date reporttime = randomDate(startDate, endDate);
        String[] pathId = { "01/01/03", "01/01/03/04", "01/01/05", "01/06", "01/07" };
        String[] pathName = { "中心/南京/机房3", "中心/南京/江宁/机房4", "中心/南京/机房5", "中心/机房6", "中心/机房7" };
        String[] stationId = { "03", "04", "05", "06", "07" };
        String[] stationName = { "机房3", "机房4", "机房5", "机房6", "机房7" };
        String[] type = { "01", "02", "03" };
        String[] typeName = { "总用电量", "主设备用电", "空调用电" };
        // String[] reptype = { "01", "02", "03", "04" };// 小时能耗 日能耗 月能耗 年能耗
        byte[] family = Bytes.toBytes("family");// family monitoring
        Random rand = new Random();
        int num = rand.nextInt(5);
        int num2 = rand.nextInt(3);
        // num =0;
        // num2=2;

        double f = rand.nextDouble();
        double valuet = Double.valueOf(f * 20);
        BigDecimal b = new BigDecimal(valuet);
        double value = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

        byte[] rowkey = Bytes.add(Bytes.toBytes(type[num2]), Bytes.toBytes(Long.MAX_VALUE - reporttime.getTime()),
            Bytes.toBytes(stationId[num]));
        String tttt = String.valueOf(value);
        System.out.println("输出随机能耗值");
        System.out.println(value);
        System.out.println(tttt);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Put put = new Put(rowkey);
        System.out.println(sdf.format(reporttime));
        put.add(family, Bytes.toBytes("sampling_time"), Bytes.toBytes(sdf.format(reporttime)));
        put.add(family, Bytes.toBytes("electricity_id"), Bytes.toBytes(type[num2]));
        put.add(family, Bytes.toBytes("electricity_name"), Bytes.toBytes(typeName[num2]));
        put.add(family, Bytes.toBytes("electricity_value"), Bytes.toBytes(value));
        put.add(family, Bytes.toBytes("station_id"), Bytes.toBytes(stationId[num]));
        put.add(family, Bytes.toBytes("station_name"), Bytes.toBytes(stationName[num]));
        put.add(family, Bytes.toBytes("parent_path"), Bytes.toBytes(pathId[num]));
        put.add(family, Bytes.toBytes("parent_path_name"), Bytes.toBytes(pathName[num]));
        return put;
    }

    public static void monidata() {
        int cnt = 30000;
        int n = 0;
        // 插入数据
        HTable table = null;
        try {
            table = HBaseConfig.getTable(tableName);
            // System.out.println(table);
            table.setAutoFlush(false, false);

            List<Put> puts = new ArrayList<Put>();
            for (int i = 0; i < cnt; i++) {

                puts.add(produceElectrData());
            }
            table.put(puts);

        } catch (IOException e) {
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
        // 线程结束时计数器减1
    }

    public static void main(String[] args) throws Exception {
        try {
            // 初始化
            String hostUrl = "hdfs://znv.bfw02:8020";
            HBaseConfig.initConnection(hostUrl);
            // String filePath = "hdfs://znv.bfw02:8020/user/hbaseconfig/HBaseConfAndTables-DCIM.json";
            // HBaseConfig.initConnection(filePath);
            String tablename = "lq_electr_test_0324";
            // 创建表
            // CreateElectrTest tclass = new CreateElectrTest(tablename);

            setTableNameValue(tablename); // 通过静态方法更新静态属性
            CreateElectrTest tclass = new CreateElectrTest();
            Long t1 = System.currentTimeMillis();
            System.out.println("Begin put dada");
            tclass.monidata();
            System.out.println("End");
            Long t2 = System.currentTimeMillis() - t1;
            System.out.println(String.format("time is %s ms", t2));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 结束，关闭实例，释放资源
            HBaseConfig.closeConnection();
        }

    }

}
