package com.znv.hbase;

import com.znv.hbase.client.coprocessor.ImageSearchClient;
import com.znv.hbase.client.featureComp.ImageSearchParam;
import com.znv.hbase.coprocessor.endpoint.ImageSearchImplementation;
import com.znv.hbase.coprocessor.endpoint.searchByImage.ImageSearchOutData;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by estine on 2016/12/27.
 */
public class TestImageSearch {
    public static void addCoprocessor(String tableName) throws IOException {
        String coprocessorClassName = ImageSearchImplementation.class.getName();
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
        long t1 = System.currentTimeMillis();

        String zkServer = "lv95.dct-znv.com,lv96.dct-znv.com,lv97.dct-znv.com"; // znv.bfw01,znv.bfw02,znv.bfw03
        HBaseConfig.initConnection(zkServer);
        String TABLENAME = "FSS_HISTORY_V113_LY"; // FSS_imageData_searchByImage estine_senseTime_20161226_test
        List<String> cameraIds = new ArrayList<String>();
        addCoprocessor(TABLENAME);

        try {

            // test
//            Base64 base64 = new Base64();
//            Scan s = new Scan();
//            // s.setStartRow(Bytes.toBytes("10-9223370550499258394-44942-201701241623030001000000207-161893"));
//            // s.setStopRow(Bytes.toBytes("10-9923370550499258394"));
//            s.setStartRow(Bytes.toBytes("10-9223370548393808807-935863-201703021943450001000000043-1889900"));
//            s.setStopRow(Bytes.toBytes("10-9323370548393808807"));
//            HTable table = HBaseConfig.getTable(TABLENAME);
//            ResultScanner rs = table.getScanner(s);
//            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            Calendar cal = Calendar.getInstance();
//
//            // String FEATURE =
//            // "VU4AAAAAAAAMAgAAfRc1vjEJkr2SZuS9PT50vWivZ7z2qHw8hRL0Pc1fojyykSc9Kuv6vbEVqLy6zS++B3vxOzzrmj04sCs+zha6PbL7dbwB11e81KuwPS561Dw4UCe+CrJ1PR6BGT2tir89oRZJPiEUmjzFcTO9CrQzvUAt6L2UrnA9wz/IPYQXyD16BrI8ti3+vfCmEr2Qaiu94ZgXPQXz1jxjzry8PAf4PS/FDL5ybno8qyaZPfO4nb2UpAC7qg09PdMd5DzPLF691UucPZuSRD2krcq9A1zZPbJ4Dj4XLXC9B/1svUSR4j0hp+E8wIK/vdB9xD3NSVI8PdXuPNTO672zmmc8BcfZvenHizw3vxY9vecivPNDZb31BeS9E4sHPdrgmbtE77A9SAnPPXlxgz1fGmU7CU7AOxvGDr23xiG+0NrePF21FT0QfkW+f2SLvVA1ib2MG6y92aUgPJNlnT2uz6G97bECvijk9zyrB389R0TXPHo09zvovf29fx/CPOPioTwFtag9X4m1vZA2KT5h85u9GEcLPT8jQj6TCag9RR/nvT75lbxZcWY93Y78O0FcBTzb64099Bg7Pfib5r1UP889npRAPTcO4T046/874FvpPAo/vz2ubuc9ICypvBuRUz2sHJ+9wA7jvH6UK7647Fs+vGMsvvmjhT6udly939cMu93wMj0=";
//            String FEATURE = ""; // 按时间查询

            List<String> FEATURES = new ArrayList<String>(3);
            // String[] FEATURES =new String[3];
            String FEATURE1 = "7qpXQpFlAACAAAAAhEaavHSKWLypcTK7xqM8PCGmgz1g4pw9qq+hvQGdML6qCHM9yiaOPbUOEb7Zn3I9EPFlPQx30r0YVp88keoWPeGlXTu6Yzc83L+MvXCQHr4p4pw9Z0oYvhXLej2k2yw9EIl1PQNH5Dud7sK9gnwMPEdODjxBWGK9mRbkPQz0Dz7zV667BIDbvETPh73Yqxs8QWfCvErlbj1GDm29mB71vbw6jbzlHhq+5d0zvSOytTt2GFO8hCryPfzjWzx9EzE8oyGlPS9CMT5JeeW9BFybO+sMDLznoA69NJ/jPPuLUD2NHQo+pkApvtyVmzxbcVm+OPjfPYlE2L127rO7lsbZPT2dqzxd5Wq9GpE5PVOmCr1xaYE9a5+9PBtKOL68N2u79xeDvUIpwzwnmD29292svQzTXjydkkc9PPVkvOUbH742i5g9Taxvvdb5Vr3agXE9Y5DuPM05ub1zyM08fzlTPYhFI7xpWFw9Ng4MPqpZO71nuAI9MIDzPHqxRD1tOoS+97MsveaXHr5oORU+aixJvljImTue6VI9mVvHPVu2Mj7cQIQ9pdBKPU8dOL1FPhU+cbeRPZdPqz0wGya9Fd1uvm2GGbwKF4q9Tc+ava3bHz1xuaw9xhROvlfGOj2kvOs8Pd9XvV+EQL1bTNq8elUxvpLoAb7RDFS9ocrtu9CRbzw=";
            String FEATURE2 = "7qpXQpFlAACAAAAApdNqvfyz8btF+Is9i85+PQ1vFj5ERh48BuVvvZvRjTxYP7I9B54pO9jH5zxPzpS9mxV7PnWNp7x42968SYf9vF1YAj74kCq82V9JvCf79bvKq/28JTcEvnTuK7wNQzo8hW/Lvcrjaj2kGbK8svZZPth0GD2yAaK7G09UPWgwib3aZL69tOphPYlnFz2K5iC96XMlvUGBkTyYfuI9ABQOvvEtDD7R3iC+a/PEvTG18D0JD0O9UMq/vJjOorz7jsg8RjecvKfeFj7WqHI96X6nvRMYeD25itO9SbTEvYT7Oz5kn38+Uk0KvTnuPTwhEbU8gVAKPlXH2L08iEk+L8GdPAm7BL2t75s9Cm8KvgJAZDzXr849Jm6oPceNaL3+Yy2978jvvYX7B713Uhy+66fgvaJXgr0nL5a9bEiUvcOp670C8Em99TiPvRaXZb34AJW9o6UxPABUlT1sJTU+ILUYPZHTsTzkUxU9iO1DPDzj6ToGEJi9x+UyPWUbRj16zJw7NO0mu28OLT6k/M49FnsbPKTk0j0Fpu48k6oGvqXJl72uAKY5DXRivXXdnbuvrRY9Ai1BPjwvSDxHk9e9CkpWvV4BLr0WFEK9wr3HvA2CpT0tg/O98v+XvSPfDj7Glgg+SCklO0tOFT5AL5g9vQJfvQoJhj3qxxc9uTtuvXIEDz4=";
            String FEATURE3 = "7qpXQpFlAACAAAAAs/VJvfQdgjyNWgk+PydavQR8lD2bk6M9tB99vQkyWjvvC5E9YFm5PfJNpz1qqFY8MhWrPciK2D0HP2i72UtWvdMe1z1k+SW9BQUfPRsnIr5KsxC+WrFhvfIxvb2sBVW9ve3MPUBFUr3Zaus9PhKSPWX1ur36vb+7nIo3vTqHBj50AdM8tF8jvDXejrxwokQ9EL4nO+ljgrwrXvu9r2qWPF67OTyXVsm5SNfjvBLvDr7r4p49EMe0vc7z/TpbZqE8KpoJvsXykj5vMBI+0x47PevaDT6WYAs9fsAtveEqYL0wc3Y8OVE6vRMxlL0804m9JyVuvrrCqLw8ljO9z3TgPUcNsDwjBiu+m6KXvV/I4r3izpC8cNpCPVXKPT3KeIs9K62KvTByFL2Rfw49g3GovKrW1Tz+pLk9UtT2vZlK6b3P4Bo+G9vLvOzimL2CAqC9hYZ/vQBJijya1Va9E2gLvjrJVzuy00G9tlgovlnwProH+yy92zClvdy6vDw4A0c8xJySvXy4N70oZBY+qtSgPdG8/T21r/Y6+sEruw6hTD6E5le92hMFvhbNR77hEJE9whKcvcPbBDwQIKQ9MyXpvAL0ab0UdII8KZvqPQCY7D1V4pS9VAl7PVPbfT5Yoks7+DyuvXomMry9Hcu9GBCdPLjd+DxJfdm8IeATPheM/7w=";
            FEATURES.add(FEATURE1);
            FEATURES.add(FEATURE2);
            FEATURES.add(FEATURE3);

//            for (Result r : rs) {
//                int i = 20;
//                if (i > 0) {
//                    String row = Bytes.toString(r.getRow());
//                    String[] keys = row.split("-");// salting-time-trackId-taskId-resultId
//                    String catchTime = keys[1];
//                    long dt = Long.MAX_VALUE - Long.parseLong(catchTime);
//                    cal.setTimeInMillis(dt);
//                    // System.out.println("catchTime :" + (sdf.format(cal.getTime())));
//                    // System.out.println("rowKey :" + row);
//                    byte[] value = r.getValue(Bytes.toBytes("feature"), Bytes.toBytes("feature"));
//                    byte[] value0 = r.getValue(Bytes.toBytes("picture"), Bytes.toBytes("imageData"));
//                    byte[] value1 = r.getValue(Bytes.toBytes("feature"), Bytes.toBytes("cameraId"));
//                    // System.out.println("cameraId :" + Bytes.toString(value1)); // 85
//                    // System.out.println("imageData = " + encoder.encode(value0));
//                    // System.out.println("value size:"+value.length);
//                    String encoderFeatrue = base64.encodeAsString(value);
//                    // String encoderFeatrue = encoder.encode(value);
//                    // System.out.println("encoderFeatrue:" + encoderFeatrue);
//
//                    /*
//                     * //TEST FACE COMP FeatureCompUtil fc = new FeatureCompUtil(); float sim =
//                     * STFeatureCompare.featureCompJava(fc,value, value); System.out.println(" 相似度测试结果 =" + sim);
//                     */
//
//                    /*
//                     * // test save picture ! // 将字符串转换成二进制，用于显示图片 // 将上面生成的图片格式字符串 imgStr，还原成图片显示
//                     * E:\FssProgram\V1.0\全表扫描\查询图片 String fileName = ""; fileName =
//                     * "E:\\FssProgram\\V1.0\\全表扫描\\00-单机测试\\131查询图片\\" + "02" + "-" + Bytes.toString(value1) + ".jpg";
//                     * OutputStream o = new FileOutputStream(fileName); InputStream in = new
//                     * ByteArrayInputStream(value0); byte[] b = new byte[1024]; int nRead = 0; while ((nRead =
//                     * in.read(b)) != -1) { o.write(b, 0, nRead); } o.flush(); o.close(); in.close();
//                     */
//
//                    i -= 1;
//                    break;
//                }
//            }

            String cameraId1 = "96";
            String cameraId2 = "31";
            String cameraId3 = "70";
            // cameraIds.add(cameraId1);
            // cameraIds.add(cameraId2);
            // cameraIds.add(cameraId3);
            float THRESHOLD = 0.8f;  //0.00—1.00  0.5924f 归一化前阈值
            String startTime = "2017-02-20 00:00:00";
            String endTime = "2017-08-20 23:59:59";

            ImageSearchParam param = new ImageSearchParam();
            param.setSearchFeatures(FEATURES);
            param.setThreshold(THRESHOLD);
           // param.setCameraIds(cameraIds);
            param.setStartTime(startTime);
            param.setEndTime(endTime);

            ImageSearchClient client1 = new ImageSearchClient();
            List<ImageSearchOutData> suspectList = client1.getSearchByImageResult(HBaseConfig.getTable(TABLENAME),
                param);

            System.out.println("suspectList.size= " + suspectList.size());
            for (ImageSearchOutData res : suspectList) {
                // test 测试是否有输出
                System.out.println("rowkey = " + res.getSuspectRowKey());
                // System.out.println("sim = " + res.getSuspectSim());

            }
            long t2 = System.currentTimeMillis() - t1;
            System.out.println("统计耗时： " + (t2) + " 毫秒");

            // test rowkwy是否有相同！
            {
                for (int i = 0, len = suspectList.size(); i < len; i++) {
                    byte[] str1 = suspectList.get(i).getSuspectRowKey();
                    for (int j = 0; j < i; j++) {
                        byte[] str2 = suspectList.get(j).getSuspectRowKey();
                        if (Bytes.equals(str1,str2)) {
                            System.out.println("相同 rowkey = " + str1);
                        }
                    }
                    for (int k = i + 1; k < len; k++) {
                        byte[] str3 = suspectList.get(k).getSuspectRowKey();
                        if (Bytes.equals(str1,str3)) {
                            System.out.println("相同 rowkey = " + str1);
                        }
                    }
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            HBaseConfig.closeConnection();
        }

    }

}
