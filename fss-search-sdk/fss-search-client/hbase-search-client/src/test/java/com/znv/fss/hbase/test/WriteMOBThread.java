package com.znv.fss.hbase.test;

import com.alibaba.fastjson.JSON;
import com.znv.fss.hbase.mob.MOBWriteReportServiceIn;
import com.znv.fss.hbase.HBaseManager;
import com.znv.fss.hbase.MultiHBaseSearch;
import com.znv.fss.hbase.mob.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Administrator on 2017/11/10.
 */
public class WriteMOBThread extends Thread {
    private static final Log LOG = LogFactory.getLog(WriteMOBThread.class);
    private static final String mobTableName = "LY_TEST:FSS_BIG_PICTURE_V1_1_3_20170727";

    private static final String IMAGE_DATA = "IMAGE_DATA";
    private static final String KEY = "UUID";
    private ByteArrayOutputStream bos = new ByteArrayOutputStream();

    public WriteMOBThread(/* JSONObject data */) {
        // this.data = data;
    }

    public void run() {

        while (true) {
            // 重组数据
            // JSONObject data = new JSONObject();
            // data.put("id", "32001");
            // JSONArray arry = new JSONArray();
            // //JSONObject insertData = new JSONObject();
            // String filePath = "1.jpg";
            // bos = com.znv.hbase.mob.PictureUtils.image2byte(filePath, bos);
            // UUID uuid = UUID.randomUUID();
            // String uuidStr = uuid.toString().replace("-", "");
            // insertData.put(KEY, uuidStr);
            // insertData.put(IMAGE_DATA, bos.toByteArray());
            // bos.reset();
            // arry.add(insertData);
            // data.put("data", arry);

            // 重组数据
            MOBInputData insertData = new MOBInputData();
            String filePath = "1.jpg";
            PictureUtils.image2byte(WriteMOBThread.class.getResourceAsStream("/" + filePath), bos);//PictureUtils.image2byte(filePath, bos);
            UUID uuid = UUID.randomUUID();
            String uuidStr = uuid.toString().replace("-", "");
            insertData.setUuid(uuidStr);
            insertData.setImageData(bos.toByteArray());
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
                System.out.println("MOB 线程内部错误！");
            }
        }

    }

}
