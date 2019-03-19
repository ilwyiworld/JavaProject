package com.znv.hbase;

import com.znv.hbase.client.coprocessor.FaceFeatureCompClient;
import com.znv.hbase.client.featureComp.FeatureCompOutData;
import com.znv.hbase.coprocessor.endpoint.FaceFeatureCompImplementation;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import sun.misc.BASE64Encoder;

import java.io.IOException;

/**
 * Created by Administrator on 2016/12/27.
 */
public class TestFaceSearch {

    public static void addCoprocessor(String tableName) throws IOException {
        String coprocessorClassName = FaceFeatureCompImplementation.class.getName();
        HBaseAdmin hbaseAdmin = new HBaseAdmin(HBaseConfig.getConnection());
        try {
            HTableDescriptor htd = hbaseAdmin.getTableDescriptor(TableName.valueOf(tableName));
            if ((false == htd.hasCoprocessor(coprocessorClassName))) {
                if (hbaseAdmin.isTableEnabled(tableName)) {
                    hbaseAdmin.disableTable(tableName);
                }
                // Path jarpath = new Path("file:///usr/hdp/2.4.2.0-258/hbase/lib", "hbase-secondary-index.jar");
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

    public static void main(String[] args) throws Exception {

        try {
            // 初始化
            String zkServer = "znv.bfw01,znv.bfw02,znv.bfw03"; // znv.bfw01,znv.bfw02,znv.bfw03
            String phoenixTablename = "FSS_WHITELIST_TEST2";// "FSS_WHITELIST_TEST"; FSS_BLACKLIST_5
            HBaseConfig.initConnection(zkServer);
            // BASE64Decoder decoder = new BASE64Decoder();
            BASE64Encoder encoder = new BASE64Encoder();

            Get get = new Get(Bytes.toBytes(1));
            HTable table = HBaseConfig.getTable(phoenixTablename);
            Result r = table.get(get);
            byte[] colfamily = Bytes.toBytes("FEATURES");
            byte[] col = Bytes.toBytes("FEATURE0");
            byte[] value = r.getValue(colfamily, col);
            System.out.println("value size:" + value.length);
            System.out.println("value :" + value);
            String encoderFeatrue = encoder.encode(value);
            System.out.println("encoderFeatrue:" + encoderFeatrue);

            // 测试hbase表的数据
            // FEATURE来自hbase表第一条数据
            String FEATURE = "VU4AAAAAAAAMAgAAeVvoO5wpjj6Jmk09SOapPTItajw5qh49qX+2vAMWAL7DFGA9zScHPUJkLD0Hgwy86k/GvDaPnj1Rn729pkavPfYYJT33uis+hiYPPt3toT0Hqkm+L3vEvQRy7jwyEnA9eNxevAJtgL4Wzqs9xG+zPL6LdT2imzS9nNqUPYIVozyz/46+FB6DvVpn0b2Mg/a9BgTNPRZYq72A9lq8FoBLPdtzFT3+EjY936ywPQfSjr3zYY09BWL/u7KVVz3JKWE9yov0PEkYP70MZqi8gyh5vWtv1jkuY5M9jOWLPaDGIL0H01k9X6euvLQkS7zMp1y+owYAPVHd7jy1Gyk9SJkGvb13Jb2Jqwk+iWGkvBTRFz2a3DW+Hz/VPBv/kT3DHki8siqQvQwnOL0kVbc6UroKPdkkSL2gB9g9Lz+0uz9tBrv6wy49uqTcvI57Pz0ZXfq9qfaKvfYC+Dv8iKW9M//HvCJaZL0H8pG9GLkvPr2LmT2jakC7TSkevhqYJL5MJ4w9Yoo3vfJN5T3a2fI9+sAQvtKKND2Qh4c8JctxvURrFr41Ona9XO6TPbY7Ar3V7xC+F9s0PWNUsT0YL6S8MPR/uklg1Tykws098aWjvVGrZ75IVV69pVj0uy7hDL2DT4C9OVYxPC7zSj2qFw69b0OuvOp4/z14r0Q93nyrPUhDEz4=";

            /*
             * BASE64Decoder decoder = new BASE64Decoder(); byte[] decodeFeature = decoder.decodeBuffer(feature1); int
             * rowkey = 33; Scan s = new Scan(); HTable table = HBaseConfig.getTable(phoenixTablename); ResultScanner rs
             * = table.getScanner(s); for(Result r:rs) { byte[] feature =
             * r.getValue(Bytes.toBytes("FEATURES"),Bytes.toBytes("FEATURE0")); int row = Bytes.toInt( r.getRow() );
             * break; }
             */

            FaceFeatureCompClient client = new FaceFeatureCompClient();
            addCoprocessor(phoenixTablename);

            long t1 = System.currentTimeMillis();
            FeatureCompOutData out = client.getFeatureCompResult(HBaseConfig.getTable(phoenixTablename), FEATURE, 0.9f);

            long ts1 = System.currentTimeMillis() - t1;
            System.out.println(", cost " + ts1 + " ms." + " result size =" + out.getFeatureIds().size());

            for (int i = 0; i < out.getFeatureIds().size(); i++) {
                System.out.println(" id  =" + out.getFeatureIds().get(i).getId());
            }

        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            HBaseConfig.closeConnection();
        }

    }
}
