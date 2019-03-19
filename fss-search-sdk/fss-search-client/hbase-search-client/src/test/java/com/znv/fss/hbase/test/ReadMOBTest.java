package com.znv.fss.hbase.test;

import com.alibaba.fastjson.JSONObject;

import com.znv.fss.common.errorcode.FssErrorCodeEnum;
import com.znv.fss.hbase.HBaseConfig;
import com.znv.fss.hbase.HBaseManager;
import com.znv.fss.hbase.MultiHBaseSearch;

public class ReadMOBTest {

    private static void readBigpicTest() {
        JSONObject params = new JSONObject();
        params.put("id", HBaseManager.SearchId.ReadMOB.getId());
        params.put("type", "request");
        params.put("uuid", "21922b40dbc111e784b4c03fd570e3a6");
        params.put("picType", "big");// big or small
        JSONObject json = new JSONObject();
        json.put("reportService", params);

        MultiHBaseSearch search = HBaseManager.createSearch(json.toJSONString());
        if (search != null) {
            try {
                JSONObject result = search.getJsonResult(json);
                System.out.println(result.toJSONString());

                JSONObject service = result.getJSONObject("reportService");
                int errCode = service.getInteger("errorCode");
                int count = service.getInteger("count");
                if (errCode == FssErrorCodeEnum.SUCCESS.getCode() && count > 0) {
                    byte[] img = service.getBytes("imageData");
                    PictureUtils.savePicture(img, 1, "E:\\project_test\\face");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void readSmallpicTest() {
        JSONObject params = new JSONObject();
        params.put("id", HBaseManager.SearchId.ReadMOB.getId());
        params.put("type", "request");
        params.put("uuid", "2c6acd72ed294ee3bbd36f87d7753975");
        params.put("picType", "small");// big or small
        JSONObject json = new JSONObject();
        json.put("reportService", params);

        MultiHBaseSearch search = HBaseManager.createSearch(json.toJSONString());
        if (search != null) {
            try {
                JSONObject result = search.getJsonResult(json);
                System.out.println(result.toJSONString());

                JSONObject service = result.getJSONObject("reportService");
                int errCode = service.getInteger("errorCode");
                int count = service.getInteger("count");
                if (errCode == FssErrorCodeEnum.SUCCESS.getCode() && count > 0) {
                    byte[] img = service.getBytes("imageData");
                    PictureUtils.savePicture(img, 0, "E:\\project_test\\face");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            // 初始化
            //String hostUrl = "hdfs://lv102.dct-znv.com:8020/user/fss_V113/development";
            String hostUrl = "hdfs://lv94.dct-znv.com:8020/user/fss_V113/development";
            HBaseConfig.initConnection(hostUrl);

            readSmallpicTest();
            readBigpicTest();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 结束，关闭实例
            try {
                HBaseConfig.closeConnection();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
}
