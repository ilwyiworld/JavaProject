package com.znv.fss.phoenix;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.znv.fss.common.VConstants;
import com.znv.fss.conf.ConfigManager;
import com.znv.fss.phoenix.PhoenixClient;
import com.znv.kafka.ProducerBase;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/8/3.
 */
public class PersonListTest {
    private static PhoenixClient pClient = null;
    private static final String tableName = "FSS_PERSONLIST_V1_1_3_20170727";

    public PersonListTest() throws Exception {
        pClient = new PhoenixClient("jdbc:phoenix:lv130.dct-znv.com:2181:/hbase",
            "hdfs://lv130.dct-znv.com:8020/user/fss_V113/development/config");
        // hdfs://lv96.dct-znv.com:8020/user/fss_V113/development/config 开发环境
        // hdfs://lv96.dct-znv.com:8020/user/FSS_V110/sdk/config 测试环境
        // phoenixConnURL hdfsFilePath
        // pClient.createTable(tableName);
    }

    private void insert() throws Exception {
        JSONObject data = new JSONObject();
        File file = new File("cs.jpg");
        byte[] personImg = null;
        BufferedImage bufferedSrcImage = ImageIO.read(new FileInputStream(file));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(bufferedSrcImage, "jpg", out);
        personImg = out.toByteArray();

        data.put("person_name", "陈述");
        data.put("birth", "1980-01-01");// "1980-01-01"
        data.put("nation", "汉族");// 汉族
        data.put("country", "中国");// 中国
        data.put("positive_url", "");
        data.put("negative_url", "");
        data.put("addr", "江苏省南京市江宁区正方中路888号中兴力维软件有限公司");
        data.put("tel", "025-66086608");
        data.put("nature_residence", "临时居住");// 临时居住
        data.put("room_number", "901");// 901
        data.put("door_open", 0);// 0
        data.put("sex", 1);
        data.put("image_name", "陈述.jpg");
        data.put("person_img", personImg);
        String feature = "7qpXQpFlAACAAAAAjM0QvpuPOr1BVwy9jX4svslmoLw/3mu9eVoLvuDXMLsnQrU9a+JFPOshtr2jfYe9EEpUPndMSj7LSqU85Da7PBRNv70/Vsi6BXZ+PVS8Q71JzlQ8LAO7vSHuMr1b+eA97Qq5PUTnAL4r+qG9JhSAvTHxjbzS+Ja6+cxnPeQTSj1WJ1W9KnCEvOVUhL1fLQu9BQR7PF3zzT1qz8O9WNtVvZ5nnr0UHSY9bAdevV3uwD1xxsY8sw+ZvN07Xz3eq1q+3ORYO9FstLsj2TI8s4TCvGQeBr61PAo+NrUZvkfVIb3ukYI8xPDuPTSL9jpBotG9cNa7PBET8T2guyE83EsIPv33GDs0ZuW7w06QPeMwgb09I9A9QGuUvJLycT0DZp+9t+B8vJDj9j2cYUE9H5sqvssTgT0r6Re+tByhve4j2r02f8A7PUZJvrZh+r3oeCi9eyPiO1fYArwGgzU+ykcqvQBG8r3sSbO8MhYyu3YarT0uPpi9tjhlPpae5L1zN3Q6n1l1PRpAUT13TwQ+rkqwuxpgjb2OPzs9W6W3POnDnbzI9/q92PSrPAcPJr7V4F08Xfx2vbUIqr0RQcY6oTbvO+Jbz701duK9XA8zvqZvg71p3L28q6cWPNO3rL2bOiY+h4b0vWcqhLwljxC9Mn8jvamzTL0ZqPQ9wqjVvZb58r0=";
        data.put("feature", Base64.getDecoder().decode(feature));
        data.put("card_id", "555555");
        data.put("flag", 1);
        data.put("comment", "");
        data.put("control_start_time", "2017-03-23 00:00:00");
        data.put("control_end_time", "2017-12-31 23:59:59");
        data.put("is_del", "0");
        // 新增字段
        data.put("community_id", "community_01");
        data.put("community_name", "community_01");
        data.put("control_community_id", "control_community_01");
        data.put("control_person_id", "");
        data.put("control_event_id", "");
        long currentTime = System.currentTimeMillis();
        Date timeDate = new Date(currentTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = sdf.format(timeDate);

        data.put("create_time", timeStr);
        data.put("modify_time", timeStr);

        JSONArray inputList = new JSONArray();
        inputList.add(data);

        JSONObject personData = new JSONObject();
        personData.put("lib_id", 1);
        personData.put("personlib_type", 1);
        personData.put("data", inputList);
        personData.put("count",2208285);

        JSONObject insertData = new JSONObject();
        insertData.put("id", "31001");
        insertData.put("table_name", tableName);
        insertData.put("data", personData);

        System.out.println("insert :" + insertData.toString());

        JSONObject result = pClient.insert(insertData);

        System.out.println("result :" + result.toString());

    }

    private void batchInsert() throws Exception {
        JSONArray inputList = new JSONArray();

        File file = new File("cs.jpg");
        byte[] personImg = null;
        BufferedImage bufferedSrcImage = ImageIO.read(new FileInputStream(file));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(bufferedSrcImage, "jpg", out);
        personImg = out.toByteArray();
        for (int i = 1; i <= 3; i++) {
            JSONObject data = new JSONObject();
            data.put("person_name", ("陈述test" + String.valueOf(i)));
            data.put("birth", "1980-01-01");// "1980-01-01"
            data.put("nation", "汉族");// 汉族
            data.put("country", "中国");// 中国
            data.put("positive_url", "");
            data.put("negative_url", "");
            data.put("addr", "江苏省南京市江宁区正方中路888号中兴力维软件有限公司");
            data.put("tel", "025-66086608");
            data.put("nature_residence", "临时居住");// 临时居住
            data.put("room_number", "901");// 901
            data.put("door_open", 0);// 0
            data.put("sex", 1);
            data.put("image_name", "陈述.jpg");
            data.put("person_img", personImg);
            String feature = "7qpXQpFlAACAAAAAjM0QvpuPOr1BVwy9jX4svslmoLw/3mu9eVoLvuDXMLsnQrU9a+JFPOshtr2jfYe9EEpUPndMSj7LSqU85Da7PBRNv70/Vsi6BXZ+PVS8Q71JzlQ8LAO7vSHuMr1b+eA97Qq5PUTnAL4r+qG9JhSAvTHxjbzS+Ja6+cxnPeQTSj1WJ1W9KnCEvOVUhL1fLQu9BQR7PF3zzT1qz8O9WNtVvZ5nnr0UHSY9bAdevV3uwD1xxsY8sw+ZvN07Xz3eq1q+3ORYO9FstLsj2TI8s4TCvGQeBr61PAo+NrUZvkfVIb3ukYI8xPDuPTSL9jpBotG9cNa7PBET8T2guyE83EsIPv33GDs0ZuW7w06QPeMwgb09I9A9QGuUvJLycT0DZp+9t+B8vJDj9j2cYUE9H5sqvssTgT0r6Re+tByhve4j2r02f8A7PUZJvrZh+r3oeCi9eyPiO1fYArwGgzU+ykcqvQBG8r3sSbO8MhYyu3YarT0uPpi9tjhlPpae5L1zN3Q6n1l1PRpAUT13TwQ+rkqwuxpgjb2OPzs9W6W3POnDnbzI9/q92PSrPAcPJr7V4F08Xfx2vbUIqr0RQcY6oTbvO+Jbz701duK9XA8zvqZvg71p3L28q6cWPNO3rL2bOiY+h4b0vWcqhLwljxC9Mn8jvamzTL0ZqPQ9wqjVvZb58r0=";
            data.put("feature", Base64.getDecoder().decode(feature));
            data.put("card_id", "555555");
            data.put("flag", 1);
            data.put("comment", "");
            data.put("control_start_time", "2017-03-23 00:00:00");
            data.put("control_end_time", "2017-12-31 23:59:59");
            data.put("is_del", "0");
            // 新增字段
            data.put("community_id", "community_01");
            data.put("community_name", "community_01");
            data.put("control_community_id", "control_community_01");
            data.put("control_person_id", "");
            data.put("control_event_id", "");

            long currentTime = System.currentTimeMillis();
            Date timeDate = new Date(currentTime);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timeStr = sdf.format(timeDate);

            data.put("create_time", timeStr);
            data.put("modify_time", timeStr);

            inputList.add(data);
        }

        JSONArray inputData1 = new JSONArray(2);
        for (int i = 0; i < 3; i++) {
            inputData1.add(i, inputList.get(i));
        }
        JSONObject personData = new JSONObject();
        personData.put("lib_id", 2);
        personData.put("count", 2208285);
        personData.put("personlib_type", 1);
        personData.put("is_send", "0");
        personData.put("is_end", "0");
        personData.put("data", inputData1);

        JSONObject insertData = new JSONObject();
        insertData.put("id", "31001");
        insertData.put("table_name", tableName);
        insertData.put("data", personData);

        // 第一次新增 is_send,is_end:0,0
        JSONObject result1 = pClient.insert(insertData);
        System.out.println("result1 :" + result1.toString());

        // // 第二次新增 is_send,is_end:1,0
        // personData.remove("is_send");
        // personData.put("is_send",1);
        // result1 = pClient.insert(insertData);
        // System.out.println("--------------第二次新增----------------");
        // System.out.println("result2 :" + result1.toString());

        // 第三次新增 is_send,is_end:0,1    is_send,is_end:1,1
        JSONObject personData2 = new JSONObject();
        personData2.put("lib_id", 99);
        personData2.put("count", 2208285);
        personData2.put("personlib_type", 1);
        personData2.put("is_send", "1");
        personData2.put("is_end", "1");
        JSONArray inputData2 = new JSONArray(1);
        inputData2.add(0, inputList.get(2));
        personData2.put("data", inputData2);

        JSONObject insertData2 = new JSONObject();
        insertData2.put("id", "31001");
        insertData2.put("table_name", tableName);
        insertData2.put("data", personData2);
        JSONObject result2 = pClient.insert(insertData2);
        System.out.println("--------------第三次新增结果-------------------");
        System.out.println("result3 :" + result2.toString());

    }

    private void update() throws Exception {
        JSONObject data = new JSONObject();
        File file = new File("cs.jpg");
        byte[] personImg = null;
        BufferedImage bufferedSrcImage = ImageIO.read(new FileInputStream(file));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(bufferedSrcImage, "jpg", out);
        personImg = out.toByteArray();
        data.put("person_id", "1511500795988000");
        // data.put("tel", "111");
        data.put("is_del", "0");
        // data.put("person_name", "test-lq");
        data.put("personlib_type", 1);

        JSONObject personData = new JSONObject();
        personData.put("lib_id", 1);
        personData.put("original_lib_id",1);
        // personData.put("personlib_type", 1);
        personData.put("data", data);

        JSONObject insertData = new JSONObject();
        insertData.put("id", "31001");
        insertData.put("table_name", tableName);
        insertData.put("data", personData);

        System.out.println("update :" + insertData.toString());

        JSONObject result = pClient.update(insertData);

        System.out.println("result :" + result.toString());

    }

    private void delete() {
        JSONObject data = new JSONObject();
        data.put("person_id", "1511259276633000");
        //data.put("person_name", "陈述");
        JSONObject personData = new JSONObject();
        personData.put("lib_id", 1);
        personData.put("data", data);

        JSONObject insertData = new JSONObject();
        insertData.put("id", "31001");
        insertData.put("table_name", tableName);
        insertData.put("data", personData);

        System.out.println("delete :" + insertData.toString());

        JSONObject result = pClient.delete(insertData);

        System.out.println("result :" + result.toString());

    }

    private void query() throws Exception {
        JSONObject queryTerm = new JSONObject();
        queryTerm.put("lib_id", 1);

        JSONObject queryRange = new JSONObject();
        queryRange.put("start_time", "2017-01-01 00:00:00");
        queryRange.put("end_time", "2017-08-30 00:00:00");
        queryRange.put("order", "1");

        JSONObject queryFeature = new JSONObject();
        // 静态比对
        String feature = "7qpXQpFlAACAAAAAjM0QvpuPOr1BVwy9jX4svslmoLw/3mu9eVoLvuDXMLsnQrU9a+JFPOshtr2jfYe9EEpUPndMSj7LSqU85Da7PBRNv70/Vsi6BXZ+PVS8Q71JzlQ8LAO7vSHuMr1b+eA97Qq5PUTnAL4r+qG9JhSAvTHxjbzS+Ja6+cxnPeQTSj1WJ1W9KnCEvOVUhL1fLQu9BQR7PF3zzT1qz8O9WNtVvZ5nnr0UHSY9bAdevV3uwD1xxsY8sw+ZvN07Xz3eq1q+3ORYO9FstLsj2TI8s4TCvGQeBr61PAo+NrUZvkfVIb3ukYI8xPDuPTSL9jpBotG9cNa7PBET8T2guyE83EsIPv33GDs0ZuW7w06QPeMwgb09I9A9QGuUvJLycT0DZp+9t+B8vJDj9j2cYUE9H5sqvssTgT0r6Re+tByhve4j2r02f8A7PUZJvrZh+r3oeCi9eyPiO1fYArwGgzU+ykcqvQBG8r3sSbO8MhYyu3YarT0uPpi9tjhlPpae5L1zN3Q6n1l1PRpAUT13TwQ+rkqwuxpgjb2OPzs9W6W3POnDnbzI9/q92PSrPAcPJr7V4F08Xfx2vbUIqr0RQcY6oTbvO+Jbz701duK9XA8zvqZvg71p3L28q6cWPNO3rL2bOiY+h4b0vWcqhLwljxC9Mn8jvamzTL0ZqPQ9wqjVvZb58r0=";
        queryFeature.put("feature", feature);
        queryFeature.put("sim", 80);

        JSONObject queryMulti = new JSONObject();
        List<String> personId = new ArrayList<String>();
        personId.add("0000000000000001");
        personId.add("0000000000000003");
        queryMulti.put("person_id", personId);

        JSONObject insertData = new JSONObject();
        insertData.put("query_range_modify", queryRange);
        //insertData.put("query_term", queryTerm);
        //insertData.put("query_multi", queryMulti);
        insertData.put("query_feature", queryFeature);
        insertData.put("id", "31002");
        insertData.put("table_name", tableName);
        insertData.put("count", -1);
        insertData.put("total_page", -1);
        insertData.put("page_no", 1);
        insertData.put("page_size", 4);
        insertData.put("is_del", "1"); // todo test

        System.out.println("query :" + insertData.toString());

        JSONObject rs = pClient.query(insertData);

        System.out.println("result :" + rs.toString());

        for (String keyset : rs.keySet()) {
            if (keyset.equalsIgnoreCase("data")) {
                List<JSONObject> objList = (List<JSONObject>) rs.get("data");
                int idx = 0;
                for (JSONObject ele : objList) {
                    for (Object key : ele.keySet()) {
                        if (key.toString().equals("person_img")) {
                            idx++;
                            System.out.println(" idx = " + idx);
                            File fileImage = new File(
                                "E:\\FssProgram\\V1.1\\13-迭代三增删改查SDK\\03-sdk-测试结果\\fss" + idx + ".jpg");
                            OutputStream out = new FileOutputStream(fileImage);
                            Object value = ele.get(key);
                            // byte[] imageData = rs.getBytes(keyset);
                            if (value instanceof byte[]) {
                                out.write((byte[]) (value));
                                out.flush();
                                out.close();
                            }

                        }
                    }
                    System.out.println("");
                }
            } else {
                System.out.println(keyset + ": " + rs.get(keyset));
            }
        }

    }

    private void getPersonInfo() {

        JSONArray arrayList = new JSONArray();
        JSONObject data = new JSONObject();
        data.put("lib_id", 3);
        data.put("person_id", "0000000000000001");
        arrayList.add(data);
        JSONObject data1 = new JSONObject();
        data1.put("lib_id", 3);
        data1.put("person_id", "0000000000000002");
        arrayList.add(data1);

        JSONObject insertData = new JSONObject();
        insertData.put("id", "31001");
        insertData.put("table_name", tableName);
        insertData.put("data", arrayList);

        System.out.println(" get person info :" + insertData.toString());
        JSONObject rs = pClient.searchPersonList(insertData);

        System.out.println("getPersonInfo result : " + rs.toString());
    }

    private void getCount(){
        JSONObject data = new JSONObject();
        data.put("lib_id", 3);
        JSONObject insertData = new JSONObject();
        insertData.put("id", "31001");
        insertData.put("table_name", tableName);
        insertData.put("data", data);

        System.out.println(" getCount sql :" + insertData.toString());
        JSONObject rs = pClient.count(insertData);
        System.out.println(" person list count :" + rs.toString());

    }

    private void getPersonPicture() throws Exception{
        JSONObject queryInfo = new JSONObject();
        queryInfo.put("id","31001");
        queryInfo.put("table_name", "FSS_PERSONLIST_V1_1_3_20170727");
        queryInfo.put("person_id", "0000000000000029");
        queryInfo.put("lib_id", 1);

        JSONObject queryResult = pClient.getPicture(queryInfo);

        System.out.println(" result :" + queryResult.toString());

        byte[] value = queryResult.getBytes("person_img");
        File fileImage = new File("E:\\FssProgram\\V1.1\\13-迭代三增删改查SDK\\03-sdk-测试结果\\fss" + "person20170830" + ".jpg");
        OutputStream out = new FileOutputStream(fileImage);
        if (value instanceof byte[]) {
            out.write((byte[]) (value));
            out.flush();
            out.close();
        }
    }

    private void getCardPicture() throws Exception {
        JSONObject queryInfo = new JSONObject();
        queryInfo.put("id", "31008"/* VConstants.GET_PERSON_CARD_PICTURE_V113 */);
        queryInfo.put("table_name", "FSS_PERSONLIST_V1_1_3_20170727");
        queryInfo.put("person_id", "1504679667946000");
        queryInfo.put("lib_id", 1);

        JSONObject queryResult = pClient.getPicture(queryInfo);

        System.out.println(" result :" + queryResult.toString());

        String cardStr = queryResult.getString("positive_url");
        System.out.println("positive_url :" + cardStr);
        File fileImage = new File("E:\\FssProgram\\V1.1\\13-迭代三增删改查SDK\\03-sdk-测试结果\\fss" + "card0906" + ".jpg");
        OutputStream out = new FileOutputStream(fileImage);
        // BASE64Decoder decoder = new BASE64Decoder();
        byte[] value = Base64.getDecoder().decode(cardStr);
        if (value instanceof byte[]) {
            out.write((byte[]) (value));
            out.flush();
            out.close();
        }
    }

    private void testBatchModifyFlag() throws Exception {
        JSONObject data = new JSONObject();

        data.put("lib_id", 6);
        data.put("flag", 1);
        data.put("personlib_type", 1);

        JSONObject insertData = new JSONObject();
        insertData.put("id", "31011");
        insertData.put("table_name", tableName);
        insertData.put("data", data);

        System.out.println(FormatUtil.formatJson(insertData.toString()));

        JSONObject result = pClient.update(insertData);

        System.out.println("result :" + result.toString());

    }

    private void testWait() throws Exception {
        long currentTime = System.currentTimeMillis();
        Date timeDate = new Date(currentTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = sdf.format(timeDate);
        System.out.println("Current time is :" + timeStr);
        wait(1000 * 2);

        long currentTime2 = System.currentTimeMillis();
        String timeStr2 = sdf.format(new Date(currentTime2));
        System.out.println("Current time is :" + timeStr2);

    }

    private void testSend2Kafka() {
        ProducerBase producer = pClient.getProducer();
        JSONObject notifyMsg = new JSONObject();
        notifyMsg.put("msg_type", ConfigManager.getString(VConstants.NOTIFY_TOPIC_MSGTYPE));
        notifyMsg.put("table_name", "FSS_DEVELOP_410.FSS_PERSONLIST_V1_1_3_20170727");
        notifyMsg.put("primary_id", 99); // 批量新增 or 单条新增
        notifyMsg.put("reference_id", null);
        long currentTime = System.currentTimeMillis();
        Date timeDate = new Date(currentTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = sdf.format(timeDate);
        notifyMsg.put("send_time", timeStr);
        boolean ret = producer.sendData(notifyMsg);
        System.out.println("batch-PersonListClient-batch-ret:" + ret + ",send_time:" + timeStr);
        System.out.println("batch-PersonListClient-batch-ret:" + notifyMsg.toString());
    }

    private void getPersonListCount() throws Exception {
        JSONObject queryInfo = new JSONObject();
        queryInfo.put("id", "31001");
        queryInfo.put("table_name", tableName);

        JSONObject data1 = new JSONObject();
        data1.put("lib_id", 1);
        queryInfo.put("data", data1);

        JSONObject queryResult = pClient.count(queryInfo);

        System.out.println(" result :" + queryResult.toString());
    }

    public static void main(String[] args) {
        try {
            PersonListTest test = new PersonListTest();

             test.insert();
            // test.insertTest();

            // test.update();

            // test.delete();

            // test.query();

            // test.batchInsert();
            // test.testSend2Kafka();

            // test.getCardPicture();
            // test.getPersonPicture();
            // test.testBatchModifyFlag();
            // test.getPersonListCount();
            // test.getPersonInfo();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (pClient != null) {
                    pClient.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
}
