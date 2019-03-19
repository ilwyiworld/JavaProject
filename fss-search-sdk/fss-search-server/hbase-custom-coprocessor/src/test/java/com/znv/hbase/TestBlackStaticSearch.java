package com.znv.hbase;

import com.google.protobuf.ByteString;
import com.znv.hbase.client.coprocessor.BlackStaticSearchClient;
import com.znv.hbase.client.featureComp.ImageSearchParam;
import com.znv.hbase.coprocessor.endpoint.BlackStaticSearchImplementation;
import com.znv.hbase.coprocessor.endpoint.searchByImage.ImageSearchOutData;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by estine on 2017/2/20.
 */
public class TestBlackStaticSearch {
    public static void addCoprocessor(String tableName) throws IOException {
        String coprocessorClassName = BlackStaticSearchImplementation.class.getName();
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

    public static List<ImageSearchOutData> getResults(String tableName, long currentTime) {
        BlackStaticSearchClient client = new BlackStaticSearchClient();
        List<ImageSearchOutData> suspectList = new ArrayList<ImageSearchOutData>();
        try {
            float THRESHOLD = 0.5924f; // 0.5924
            List<String> featureList = new ArrayList<String>(1);
            String FEATURE = "VU4AAAAAAAAMAgAAPSO4vOMVULzKoOY8lfYyPFBWwrwj1Ze99Qw7vk+2cr0QXIC8+J9fvWzDo723vCK9Cz4gPrbAmj1rwYK9o9X4vRpNUjy3b+y8LS4tvsYR47xZWLi93c5jPSUknD03R+29W6bOvfIPyD3apAe9bQArPf9i4rwR9cy9mYfyPbESRr0lY7c9ZoAxPRDavzyVvIq9aL66vd7liT2moWQ+5rqUvPEHCD7CdDO9wUGUPVeO7b2xDbs8nNftvZr/Ur322h49T4XGPeSp2jt7/4W9PGeWvZvogj0NHaq9ttKuvV6Mqr044Ea91OukvRWvnr1znMM9KRpuPWr7VT0uf4A8SI3XvUuGhL0Kdgi+dIUWvXHRAL1n8Lm9oLPNPKp7Pr4l4qc9o5gPO2M+RL7b+eW9bQbgPU5yzDs4QDC9oSoVPf+GmjvbmCY8SaZRu4gTDb4cBF++US0xvKojpjwZ18+9gErxOw/0sT06bS49Ftd2Papbpb3aZRM+EImjPSoKCr5IpIw9/w9PPAXEDr6CyoA9d/qlvXsAzryxng4+oDLCPdKxeb3bRSA+bhDNvZlTbz05p+S7GH42PR3NJD2T1FG6HI1VvfznBb7gG/A976E5vhXu8D0afEM9BFPUPfL0sD1gfjy8Era6Pb90yzz9H+W5pd4APe495D3lDT++OAIBPhB5e7w=";
            featureList.add(FEATURE);
            ImageSearchParam param = new ImageSearchParam();
            param.setSearchFeatures(featureList);
            param.setThreshold(THRESHOLD);
            suspectList = client.getBlackStaticSearchResult(HBaseConfig.getTable(tableName), param);
            System.out.println("suspectList.size= " + suspectList.size());
            for (ImageSearchOutData temp : suspectList) {
                System.out.println("rowkey = " + temp.getBytesRowKey());
                System.out.println("sim = " + temp.getSuspectSim());
            }

            long t2 = System.currentTimeMillis() - currentTime;
            System.out.println("统计耗时： " + (t2) + " 毫秒");

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return suspectList;
    }

    public static void savePicture(String TABLENAME, List<ImageSearchOutData> suspectList) {
        Scan s = new Scan();
        for (ImageSearchOutData list : suspectList) {
            ByteString rowkey = list.getBytesRowKey();
            s.setStartRow(rowkey.toByteArray());
            try {
                HTable table = HBaseConfig.getTable(TABLENAME);
                ResultScanner rs = table.getScanner(s);
                for (Result r : rs) {
                    int num = 20;
                    if (num > 0) {
                        int row = Bytes.toInt(r.getRow());
                        byte[] value = r.getValue(Bytes.toBytes("FEATURES"), Bytes.toBytes("FEATURE")); // FEATURES:FEATURE
                        byte[] value0 = r.getValue(Bytes.toBytes("PICS"), Bytes.toBytes("IMAGEDATA")); // PICS:IMAGEDATA
                        System.out.println("row = " + row);
                        System.out.println("FEATURE size:" + value.length);

                        // test save picture !
                        // 将字符串转换成二进制，用于显示图片
                        // 将上面生成的图片格式字符串 imgStr，还原成图片显示 E:\FssProgram\V1.0\全表扫描\查询图片
                        String fileName = "";
                        fileName = "E:\\FssProgram\\V1.0\\全表扫描\\02-黑名单静态比对\\历史图片\\" + "06" + "-" + (0 - row) + ".jpg";
                        OutputStream o = new FileOutputStream(fileName);
                        InputStream in = new ByteArrayInputStream(value0);
                        byte[] b = new byte[1024];
                        int nRead = 0;
                        while ((nRead = in.read(b)) != -1) {
                            o.write(b, 0, nRead);
                        }
                        o.flush();
                        o.close();
                        in.close();
                        num -= 1;
                        break;
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        long currentTime = System.currentTimeMillis();
        String zkServer = "znv.bfw04"; // znv.bfw01,znv.bfw02,znv.bfw03 znv.bfw04
        HBaseConfig.initConnection(zkServer);
        String TABLENAME = "FSS_BLACKLIST_TEST_0117"; // 94白名单静态比对
        List<ImageSearchOutData> suspectList = new ArrayList<ImageSearchOutData>();
        addCoprocessor(TABLENAME);
        suspectList = getResults(TABLENAME, currentTime);
        savePicture(TABLENAME, suspectList);

    }
}
