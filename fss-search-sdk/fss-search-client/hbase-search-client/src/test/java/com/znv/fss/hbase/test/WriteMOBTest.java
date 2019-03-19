package com.znv.fss.hbase.test;

import com.alibaba.fastjson.JSON;
import com.znv.fss.hbase.HBaseConfig;
import com.znv.fss.hbase.HBaseManager;
import com.znv.fss.hbase.MultiHBaseSearch;
import com.znv.fss.hbase.mob.MOBInputData;
import com.znv.fss.hbase.mob.MOBWriteJsonInput;
import com.znv.fss.hbase.mob.MOBWriteParam;
import com.znv.fss.hbase.mob.MOBWriteReportServiceIn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//import java.sql.Connection;

/**
 * Created by Administrator on 2017/11/10.
 */
public class WriteMOBTest {
    private static ByteArrayOutputStream bos = new ByteArrayOutputStream();

    private static void multiWriteMOB() {
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);
        CountDownLatch threadSignal = new CountDownLatch(1);
        System.out.println(" It's keep writing now,please wait ......");

        for (int i = 0; i < 4; i++) {
            Thread t = new WriteMOBThread();
            t.start();
        }

        try {
            // threadSignal.wait();
            threadSignal.await(1000 * 60 * 60 * 1, TimeUnit.MILLISECONDS); // 设置60秒超时等待
        } catch (InterruptedException e) {
            fixedThreadPool.shutdownNow();
            e.printStackTrace();
        }
        fixedThreadPool.shutdown();

    }

    private static void writeTest() {
        // 重组数据
        MOBInputData insertData = new MOBInputData();
        String filePath = "1.jpg"; // E:\FssProgram\V1.1\11-HBaseMOB\03-读性能测试\1.jpg
        // PictureUtils.image2byte(WriteMOBTest.class.getResourceAsStream("/" + filePath), bos);
        byte[] image = PictureUtils.image2byte(filePath);
        UUID uuid = UUID.randomUUID();
        String uuidStr = uuid.toString().replace("-", "");
        System.out.println("uuidStr :" + uuidStr);
        insertData.setUuid(uuidStr);
        // insertData.setImageData(bos.toByteArray());
        insertData.setImageData(image);
        bos.reset();

        List<MOBInputData> datas = new ArrayList<MOBInputData>(2);
        datas.add(insertData);

        MOBWriteParam param = new MOBWriteParam();
        param.setData(datas);

        MOBWriteReportServiceIn serviceIn = new MOBWriteReportServiceIn();
        serviceIn.setId("12009");
        serviceIn.setType("request");
        serviceIn.setMobWriteParam(param);
        MOBWriteJsonInput inputParam = new MOBWriteJsonInput();
        inputParam.setReportservice(serviceIn);

        String str = JSON.toJSONString(inputParam);

        try {
            MultiHBaseSearch search = HBaseManager.createSearch(str);
            System.out.println(search.getJsonResult(str));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("主线程写入错误！");
        }
    }

    private static void writeSmallPictureTest() {
        // 重组数据
        MOBInputData insertData = new MOBInputData();
        String filePath = "3.jpg";
        PictureUtils.image2byte(filePath, bos);
        UUID uuid = UUID.randomUUID();
        String uuidStr = uuid.toString().replace("-", "");
        System.out.println("uuidStr :" + uuidStr);
        insertData.setUuid(uuidStr);
        insertData.setImageData(bos.toByteArray());
        bos.reset();

        List<MOBInputData> datas = new ArrayList<MOBInputData>(2);
        datas.add(insertData);

        MOBWriteParam param = new MOBWriteParam();
        param.setData(datas);

        MOBWriteReportServiceIn serviceIn = new MOBWriteReportServiceIn();
        serviceIn.setId("12010");
        serviceIn.setType("request");
        serviceIn.setMobWriteParam(param);
        MOBWriteJsonInput inputParam = new MOBWriteJsonInput();
        inputParam.setReportservice(serviceIn);

        String str = JSON.toJSONString(inputParam);

        try {
            MultiHBaseSearch search = HBaseManager.createSearch(str);
            System.out.println(search.getJsonResult(str));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("主线程写入错误！");
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            String hostUrl = "hdfs://10.45.157.102:8020/user/fss_V113/development";
            // String hostUrl = "hdfs://lv102.dct-znv.com:8020/user/fss_V113/development";
            HBaseConfig.initConnection(hostUrl);
            // multiWriteMOB();
            writeTest();

            // writeSmallPictureTest();

            HBaseConfig.closeConnection();
        } catch (IOException e) {
            System.out.println(" 初始化异常！");
        }
    }
}
