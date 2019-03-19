package com.znv.fss.phoenix;

import com.alibaba.fastjson.JSONObject;
import com.znv.fss.phoenix.PhoenixClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/8/2.
 */
public class CameraLibTest {

    private static PhoenixClient pClient = null;
    private static final String tableName = "FSS_CAMERA_LIB_V1_1_3_20170727";

    public CameraLibTest() throws Exception {
        // pClient = new PhoenixClient("jdbc:phoenix:lv101.dct-znv.com:2181:/hbase",
        // "hdfs://lv102.dct-znv.com:8020/user/FSS_V110/sdk/config");

        pClient = new PhoenixClient("jdbc:phoenix:lvt102.dct-znv.com:2181:/hbase",
                "hdfs://lvt102.dct-znv.com:8020/user/fss_V113/development/config");
        
        // phoenixConnURL hdfsFilePath
        // pClient.createTable(tableName);
    }

    private void insert() {
        JSONObject result = new JSONObject();
        JSONObject data = new JSONObject();
        data.put("camera_id", "2");
        data.put("lib_id", 1);
        data.put("camera_name", "camera_A");
        data.put("control_start_time", "2010-01-01 00:00:00");
        data.put("control_end_time", "2017-12-12 23:59:59");

        JSONObject insertData = new JSONObject();
        insertData.put("id", "31003");
        insertData.put("table_name", tableName);
        insertData.put("data", data);

        System.out.println("insert :" + insertData.toString());
        result = pClient.insert(insertData);

        System.out.println("result :" + result.toString());

    }

    private void update() {
        // 测试修改布控的摄像头信息
        JSONObject result = new JSONObject();
        JSONObject data = new JSONObject();
        data.put("camera_id", "1");
        data.put("camera_name", "camera_B");
        data.put("lib_id", 1);
        data.put("control_start_time", "2012-01-01 00:00:00");
        data.put("control_end_time", "2017-12-12 23:59:59");
        JSONObject totalData = new JSONObject();
        totalData.put("data", data);
        totalData.put("original_camera_id", "2"); // 修改布控摄像头

        JSONObject insertData = new JSONObject();
        insertData.put("id", "31003");
        insertData.put("table_name", tableName);
        insertData.put("data", totalData);

        System.out.println("update :" + insertData.toString());

        result = pClient.update(insertData);

        System.out.println("result :" + result.toString());
    }

    private void delete() {
        JSONObject result = new JSONObject();
        JSONObject data = new JSONObject();
        data.put("camera_id", "2");
        data.put("lib_id", 1);
        // data.put("camera_name", "camera_C");
        // data.put("lib_name", "camera_B_lib");
        data.put("control_start_time", "2010-01-01 00:00:00");
        data.put("control_end_time", "2017-12-12 23:59:59");

        JSONObject deleteData = new JSONObject();
        deleteData.put("id", "31003");
        deleteData.put("table_name", tableName);
        deleteData.put("data", data);

        System.out.println("delete : " + deleteData.toString());
        result = pClient.delete(deleteData);

        if ("success".equals(result.getString("errorCode"))) {
            System.out.println(String.format(" delete %s end !", tableName));
        } else {
            System.out.println(String.format(" delete %s %s ", tableName, result.getString("errorCode")));
        }
    }

    private void query() throws Exception {
        JSONObject queryTerm = new JSONObject();
        queryTerm.put("camera_id", "1");

        JSONObject queryMulti = new JSONObject();
        List<Integer> libId = new ArrayList<Integer>();
        libId.add(1);
        libId.add(2);
        queryMulti.put("lib_id", libId);

        JSONObject insertData = new JSONObject();
        insertData.put("query_term", queryTerm);
        //insertData.put("query_multi", queryMulti);
        insertData.put("id", "31003");
        insertData.put("table_name", tableName);
        insertData.put("count", -1);
        insertData.put("total_page", -1);
        insertData.put("page_no", 1);
        insertData.put("page_size", 4);

        System.out.println("query :" + insertData.toString());

        JSONObject rs = pClient.query(insertData);

        System.out.println("result :" + rs.toString());

    }

    private void getAll() {
        JSONObject insertData = new JSONObject();
        insertData.put("id", "31003");
        insertData.put("table_name", tableName);

        System.out.println(" sql :" + insertData.toString());
        JSONObject result = pClient.getAll(insertData);
        System.out.println(" getAll result :" + result.toString());
    }

    private void getConfigAll(){
        JSONObject insertData = new JSONObject();
        insertData.put("id", "31007");
        insertData.put("table_name", "FSS_LIB_CONFIG_V1_1_3_20170727");

        System.out.println(" config sql :" + insertData.toString());
        JSONObject result = pClient.getAll(insertData);
        System.out.println(" config result :" + result.toString());
    }

    private void configInsert(){
        JSONObject insertData = new JSONObject();
        insertData.put("id", "31007");
        insertData.put("table_name", "FSS_LIB_CONFIG_V1_1_3_20170727");

        JSONObject data = new JSONObject();
        data.put("lib_id",1);
        data.put("lib_name","lib01");
        data.put("personlib_type","type01");
        data.put("plib_alarm_level","alarmLevel02");
        data.put("creator_id","01");
        insertData.put("data",data);

        System.out.println(" config insert sql :" + insertData.toString());
        JSONObject result = pClient.insert(insertData);
        System.out.println(" config insert result :" + result.toString());
    }

    private void configDelete (){
        JSONObject insertData = new JSONObject();
        insertData.put("id", "31007");
        insertData.put("table_name", "FSS_LIB_CONFIG_V1_1_3_20170727");
        JSONObject data = new JSONObject();
        data.put("lib_id",1);
        insertData.put("data",data);

        System.out.println(" config delete sql :" + insertData.toString());
        JSONObject result = pClient.delete(insertData);
        System.out.println(" config delete result :" + result.toString());

    }

    private void configQuery(){
        JSONObject queryTerm = new JSONObject();
        queryTerm.put("lib_id", 2);

        JSONObject queryMulti = new JSONObject();
        List<String> libName = new ArrayList<String>();
        libName.add("lib03");
        libName.add("lib01");
        queryMulti.put("lib_name", libName);

        JSONObject insertData = new JSONObject();
        insertData.put("query_term", queryTerm);
        insertData.put("query_multi", queryMulti);
        insertData.put("id", "31007");
        insertData.put("table_name", "FSS_LIB_CONFIG_V1_1_3_20170727");
        insertData.put("count", -1);
        insertData.put("total_page", -1);
        insertData.put("page_no", 1);
        insertData.put("page_size", 4);

        System.out.println("query :" + insertData.toString());

        JSONObject rs = pClient.query(insertData);

        System.out.println("result :" + rs.toString());
    }

    private void deleteLibId() {
        JSONObject insertData = new JSONObject();
        insertData.put("id", "31003");
        insertData.put("lib_id", 1);

        System.out.println(" DELETE LIB ID ：" + insertData.toString());
        JSONObject rs = pClient.deleteLibId(insertData);
        System.out.println(" delete lib id result :" + rs.toString());

    }

    public static void main(String[] args) {
        try {
            CameraLibTest test = new CameraLibTest();

             //test.insert();

            //test.update();

             //test.delete();

             //test.query();

           // test.getAll();

            //test.getConfigAll();

            //test.configInsert();

            //test.configDelete();

            test.configQuery();
            test.deleteLibId();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
