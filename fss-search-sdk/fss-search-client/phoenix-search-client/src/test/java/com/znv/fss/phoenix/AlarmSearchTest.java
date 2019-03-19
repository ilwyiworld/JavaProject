package com.znv.fss.phoenix;

import com.alibaba.fastjson.JSONObject;
import com.znv.fss.common.VConstants;
import com.znv.fss.phoenix.PhoenixClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 2017/8/2.
 */
public class AlarmSearchTest {
    private static PhoenixClient pClient = null;
    private static final String tableName = "FSS_ALARM";
    // private static final String tableName = "FSS_ALARM_V1_1_3_20170727";

    public AlarmSearchTest() throws Exception {
        String hdfsFilePath = "hdfs://face.dct-znv.com:8020/user/fss/V120/config";
        pClient = new PhoenixClient("jdbc:phoenix:face.dct-znv.com:2181:/hbase", hdfsFilePath);
    }

    public void query() throws Exception {
        JSONObject queryInfo = new JSONObject();
        JSONObject queryTerm = new JSONObject();
        JSONObject queryMulti = new JSONObject();

        // lq-V242
        String feature = "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";

        // 按图片查询
//         queryTerm.put("feature",
//         "7qpXQpFlAACAAAAAHuvvu+KhAL6gPde9aZPUvXgR7row5Ug9xfx9PMmwND20SEi9F2JPvDZZtr3EKwi8xMASvjmiOrxiOTy966yxPRZOo73vwhy8cGwjPYwQcT2skQy9RkNbvKlBIb6jvwO+O4fSPB+UMD3iCuM7Ew72PN3/sL3rDUe9jFyKPm7WqLvKjBA9Xqb4vfcUqr1peKk9inx1PaB4WD2R7Ly9xEq+Pc3Z9TtOwVU+MRjdPUva0D2Y6YK9AYoYPU22+L36JlI952jSPVDwTjzpoEq9B18gvqb6XT2zPhc+KrpAvZF1IT4avoE8AEfBPQbpjj0l1ro9DLH9PV2lKr0lpZm9LTS9PTib2z1IQoW9HGgAPYq2Ir71uJc+rqacPFy23r2DXi28uN8aPYhbRz4gzLE8hzMrvmDP+DtsKrg9FXC3PeO7pr3iqQ09MOBoPRxCdb3B5qk9nZLRPVQeerzxuua8fUj4vc4jRT2SVHA9pBCDvN15Ob34KF69N6GSvX8CJ71ijH29T9GMunv8ET3W4As9TJ2LvQ4Fqb1N/lo9CY3yvLcqXr3LPAO9iEvvPDrpljwf3SK+RfzrvN8Yo70oke885QecvWw2WboIf/Y9yHK2PUk/gTyH8vm9NiTKvSZuk73mH3E9Rv2HPNV3rj3Z+cE9jfoUvvROND3Xroa9VcwwPqKhiL0=");
        queryTerm.put("feature",feature);
         queryTerm.put("sim", 90);
         queryInfo.put("query_term", queryTerm);

        JSONObject queryRange = new JSONObject();
        queryRange.put("start_time", "2018-05-24 00:00:00");
        queryRange.put("end_time", "2018-05-24 23:59:59");
        queryRange.put("order_field", "1"); // 1:告警时间
        queryRange.put("order_type", "1"); // 1:升序
        queryInfo.put("query_range", queryRange);

        queryInfo.put("page_no", 1);
        queryInfo.put("page_size", 1);
        queryInfo.put("total_page", -1);
        queryInfo.put("count", -1);

        // 指定摄像头id
        // List<String> cameraIdList = new ArrayList<>();
        // cameraIdList.add("124");
        // queryMulti.put("camera_id", cameraIdList);
        // data.put("query_multi", queryMulti);

        // 指定告警类型
        // List<Integer> alarmTypeList = new ArrayList<>();
        // alarmTypeList.add(1);
        // queryMulti.put("alarm_type", alarmTypeList);
        // data.put("query_multi", queryMulti);

        queryInfo.put("id", VConstants.QUERY_FSS_ALARM_V113);
        queryInfo.put("table_name", tableName);

        System.out.println(queryInfo.toString());
        JSONObject queryResult = pClient.query(queryInfo);
        System.out.println(FormatUtil.formatJson(queryResult.toString()));

    }

    public void exportAlarmData(){
        JSONObject queryInfo = new JSONObject();
        JSONObject queryTerm = new JSONObject();
        JSONObject queryMulti = new JSONObject();

        // 按图片查询
        // queryTerm.put("feature",
        // "7qpXQpFlAACAAAAAHuvvu+KhAL6gPde9aZPUvXgR7row5Ug9xfx9PMmwND20SEi9F2JPvDZZtr3EKwi8xMASvjmiOrxiOTy966yxPRZOo73vwhy8cGwjPYwQcT2skQy9RkNbvKlBIb6jvwO+O4fSPB+UMD3iCuM7Ew72PN3/sL3rDUe9jFyKPm7WqLvKjBA9Xqb4vfcUqr1peKk9inx1PaB4WD2R7Ly9xEq+Pc3Z9TtOwVU+MRjdPUva0D2Y6YK9AYoYPU22+L36JlI952jSPVDwTjzpoEq9B18gvqb6XT2zPhc+KrpAvZF1IT4avoE8AEfBPQbpjj0l1ro9DLH9PV2lKr0lpZm9LTS9PTib2z1IQoW9HGgAPYq2Ir71uJc+rqacPFy23r2DXi28uN8aPYhbRz4gzLE8hzMrvmDP+DtsKrg9FXC3PeO7pr3iqQ09MOBoPRxCdb3B5qk9nZLRPVQeerzxuua8fUj4vc4jRT2SVHA9pBCDvN15Ob34KF69N6GSvX8CJ71ijH29T9GMunv8ET3W4As9TJ2LvQ4Fqb1N/lo9CY3yvLcqXr3LPAO9iEvvPDrpljwf3SK+RfzrvN8Yo70oke885QecvWw2WboIf/Y9yHK2PUk/gTyH8vm9NiTKvSZuk73mH3E9Rv2HPNV3rj3Z+cE9jfoUvvROND3Xroa9VcwwPqKhiL0=");
        // queryTerm.put("sim", 90);
        // queryTerm.put("camera_name","cameraName1");
        // queryInfo.put("query_term", queryTerm);

        JSONObject queryRange = new JSONObject();
        queryRange.put("start_time", "2017-08-28 00:00:00");
        queryRange.put("end_time", "2017-08-28 10:59:59");
        queryRange.put("order_field", "1"); // 1:告警时间
        queryRange.put("order_type", "1"); // 1:升序
        queryInfo.put("query_range", queryRange);

        // queryInfo.put("page_no", 1);
        // queryInfo.put("page_size", 1);
        // queryInfo.put("total_page", -1);
        // queryInfo.put("count", -1);

        // 指定摄像头id
        // List<String> cameraIdList = new ArrayList<>();
        // cameraIdList.add("124");
        // queryMulti.put("camera_id", cameraIdList);
        // data.put("query_multi", queryMulti);

        // 指定告警类型
        // List<Integer> alarmTypeList = new ArrayList<>();
        // alarmTypeList.add(1);
        // queryMulti.put("alarm_type", alarmTypeList);
        // data.put("query_multi", queryMulti);

        queryInfo.put("id", VConstants.GET_ALARM_EXPORT_DATA_V113);
        queryInfo.put("table_name", tableName);

        System.out.println(FormatUtil.formatJson(queryInfo.toString()));
        JSONObject queryResult = pClient.query(queryInfo);
        System.out.println(FormatUtil.formatJson(queryResult.toString()));

    }

    public void queryTest() throws Exception {
        JSONObject queryInfo = new JSONObject();
        JSONObject queryTerm = new JSONObject();
        JSONObject queryMulti = new JSONObject();

        queryTerm.put("camera_id", "11000000001310000007");
        queryInfo.put("query_term", queryTerm);

        List<String> eventId = new ArrayList<String>();
        eventId.add("1");
        eventId.add("89");
        queryMulti.put("control_event_id", eventId);
        queryInfo.put("query_multi", queryMulti);

        JSONObject queryRange = new JSONObject();
        queryRange.put("start_time", "2017-08-10 00:00:00");
        queryRange.put("end_time", "2017-08-18 23:59:59");
        queryRange.put("order_field", "1"); // 1:告警时间
        queryRange.put("order_type", "0"); // 1:升序
        queryInfo.put("query_range", queryRange);

        queryInfo.put("page_no", 1);
        queryInfo.put("page_size", 10);
        queryInfo.put("total_page", -1);
        queryInfo.put("count", -1);
        queryInfo.put("id", VConstants.QUERY_FSS_ALARM_V113);
        queryInfo.put("table_name", tableName);

        System.out.println("query :" + queryInfo.toString());
        JSONObject queryResult = pClient.query(queryInfo);
        System.out.println(FormatUtil.formatJson(queryResult.toString()));

    }

    private void insert() {
        int alarmType = 3;
        String opTime = "2017-08-15 00:00:00";
        String personId = "1";
        int libId = 1;

        int needConfirm = 1;
        int confirmStatus = 1;
        String confirmBy = "2"; // 对应名单库person_id
        // String confirmTime = "";
        String confirmComment = "balabala";

        JSONObject data = new JSONObject();
        data.put("alarm_type", alarmType);
        data.put("op_time", opTime);
        data.put("person_id", personId);
        data.put("lib_id", libId);
        data.put("need_confirm", needConfirm);
        data.put("confirm_status", confirmStatus);
        data.put("confirm_by", confirmBy);
        data.put("confirm_comment", confirmComment);

        JSONObject insertData = new JSONObject();
        insertData.put("id", VConstants.QUERY_FSS_ALARM_V113);
        insertData.put("table_name", tableName);
        insertData.put("data", data);

        System.out.println(" insert :" + insertData.toString());

        JSONObject result = pClient.insert(insertData);

        System.out.println(FormatUtil.formatJson(result.toString()));

    }

    private void getAlarmPicture() throws Exception {
        JSONObject queryInfo = new JSONObject();
        queryInfo.put("id", VConstants.QUERY_FSS_ALARM_V113);
        queryInfo.put("table_name", tableName);
         queryInfo.put("alarm_type",3);
         queryInfo.put("lib_id",6);
        queryInfo.put("op_time", "2017-08-28 00:00:04");
        queryInfo.put("person_id", "0000000000000091");

        System.out.println(queryInfo.toString());
        JSONObject queryResult = pClient.getPicture(queryInfo);

        byte[] value = queryResult.getBytes("rt_image_data");
        File fileImage = new File("E:\\FssProgram\\V1.1\\13-迭代三增删改查SDK\\03-sdk-测试结果\\fss" + "alarm01" + ".jpg");
        OutputStream out = new FileOutputStream(fileImage);
        out.write((byte[]) (value));
        out.flush();
        out.close();

        // if (value instanceof byte[]) {
        // out.write((byte[]) (value));
        // out.flush();
        // out.close();
        // }

    }

    public static void main(String args[]) {
        try {
            AlarmSearchTest alarmClient = new AlarmSearchTest();
            alarmClient.query();
            // alarmClient.queryTest();

            // alarmClient.insert();

            //alarmClient.getAlarmPicture();

//            alarmClient.exportAlarmData();

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
