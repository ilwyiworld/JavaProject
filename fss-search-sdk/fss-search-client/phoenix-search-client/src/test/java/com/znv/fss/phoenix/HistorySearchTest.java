package com.znv.fss.phoenix;
import com.alibaba.fastjson.JSONObject;
import com.znv.fss.common.VConstants;
import com.znv.fss.phoenix.PhoenixClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by User on 2017/8/2.
 */
public class HistorySearchTest {
    private static PhoenixClient pClient = null;
    private static final String tableName = "FSS_HISTORY_V1_1_3_20170727_10000W_0117";

    public HistorySearchTest() throws Exception {
        String hdfsFilePath = "hdfs://10.45.157.120:8020/user/fss_V113/development/config";
        pClient = new PhoenixClient("jdbc:phoenix:10.45.157.120:2181:/hbase", hdfsFilePath);
    }

    public void query() throws Exception {
        JSONObject queryInfo = new JSONObject();
        JSONObject queryTerm = new JSONObject();
        JSONObject queryMulti = new JSONObject();

        // 按图片查询
        // queryTerm.put("feature",
        // "7qpXQpFlAACAAAAAJWvOPbfvYrz1faY9VQ4RPhw1sTwyvpy7DxnDvIVmg70PfK08i/oAPvKXzD1wJjG9wcMcPg1RFD60woM9+hTLPFpcXz6kJZO7hbOWvJaYHL1sHm+9Fsw1Pefh6L1uD/095Cmlvctc2L1wXR+9Jhw2u1MKRLyfrhm+J6dkvSYFML1cCaY9/9IIPvtS2r3MVYq8Dovlveii7rzHwri9mzxKPQufij1Fjzq9X0bLPO4V8Tspl2k9VTq2vfVOlT1vzv28nRupvMf3hD4/Qz8+S5LpO3YEtT3T3mq8UP0PPZTu+j107pQ94Ne0PZ/MQL5OwYa9zvMZvvN8yzvdCgG9Pi71PR1Mu70FEM29Am95PVuqGb5G8/o8UtkWPScujT2SzeU9LICfPO/vY7zRZ/u96lEMu3OQkj1oI5s9o9BgPQKvt7kTuQM+/0szPZ+XDb2GRLC9Aje9PS+flj30oba9f4IAvp8aNjwkyA4+P7rhvXgGy7wUhQU9pWSHvU7lQTyMpx292/21vLmztT2F1qo94SZsPeRwID5qF6E9wMKsu1LECT4mEY+9Z1oEvpRD073D7AM9uYwTPULizj2cRry9fva0PAzaxL09uVU80YAFPclQxDxAMwm9UQECvp+BZD7/Ey2+U1U0vRDVLrx/mwq827qRPeDKp7thCs09dbGKPKn9i70=");
        // queryTerm.put("sim", 96);
        // queryTerm.put("order", "0");
        // queryTerm.put("person_id","20");
        String feature = "7qpXQsFdAAAAAQAAL8PBvBpnOjvl/mU9IN8FPTDvnT0e1888/QzKPe8Tzj3ld3c9ZTn0PHxggzsYzH49tOgvPCrUID205369t6q+vXq1ab2TIxC+AhjzPOHgzT2JrK47YqC3vQC3LzslyTi9dzHqvJCxm70fB688nxcWvW2tiLwuiqi76h68O66rtryqYTw9k694vfMXCz4NxkK9MJEkPTtjkL3nIQ09cImUvRb7nr1oItu8MCKkPG37c7wQHTk9S1U+O8GQHj3Mx969NH2qPbrpBjxkrSC+B82muo7DLD35GQa+NPOUPLfbzL0dUra8EeU0vRAjEr6hCoy9jayPO/XPZb1A2nI9CZdQvT2/vL3dZE69IpDwPPUiXrr7lza9h8rcvXvDo7zlEcY9PEvyvea5lDyOO/u92OWHvaFTNT0EBZ29msdGPLuYUr1TQCe9KdVivFAxvz0uTC49rqqvvQmAAz1z+qg9LYsKPsGkfr1iDSe7rzKpvXE6fDxqGRm+WP/svKyeML5X/sQ94yeBvbUzkj1hdtE9UDYHPBStqT17V449gsHCvLeDr71KUNA7qIIxvVWvqL3Ioww91UaDPMlOY73+4AC9yKAAvHHyaLwsNIO9ATmQPdU6nz1ZwTO+ZTbLPdi/5jw9elo9epb3OQZHIbzNtMU9J36ZvCxoNT1eugs9qCZqPcE3DDwFk3Y9xzCHveg02r2r2Fs9LU2ZPW/7vjze5w69vhiZvGhcebsdxdO92WhUPZCvOD1d/2s8wTV1vXqixzxHF2g9Ll+cu5c9ATzNY508X0ONO/gFu7y6Egq9fsuIvFEhkbsQb5899hS8vdka0TxtnN88FKksPSwihDsSH3o9kZ3cOlKg4jymyOy8inzwPFMZ57z1yHY9EZtBvcteOj1ctqo6pcC3PffROjy9Pt48bugOPLl/UDvl+5y8OS8BvhdAAr1G3KY92nh8vaZGwbxV/Bw+iBaTPFbhO73OeGQ9g8jPu5A4TrxMbDK91epfPSCKq7xKC4S9V22vvetZw7yes+y8fqOXvGNahDun5b29xh9iPVithT1O9yQ9thmmPekDQz1lF4g9ptx4PbZaBjxLaqc7NpMGPKsZYT2AM5M9HbsRvdwyaL1Qej29qSCJuw1ksDxNgZa9eGXEPSuMCr333mG9in6IvRKsCr0h02O9sQ7OPKb1bLwisZm889ynPQyk6r3oP468+ePxvXhHH72gS7I8+lyPvcwfsbwJGm+8LLk9vY07272jYL07S8qEvZz/Qb1GihE+1yp7PRJwUD29IdE9W/DGPIh3uL13D+u869ypvfqFkb2406+9iRx1PTy+AbspcOC8NKiRvaw5BbulWSc81AyRPe+/nT24HUi9OeFVvQ==";

        queryTerm.put("feature", feature);
        queryTerm.put("order", "0");
//        queryInfo.put("query_term", queryTerm);

        JSONObject queryRange = new JSONObject();
        queryRange.put("start_time", "2017-08-26 00:00:00");
        queryRange.put("end_time", "2017-09-01 23:59:59");

        queryInfo.put("query_range", queryRange);

        queryInfo.put("page_no", 1);
        queryInfo.put("page_size", 10);
        queryInfo.put("total_page", -1);
        queryInfo.put("count", -1);

        // 指定摄像头id
        // List<String> cameraIdList = new ArrayList<>();
        // cameraIdList.add("123");
        // // cameraIdList.add("2");
        // queryMulti.put("camera_id", cameraIdList);
        // queryInfo.put("query_multi", queryMulti);

        queryInfo.put("id", VConstants.QUERY_FSS_HISTORY_V113);
        queryInfo.put("table_name", tableName);

        System.out.println(queryInfo.toString());
        JSONObject queryResult = pClient.query(queryInfo);
        System.out.println(FormatUtil.formatJson(queryResult.toString()));

        // for (String keyset : queryResult.keySet()) {
        // if (keyset.equalsIgnoreCase("data")) {
        // List<JSONObject> objList = (List<JSONObject>) queryResult.get("data");
        // int idx = 0;
        // for (JSONObject ele : objList) {
        // for (Object key : ele.keySet()) {
        // if (key.toString().equals("rt_image_data")) {
        // idx++;
        // System.out.println(" idx = " + idx);
        // File fileImage = new File(
        // "E:\\FssProgram\\V1.1\\13-迭代三增删改查SDK\\03-sdk-测试结果\\fss" +"history"+ idx + ".jpg");
        // OutputStream out = new FileOutputStream(fileImage);
        // Object value = ele.get(key);
        // // byte[] imageData = rs.getBytes(keyset);
        // if (value instanceof byte[]) {
        // out.write((byte[]) (value));
        // out.flush();
        // out.close();
        // }
        //
        // }
        // }
        // System.out.println("");
        // }
        // } else {
        // System.out.println(keyset + ": " + queryResult.get(keyset));
        // }
        // }

    }

    private void getHistoryPictureByIndex() throws Exception {
        JSONObject queryInfo = new JSONObject();
        queryInfo.put("id", "12001");
        queryInfo.put("table_name", tableName);
        queryInfo.put("camera_id", "44030000001310000001");
        queryInfo.put("enter_time", "2017-08-26 22:30:20");
        queryInfo.put("track_idx", "1861");
        queryInfo.put("task_idx", "201708242335590001000000035");

        System.out.println("sql :" + queryInfo.toString());

        JSONObject queryResult = pClient.getPicture(queryInfo);

        System.out.println(" result :" + queryResult.toString());

        byte[] value = queryResult.getBytes("rt_image_data3");
        // System.out.println(" VALUE SIZE :" + value.length);
        File fileImage = new File(
                "E:\\FssProgram\\V1.1\\13-迭代三增删改查SDK\\03-sdk-测试结果\\fss" + "history_by_index_02" + ".jpg");
        OutputStream out = new FileOutputStream(fileImage);
        if (value instanceof byte[]) {
            out.write((byte[]) (value));
            out.flush();
            out.close();
        }
    }

    private void getHistoryPicture() throws Exception {
        JSONObject queryInfo = new JSONObject();
        queryInfo.put("id", VConstants.QUERY_FSS_HISTORY_V113);
        queryInfo.put("table_name", tableName);
        queryInfo.put("uuid", "d4cbb9f0-bfb6-476d-811c-f5ad8150759d");
        queryInfo.put("enter_time", "2017-08-26 22:30:59");

        JSONObject queryResult = pClient.getPicture(queryInfo);

        System.out.println(" result :" + queryResult.toString());

        byte[] value = queryResult.getBytes("rt_image_data3");
        System.out.println(" VALUE SIZE :" + value.length);
        File fileImage = new File("E:\\FssProgram\\V1.1\\13-迭代三增删改查SDK\\03-sdk-测试结果\\fss" + "history02" + ".jpg");
        OutputStream out = new FileOutputStream(fileImage);
        if (value instanceof byte[]) {
            out.write((byte[]) (value));
            out.flush();
            out.close();
        }
    }

    private void getHistoryPictureSuperSearch() throws Exception {
        JSONObject queryInfo = new JSONObject();
        queryInfo.put("id", VConstants.GET_HISTORY_SUPER_SEARCH_PICTURE_V113);
        queryInfo.put("table_name", tableName);
        queryInfo.put("uuid", "43c102a2-5df8-447e-9686-d41f1eb0f051");
        queryInfo.put("enter_time", "2017-06-27 00:18:35");

        JSONObject queryResult = pClient.getPicture(queryInfo);

        System.out.println(" result :" + queryResult.toString());

        byte[] value = queryResult.getBytes("rt_image_data3");
        System.out.println(" VALUE SIZE :" + value.length);
        File fileImage = new File("E:\\FssProgram\\V1.1\\13-迭代三增删改查SDK\\03-sdk-测试结果\\fss" + "history01" + ".jpg");
        OutputStream out = new FileOutputStream(fileImage);
        if (value instanceof byte[]) {
            out.write((byte[]) (value));
            out.flush();
            out.close();
        }
    }

    public static void main(String args[]) {
        try {
            HistorySearchTest historyClient = new HistorySearchTest();
            historyClient.query();
            // historyClient.getHistoryPicture();
            //historyClient.getHistoryPictureByIndex();

//            historyClient.getHistoryPictureSuperSearch();

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
