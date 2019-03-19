package com.znv.fss.phoenix;
import com.alibaba.fastjson.JSONObject;
import com.znv.fss.phoenix.PhoenixClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/8/4.
 */
public class RelationShipTest {
    private static PhoenixClient pClient = null;
    private static final String tableName = "FSS_RELATIONSHIP_V1_1_3_20170727";

    public RelationShipTest() throws Exception {
        pClient = new PhoenixClient("jdbc:phoenix:lvt102.dct-znv.com:2181:/hbase",
            "hdfs://lvt102.dct-znv.com:8020/user/fss_V113/development/config");
    }

    private void insert() {
        JSONObject data = new JSONObject();
        String personId = "1";
        int personIdInt = Integer.valueOf(personId);
        data.put("person_id", String.format("%16d", personIdInt).replace(" ", "0"));
        data.put("relation_id", String.format("%16d", 10).replace(" ", "0"));
        data.put("relation_type", 1);
        data.put("relation_grade", 0);

        JSONObject insertData = new JSONObject();
        insertData.put("id", "31004");
        insertData.put("table_name", tableName);
        insertData.put("data", data);

        System.out.println(" insert :" + insertData.toString());

        JSONObject result = pClient.insert(insertData);
        // JSONObject result = pClient.update(insertData);
        // JSONObject result = pClient.delete(insertData);

        System.out.println(" result :" + result.toString());

    }

    private void query() {
        JSONObject queryTerm = new JSONObject();
        queryTerm.put("person_id", "0000000000000001");

        JSONObject queryMulti = new JSONObject();
        List<Integer> libId = new ArrayList<Integer>();
        libId.add(1);
        libId.add(2);
        queryMulti.put("relation_lib_id", libId);

        JSONObject insertData = new JSONObject();
        insertData.put("query_term", queryTerm);
         insertData.put("query_multi", queryMulti);
        insertData.put("id", "31004");
        insertData.put("table_name", tableName);
        insertData.put("count", -1);
        insertData.put("total_page", -1);
        insertData.put("page_no", 1);
        insertData.put("page_size", 4);

        System.out.println("query :" + insertData.toString());

        JSONObject rs = pClient.query(insertData);

        System.out.println("result :" + rs.toString());
    }

    public static void main(String[] args) {
        try {
            RelationShipTest test = new RelationShipTest();

            // test.insert();

            test.query();

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                pClient.close();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

}
